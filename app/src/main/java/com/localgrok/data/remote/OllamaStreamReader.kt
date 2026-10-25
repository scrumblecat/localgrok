package com.localgrok.data.remote

import com.localgrok.data.remote.model.OllamaChatResponse
import java.io.BufferedReader
import java.io.EOFException
import java.io.IOException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody

object OllamaStreamReader {
    suspend fun read(
        body: ResponseBody,
        json: Json,
        thinkEnabled: Boolean,
        onThinkingToken: suspend (String) -> Unit,
        onToken: suspend (String) -> Unit,
    ) = coroutineScope {
        val closeOnCancellation =
            launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    awaitCancellation()
                } finally {
                    body.close()
                }
            }
        try {
            body.use { response ->
                response.byteStream().bufferedReader().use { reader ->
                    read(reader, json, thinkEnabled, onThinkingToken, onToken)
                }
            }
        } catch (error: IOException) {
            currentCoroutineContext().ensureActive()
            throw error
        } finally {
            closeOnCancellation.cancel()
        }
    }

    suspend fun read(
        reader: BufferedReader,
        json: Json,
        thinkEnabled: Boolean,
        onThinkingToken: suspend (String) -> Unit,
        onToken: suspend (String) -> Unit,
    ) {
        while (true) {
            currentCoroutineContext().ensureActive()
            val line =
                runInterruptible { reader.readLine() }
                    ?: throw EOFException(
                        "The server disconnected before finishing its response. Please try again."
                    )
            if (line.isBlank()) continue
            val response =
                try {
                    json.decodeFromString<OllamaChatResponse>(line)
                } catch (error: SerializationException) {
                    throw IOException("The server returned an invalid chat response.", error)
                }
            response.error?.let { throw IOException(it) }
            if (thinkEnabled) {
                response.message?.thinking?.takeIf { it.isNotEmpty() }?.let { onThinkingToken(it) }
            }
            response.message?.content?.takeIf { it.isNotEmpty() }?.let { onToken(it) }
            if (response.done) return
        }
    }
}
