package com.localgrok.data.remote

import com.localgrok.data.remote.model.OllamaChatRequest
import com.localgrok.data.remote.model.OllamaMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class OllamaRequestTest {
    @Test
    fun doesNotSendThinkingToModelsThatDoNotSupportIt() {
        val json = OllamaClient.getJson()
        val request = OllamaChatRequest("gemma3:1b-it-qat", listOf(OllamaMessage("user", "Hello")))
        val encoded = json.parseToJsonElement(json.encodeToString(request)).jsonObject
        assertFalse("think" in encoded)
        assertEquals("true", encoded.getValue("stream").jsonPrimitive.content)
    }

    @Test
    fun sendsThinkingWhenRequested() {
        val json = OllamaClient.getJson()
        val request =
            OllamaChatRequest("qwen3:1.7b", listOf(OllamaMessage("user", "Hello")), think = true)
        val encoded = json.parseToJsonElement(json.encodeToString(request)).jsonObject
        assertEquals("true", encoded.getValue("think").jsonPrimitive.content)
    }
}
