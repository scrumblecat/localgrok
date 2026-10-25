package com.localgrok.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.localgrok.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    suspend fun getMessagesForChatSync(chatId: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update suspend fun updateMessage(message: MessageEntity)

    @Delete suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Long)

    @Query(
        "UPDATE messages SET content = :content, isStreaming = :isStreaming WHERE id = :messageId"
    )
    suspend fun updateMessageContent(messageId: Long, content: String, isStreaming: Boolean = false)

    @Query(
        "UPDATE messages SET content = :content, isStreaming = :isStreaming, isThinking = :isThinking, toolUsed = :toolUsed, toolDisplayName = :toolDisplayName WHERE id = :messageId"
    )
    suspend fun updateMessageState(
        messageId: Long,
        content: String,
        isStreaming: Boolean,
        isThinking: Boolean,
        toolUsed: Boolean,
        toolDisplayName: String,
    )

    @Query(
        "UPDATE messages SET content = :content, isStreaming = :isStreaming, isThinking = :isThinking, reasoningContent = :reasoningContent, toolUsed = :toolUsed, toolDisplayName = :toolDisplayName WHERE id = :messageId"
    )
    suspend fun updateMessageStateWithReasoning(
        messageId: Long,
        content: String,
        isStreaming: Boolean,
        isThinking: Boolean,
        reasoningContent: String,
        toolUsed: Boolean,
        toolDisplayName: String,
    )

    @Query("UPDATE messages SET reasoningContent = :reasoningContent WHERE id = :messageId")
    suspend fun updateReasoningContent(messageId: Long, reasoningContent: String)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastMessageForChat(chatId: Long): MessageEntity?
}
