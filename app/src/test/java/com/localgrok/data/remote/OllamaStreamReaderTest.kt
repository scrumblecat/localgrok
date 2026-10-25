package com.localgrok.data.remote

import java.io.EOFException
import java.io.IOException
import java.io.StringReader
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Pipe
import okio.buffer
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue

class OllamaStreamReaderTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun cancellationClosesAResponseWhileWaitingForTheNextToken() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val closed = AtomicBoolean(false)
        val pipe = Pipe(1024L)
        val source = object : ForwardingSource(pipe.source) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                started.complete(Unit)
                return super.read(sink, byteCount)
            }
            override fun close() {
                closed.set(true)
                super.close()
            }
        }.buffer()
        val body = object : ResponseBody() {
            override fun contentType(): MediaType? = null
            override fun contentLength(): Long = -1
            override fun source(): BufferedSource = source
        }
        withTimeout(3000) {
            val job = launch(Dispatchers.IO) {
                OllamaStreamReader.read(body, json, false, {}, {})
            }
            started.await()
            job.cancelAndJoin()
        }
        assertTrue(closed.get())
    }

    @Test
    fun streamsContentAndThinkingUntilDone() = runBlocking {
        val input =
            """
                {"message":{"role":"assistant","thinking":"Checking"},"done":false}
                {"message":{"role":"assistant","content":"Hello "},"done":false}
                {"message":{"role":"assistant","content":"there"},"done":true,"eval_count":2}
                {"message":{"role":"assistant","content":"Must not be read"},"done":true}
            """
                .trimIndent()
        val content = StringBuilder()
        val thoughts = StringBuilder()
        OllamaStreamReader.read(
            StringReader(input).buffered(),
            json,
            true,
            {
                thoughts.append(it)
                Unit
            },
            {
                content.append(it)
                Unit
            },
        )
        assertEquals("Hello there", content.toString())
        assertEquals("Checking", thoughts.toString())
    }

    @Test
    fun reportsTruncatedStreamsWithoutLosingReceivedTokens() {
        val tokens = mutableListOf<String>()
        assertThrows(EOFException::class.java) {
            runBlocking {
                OllamaStreamReader.read(
                    StringReader(
                            """{"message":{"role":"assistant","content":"Partial"},"done":false}"""
                        )
                        .buffered(),
                    json,
                    false,
                    {},
                    {
                        tokens.add(it)
                        Unit
                    },
                )
            }
        }
        assertEquals(listOf("Partial"), tokens)
    }

    @Test
    fun surfacesServerErrorsAndMalformedData() {
        for (input in listOf("""{"error":"Model not found"}""", "not json")) {
            assertThrows(IOException::class.java) {
                runBlocking {
                    OllamaStreamReader.read(StringReader(input).buffered(), json, false, {}, {})
                }
            }
        }
    }

    @Test
    fun doesNotSwallowErrorsRaisedByTokenCallbacks() {
        val error =
            assertThrows(IllegalStateException::class.java) {
                runBlocking {
                    OllamaStreamReader.read(
                        StringReader(
                                """{"message":{"role":"assistant","content":"Hi"},"done":true}"""
                            )
                            .buffered(),
                        json,
                        false,
                        {},
                        { throw IllegalStateException("Could not save message") },
                    )
                }
            }
        assertEquals("Could not save message", error.message)
    }

    @Test
    fun ignoresThinkingWhenDisabled() = runBlocking {
        var thoughts = 0
        OllamaStreamReader.read(
            StringReader(
                    """{"message":{"role":"assistant","thinking":"hidden","content":"Hi"},"done":true}"""
                )
                .buffered(),
            json,
            false,
            { thoughts++ },
            {},
        )
        assertEquals(0, thoughts)
    }
}
