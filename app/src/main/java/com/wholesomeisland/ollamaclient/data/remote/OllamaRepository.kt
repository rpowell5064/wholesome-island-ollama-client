package com.wholesomeisland.ollamaclient.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import retrofit2.Response

class OllamaRepository(private val api: OllamaApi) {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val chatResponseAdapter = moshi.adapter(ChatResponse::class.java)
    private val generateResponseAdapter = moshi.adapter(GenerateResponse::class.java)

    suspend fun healthCheck(): Result<Boolean> = runCatching {
        val response = api.healthCheck()
        if (response.isSuccessful) {
            true
        } else {
            throw Exception("Server returned ${response.code()}")
        }
    }

    suspend fun getVersion(): Result<String> = runCatching {
        val response = api.getVersion()
        if (response.isSuccessful) {
            response.body()?.version ?: "unknown"
        } else {
            throw Exception("Version check failed: ${response.code()}")
        }
    }

    suspend fun getModels(): Result<List<String>> = runCatching {
        val response = api.getModels()
        if (response.isSuccessful) {
            response.body()?.models?.mapNotNull { it.name } ?: emptyList()
        } else {
            throw Exception("Failed to load models: ${response.code()}")
        }
    }

    suspend fun sendChat(
        model: String,
        history: List<ChatMessage>,
        webSearch: Boolean? = null,
        stream: Boolean = false,
        supportsTools: Boolean = true,
        tools: List<Any>? = null
    ): Response<ChatResponse> {
        val request = ChatRequest(
            model = model,
            messages = history,
            stream = stream,
            tools = tools,
            webSearch = if (webSearch == true && !supportsTools) true else null
        )
        return api.chat(request)
    }

    suspend fun sendChatStream(
        model: String,
        history: List<ChatMessage>,
        webSearch: Boolean? = null,
        supportsTools: Boolean = true,
        tools: List<Any>? = null
    ): Response<ResponseBody> {
        val request = ChatRequest(
            model = model,
            messages = history,
            stream = true,
            tools = tools,
            webSearch = if (webSearch == true && !supportsTools) true else null
        )
        return api.chatStream(request)
    }

    fun parseChatStream(responseBody: ResponseBody): Flow<ChatMessage> = flow {
        responseBody.source().use { source ->
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.isBlank()) continue
                
                try {
                    val res = chatResponseAdapter.fromJson(line)
                    val msg = res?.message
                    if (msg != null) {
                        if (!msg.content.isNullOrEmpty() || !msg.reasoningContent.isNullOrBlank() || msg.toolCalls != null) {
                            emit(msg)
                        }
                    }
                } catch (e: Exception) {
                    if (!line.contains("\"done\":true")) {
                        continue 
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun generate(
        model: String,
        prompt: String,
        system: String? = null,
        stream: Boolean = false
    ): Response<GenerateResponse> {
        return api.generate(
            GenerateRequest(
                model = model,
                prompt = prompt,
                system = system,
                stream = stream
            )
        )
    }

    suspend fun generateStream(
        model: String,
        prompt: String,
        system: String? = null
    ): Response<ResponseBody> {
        val request = GenerateRequest(
            model = model,
            prompt = prompt,
            system = system,
            stream = true
        )
        return api.generateStream(request)
    }

    fun parseGenerateStream(responseBody: ResponseBody): Flow<String> = flow {
        responseBody.source().use { source ->
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.isBlank()) continue
                try {
                    val res = generateResponseAdapter.fromJson(line)
                    val text = res?.response
                    val reasoning = res?.reasoningContent
                    
                    if (!text.isNullOrEmpty()) emit(text)
                    else if (!reasoning.isNullOrEmpty()) emit(reasoning)
                    
                } catch (e: Exception) {
                    if (!line.contains("\"done\":true")) {
                        continue
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
