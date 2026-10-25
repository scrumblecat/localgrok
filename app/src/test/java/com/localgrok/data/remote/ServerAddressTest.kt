package com.localgrok.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ServerAddressTest {
    @Test
    fun supportsAddressesHostnamesAndIpv6() {
        assertEquals("http://192.168.1.50:11434/", ServerAddress.baseUrl(" 192.168.1.50 ", 11434))
        assertEquals("http://ollama.local:11434/", ServerAddress.baseUrl("ollama.local", 11434))
        assertEquals("http://[::1]:11434/", ServerAddress.baseUrl("::1", 11434))
    }

    @Test
    fun rejectsInvalidHostsBeforeNetworkSetup() {
        for (host in
            listOf(
                "",
                " ",
                "http://example.com",
                "host/path",
                "user@host",
                "host:11434",
                "two words",
            )) {
            assertThrows(IllegalArgumentException::class.java) {
                ServerAddress.baseUrl(host, 11434)
            }
        }
    }

    @Test
    fun rejectsInvalidPortsRatherThanCrashingLater() {
        for (port in listOf(-1, 0, 65536, Int.MAX_VALUE)) {
            assertThrows(IllegalArgumentException::class.java) {
                ServerAddress.baseUrl("localhost", port)
            }
        }
    }
}
