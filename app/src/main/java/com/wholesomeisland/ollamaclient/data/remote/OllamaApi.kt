package com.wholesomeisland.ollamaclient.data.remote

import com.squareup.moshi.Json
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

// --- Common ---
data class OllamaModelTag(val name: String?)
data class OllamaTagsResponse(val models: List<OllamaModelTag>?)
data class OllamaVersionResponse(val version: String?)

// --- Tool Calls ---
data class ToolCall(
    val id: String? = null,
    val type: String? = null,
    val function: ToolFunction? = null
)

data class ToolFunction(
    val name: String?,
    val arguments: Map<String, Any>? = null
)

// --- Chat Endpoint ---
data class ChatMessage(
    val role: String?,
    val content: String?,
    @Json(name = "reasoning_content") val reasoningContent: String? = null,
    val images: List<String>? = null,
    @Json(name = "tool_calls") val toolCalls: List<ToolCall>? = null,
    @Json(name = "tool_call_id") val toolCallId: String? = null
)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val options: Map<String, Any>? = null,
    val tools: List<Any>? = null,
    @Json(name = "web_search") val webSearch: Boolean? = null
)

data class ChatResponse(
    val model: String?,
    val created_at: String? = null,
    val message: ChatMessage? = null,
    val done: Boolean?,
    val error: String? = null
)

// --- Generate Endpoint ---
data class GenerateRequest(
    val model: String,
    val prompt: String,
    val system: String? = null,
    val template: String? = null,
    val context: List<Int>? = null,
    val stream: Boolean = false,
    val options: Map<String, Any>? = null,
    @Json(name = "web_search") val webSearch: Boolean? = null
)

data class GenerateResponse(
    val model: String?,
    val created_at: String? = null,
    val response: String? = null,
    @Json(name = "reasoning_content") val reasoningContent: String? = null,
    val done: Boolean?,
    val context: List<Int>? = null,
    val error: String? = null
)

// --- Running Models Endpoint ---
data class RunningModel(
    val name: String?,
    val size: Long?,
    val details: Map<String, Any>? = null
)

data class RunningModelsResponse(val models: List<RunningModel>?)

interface OllamaApi {
    @GET("/")
    suspend fun healthCheck(): Response<ResponseBody>

    @GET("api/version")
    suspend fun getVersion(): Response<OllamaVersionResponse>

    @GET("api/tags")
    suspend fun getModels(): Response<OllamaTagsResponse>

    @GET("api/ps")
    suspend fun getRunningModels(): Response<RunningModelsResponse>

    @POST("api/chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>

    @Streaming
    @POST("api/chat")
    suspend fun chatStream(@Body request: ChatRequest): Response<ResponseBody>

    @POST("api/generate")
    suspend fun generate(@Body request: GenerateRequest): Response<GenerateResponse>

    @Streaming
    @POST("api/generate")
    suspend fun generateStream(@Body request: GenerateRequest): Response<ResponseBody>
}
