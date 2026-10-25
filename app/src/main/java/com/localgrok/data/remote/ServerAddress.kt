package com.localgrok.data.remote

import okhttp3.HttpUrl

object ServerAddress {
    fun baseUrl(host: String, port: Int): String {
        require(host.isNotBlank()) { "Enter a server IP address or hostname." }
        require(port in 1..65535) { "Ports must be between 1 and 65535." }
        return try {
            HttpUrl.Builder().scheme("http").host(host.trim()).port(port).build().toString()
        } catch (error: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Enter a server IP address or hostname without http://, a port, or a path.",
                error,
            )
        }
    }
}
