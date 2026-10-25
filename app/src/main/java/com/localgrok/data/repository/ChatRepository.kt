package com.localgrok.data.repository

import com.localgrok.data.local.dao.ChatDao
import com.localgrok.data.local.dao.MessageDao
import com.localgrok.data.local.entity.ChatEntity
import com.localgrok.data.local.entity.MessageEntity
import com.localgrok.data.remote.OllamaApiService
import com.localgrok.data.remote.OllamaClient
import com.localgrok.data.remote.OllamaStreamReader
import com.localgrok.data.remote.SearxngApiService
import com.localgrok.data.remote.SearxngClient
import com.localgrok.data.remote.model.OllamaChatRequest
import com.localgrok.data.remote.model.OllamaMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChatRepository(private val chatDao: ChatDao, private val messageDao: MessageDao) {
    private var ollamaService: OllamaApiService? = null
    private var searxngService: SearxngApiService? = null
    private val json = OllamaClient.getJson()

    companion object {

        private val TOOL_CALL_REGEX_CLOSED =
            Regex("""<tool_call>[\s\S]*?</tool_call>""", RegexOption.DOT_MATCHES_ALL)

        private val TOOL_CALL_REGEX_OPEN =
            Regex("""<tool_call>[\s\S]*$""", RegexOption.DOT_MATCHES_ALL)

        private const val TOOL_CALL_PREFIX = "<tool_call>"

        private val TOOL_RESULT_REGEX =
            Regex("""<tool_result>[\s\S]*?</tool_result>""", RegexOption.DOT_MATCHES_ALL)

        private val TOOL_RESULT_UNCLOSED_REGEX =
            Regex("""<tool_result>[\s\S]*$""", RegexOption.DOT_MATCHES_ALL)
    }

    fun configureServer(serverIp: String, port: Int = 11434) {
        ollamaService = OllamaClient.createService(serverIp, port)
    }

    fun configureSearxng(serverIp: String, port: Int = 8888) {
        searxngService = SearxngClient.createService(serverIp, port)
    }

    fun isServerConfigured(): Boolean = ollamaService != null

    fun isSearchConfigured(): Boolean = searxngService != null

    fun getAllChats(): Flow<List<ChatEntity>> = chatDao.getAllChats()

    fun getChatById(chatId: Long): Flow<ChatEntity?> = chatDao.getChatByIdFlow(chatId)

    suspend fun createChat(title: String, model: String): Long {
        val chat = ChatEntity(title = title, model = model)
        return chatDao.insertChat(chat)
    }

    suspend fun updateChatTitle(chatId: Long, title: String) {
        chatDao.updateChatTitle(chatId, title)
    }

    suspend fun deleteChat(chatId: Long) {
        chatDao.deleteChatById(chatId)
    }

    suspend fun deleteAllChats() {
        chatDao.deleteAllChats()
    }

    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesForChat(chatId)

    suspend fun addUserMessage(chatId: Long, content: String): Long {
        val message = MessageEntity(chatId = chatId, role = "user", content = content)
        chatDao.updateChatTimestamp(chatId)
        return messageDao.insertMessage(message)
    }

    suspend fun createAssistantMessage(chatId: Long): Long {
        val message =
            MessageEntity(chatId = chatId, role = "assistant", content = "", isStreaming = true)
        return messageDao.insertMessage(message)
    }

    suspend fun updateMessageContent(messageId: Long, content: String, isStreaming: Boolean) {
        messageDao.updateMessageContent(messageId, content, isStreaming)
    }

    suspend fun updateMessageState(
        messageId: Long,
        content: String,
        isStreaming: Boolean,
        isThinking: Boolean,
        toolUsed: Boolean = false,
        toolDisplayName: String = "",
    ) {
        messageDao.updateMessageState(
            messageId,
            content,
            isStreaming,
            isThinking,
            toolUsed,
            toolDisplayName,
        )
    }

    suspend fun updateMessageStateWithReasoning(
        messageId: Long,
        content: String,
        isStreaming: Boolean,
        isThinking: Boolean,
        reasoningContent: String,
        toolUsed: Boolean = false,
        toolDisplayName: String = "",
    ) {
        messageDao.updateMessageStateWithReasoning(
            messageId,
            content,
            isStreaming,
            isThinking,
            reasoningContent,
            toolUsed,
            toolDisplayName,
        )
    }

    suspend fun updateReasoningContent(messageId: Long, reasoningContent: String) {
        messageDao.updateReasoningContent(messageId, reasoningContent)
    }

    suspend fun streamChatWithCallback(
        model: String,
        messages: List<MessageEntity>,
        systemPrompt: String? = null,
        thinkEnabled: Boolean = false,
        onThinkingToken: suspend (String) -> Unit = {},
        onToken: suspend (String) -> Unit,
        onComplete: suspend () -> Unit,
        onError: suspend (String) -> Unit,
    ) =
        withContext(Dispatchers.IO) {
            val service =
                ollamaService
                    ?: run {
                        onError("Server not configured. Please set server IP in settings.")
                        return@withContext
                    }

            try {

                val ollamaMessages = buildList {
                    if (!systemPrompt.isNullOrBlank()) {
                        add(OllamaMessage(role = "system", content = systemPrompt))
                    }

                    addAll(
                        messages.map { msg ->
                            OllamaMessage(role = msg.role, content = msg.content)
                        }
                    )
                }

                val request =
                    OllamaChatRequest(
                        model = model,
                        messages = ollamaMessages,
                        stream = true,
                        think = if (thinkEnabled) true else null,
                    )

                val response = service.chatStream(request)

                if (!response.isSuccessful) {
                    response.errorBody()?.close()
                    onError("API Error: ${response.code()} - ${response.message()}")
                    return@withContext
                }

                val responseBody =
                    response.body()
                        ?: run {
                            onError("Empty response body")
                            return@withContext
                        }

                OllamaStreamReader.read(responseBody, json, thinkEnabled, onThinkingToken, onToken)
                onComplete()
            } catch (e: CancellationException) {

                throw e
            } catch (e: Exception) {
                onError("Network error: ${e.message}")
            }
        }

    suspend fun listModels(): Result<List<String>> =
        withContext(Dispatchers.IO) {
            val service =
                ollamaService
                    ?: return@withContext Result.failure(Exception("Server not configured"))

            try {
                val response = service.listModels()
                if (response.isSuccessful) {
                    val models = response.body()?.models?.map { it.name } ?: emptyList()
                    Result.success(models)
                } else {
                    response.errorBody()?.close()
                    Result.failure(Exception("Failed to fetch models: ${response.code()}"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun checkServerConnection(): Boolean =
        withContext(Dispatchers.IO) {
            val service = ollamaService ?: return@withContext false

            try {
                val response = service.healthCheck()

                response.body()?.close()
                response.errorBody()?.close()
                response.isSuccessful
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Connection check failed: ${e.message}", e)
                false
            }
        }

    fun cleanToolArtifacts(response: String): String {
        var cleaned = response

        cleaned = TOOL_RESULT_REGEX.replace(cleaned, "")

        cleaned = TOOL_RESULT_UNCLOSED_REGEX.replace(cleaned, "")

        cleaned = TOOL_CALL_REGEX_CLOSED.replace(cleaned, "")

        cleaned = TOOL_CALL_REGEX_OPEN.replace(cleaned, "")

        return cleaned.trim()
    }

    fun isStartingWithToolCall(response: String): Boolean {
        val trimmed = response.trimStart()
        return trimmed.startsWith(TOOL_CALL_PREFIX) ||
            TOOL_CALL_PREFIX.startsWith(trimmed) &&
                trimmed.isNotEmpty() &&
                trimmed.length < TOOL_CALL_PREFIX.length
    }

    fun detectToolCall(response: String): ToolCall? {
        val payload = findToolCallPayload(response) ?: return null
        val normalizedName = payload.name.lowercase()
        return when (normalizedName) {
            "web_search",
            "websearch" -> payload.query?.let { ToolCall.WebSearch(it) }

            "get_datetime",
            "get_timedate",
            "get_time",
            "current_time" -> ToolCall.GetDateTime
            else -> null
        }
    }

    private fun findToolCallPayload(response: String): ToolCallPayload? {

        val startIndex = response.indexOf(TOOL_CALL_PREFIX)
        if (startIndex == -1) return null
        val afterTagIndex = startIndex + TOOL_CALL_PREFIX.length

        val trailing = response.substring(afterTagIndex)
        val endIndex = trailing.indexOf("</tool_call>")
        val inner = if (endIndex != -1) trailing.substring(0, endIndex) else trailing

        val jsonStart = inner.indexOf('{')
        if (jsonStart == -1) return null

        var depth = 0
        var jsonEnd = -1
        var inString = false
        var escapeNext = false

        for (i in jsonStart until inner.length) {
            val char = inner[i]

            if (escapeNext) {
                escapeNext = false
                continue
            }

            when (char) {
                '\\' -> {
                    escapeNext = true
                }

                '"' -> {
                    inString = !inString
                }

                '{' -> {
                    if (!inString) depth++
                }

                '}' -> {
                    if (!inString) {
                        depth--
                        if (depth == 0) {
                            jsonEnd = i
                            break
                        }
                    }
                }
            }
        }

        val jsonString =
            if (jsonEnd != -1) {
                inner.substring(jsonStart, jsonEnd + 1)
            } else {

                inner.substring(jsonStart)
            }

        return parseJsonPayload(jsonString)
    }

    private fun parseJsonPayload(jsonString: String): ToolCallPayload? {
        return runCatching {
                val element: JsonElement = json.decodeFromString(jsonString)
                val obj = element.jsonObject
                val name = obj["name"]?.jsonPrimitive?.contentOrNull ?: return null
                val query = obj["query"]?.jsonPrimitive?.contentOrNull
                ToolCallPayload(name = name, query = query)
            }
            .getOrNull()
    }

    private data class ToolCallPayload(val name: String, val query: String?)

    fun getContentBeforeToolCall(response: String): String {
        val index = response.indexOf(TOOL_CALL_PREFIX)
        return if (index >= 0) response.substring(0, index).trim() else response
    }

    suspend fun executeTool(toolCall: ToolCall): String {
        return when (toolCall) {
            is ToolCall.WebSearch -> executeWebSearch(toolCall.query)
            is ToolCall.GetDateTime -> getCurrentDateTime()
        }
    }

    private suspend fun executeWebSearch(query: String): String =
        withContext(Dispatchers.IO) {
            val service =
                searxngService
                    ?: return@withContext "Search not configured. Please set SearXNG port in settings."

            try {
                val response = service.search(query)

                if (!response.isSuccessful) {
                    return@withContext "Search failed: ${response.code()} - ${response.message()}"
                }

                val searchResponse = response.body() ?: return@withContext "Empty search response"
                val formattedResults = SearxngClient.formatSearchResults(searchResponse)

                formattedResults
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("ChatRepository", "Search error: ${e.message}", e)
                "Search error: ${e.message}"
            }
        }

    private fun getCurrentDateTime(): String {
        val now = java.time.LocalDateTime.now()
        val formatter =
            java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")
        return "Current date and time: ${now.format(formatter)}"
    }
}

sealed class ToolCall {
    data class WebSearch(val query: String) : ToolCall()

    data object GetDateTime : ToolCall()

    val displayName: String
        get() =
            when (this) {
                is WebSearch -> "Searching..."
                is GetDateTime -> "Checking time..."
            }
}
