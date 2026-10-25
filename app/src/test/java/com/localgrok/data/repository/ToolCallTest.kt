package com.localgrok.data.repository

import com.localgrok.data.local.dao.ChatDao
import com.localgrok.data.local.dao.MessageDao
import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ToolCallTest {
    private fun <T : Any> unusedDao(type: Class<T>): T =
        type.cast(
            Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { _, _, _ ->
                error("Tool parsing must not access the database")
            }
        )

    private val repository =
        ChatRepository(unusedDao(ChatDao::class.java), unusedDao(MessageDao::class.java))

    @Test
    fun readsQueriesContainingBracesAndNestedMetadata() {
        val response =
            """<tool_call>{"name":"web_search","query":"what does } mean?","extra":{"nested":true}}</tool_call>"""
        assertEquals(ToolCall.WebSearch("what does } mean?"), repository.detectToolCall(response))
    }

    @Test
    fun stripsCompleteAndIncompleteToolMarkup() {
        val complete =
            """Before<tool_call>{"name":"web_search","extra":{"nested":true}}</tool_call>After"""
        assertEquals("BeforeAfter", repository.cleanToolArtifacts(complete))
        assertEquals(
            "Before",
            repository.cleanToolArtifacts("Before<tool_call>{\"name\":\"web_search\""),
        )
        assertEquals(
            "Answer",
            repository.cleanToolArtifacts("Answer<tool_result>Internal result</tool_result>"),
        )
    }

    @Test
    fun doesNotExecuteUnknownOrIncompleteTools() {
        assertNull(repository.detectToolCall("""<tool_call>{"name":"delete_files"}</tool_call>"""))
        assertNull(repository.detectToolCall("<tool_call>{\"name\":\"web_search\""))
    }

    @Test
    fun preservesTextBeforeAToolCallWithNestedData() {
        val response =
            """Let me check.<tool_call>{"name":"web_search","query":"news","extra":{}}</tool_call>"""
        assertEquals("Let me check.", repository.getContentBeforeToolCall(response))
    }
}
