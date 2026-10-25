package com.localgrok.data.remote

import com.localgrok.data.remote.model.OllamaChatRequest
import com.localgrok.data.remote.model.OllamaModelsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OllamaApiService {

    @Streaming
    @POST("api/chat")
    suspend fun chatStream(@Body request: OllamaChatRequest): Response<ResponseBody>

    @GET("api/tags") suspend fun listModels(): Response<OllamaModelsResponse>

    @GET("/") suspend fun healthCheck(): Response<ResponseBody>
}
