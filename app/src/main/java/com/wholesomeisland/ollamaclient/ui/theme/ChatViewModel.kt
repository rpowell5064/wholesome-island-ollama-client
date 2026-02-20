package com.wholesomeisland.ollamaclient.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.wholesomeisland.ollamaclient.data.remote.ChatMessage
import com.wholesomeisland.ollamaclient.data.remote.OllamaRepository
import com.wholesomeisland.ollamaclient.data.remote.OllamaServiceFactory
import com.wholesomeisland.ollamaclient.data.remote.ToolCall
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/**
 * Data representation of a message in the UI layer.
 */
data class ChatUiMessage(
    val id: Long,
    val role: String,
    val text: String,
    val reasoning: String? = null,
    val imagesBase64: List<String> = emptyList(),
    val showSearchSuggestion: Boolean = false,
    val toolCalls: List<ToolCall>? = null
)

/**
 * Quick action buttons displayed above the input field.
 */
data class QuickAction(
    val label: String,
    val prompt: String,
    val icon: String
)

/**
 * State representing the entire Chat Screen UI.
 */
data class ChatUiState(
    val serverUrl: String = "",
    val apiKey: String = "",
    val searchEngines: List<SearchEngineConfig> = listOf(ChatConstants.DEFAULT_DDG_CONFIG),
    val selectedSearchEngineId: String = ChatConstants.DEFAULT_SEARCH_ENGINE_ID,
    val availableModels: List<String> = emptyList(),
    val selectedModel: String? = null,
    val messages: List<ChatUiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val progressMessage: String = ChatConstants.IDLE_PROGRESS,
    val isSearching: Boolean = false,
    val searchQuery: String? = null,
    val isServerHealthy: Boolean? = null,
    val isWebSearchEnabled: Boolean = true,
    val isStreamingEnabled: Boolean = true,
    val verbosity: Float = 0.5f,
    val infoMessage: String? = null,
    val error: String? = null,
    val attachedImagesBase64: List<String> = emptyList(),
    val quickActions: List<QuickAction> = listOf(
        QuickAction("Summarize", "Summarize our conversation.", "summarize"),
        QuickAction("Tasks", "Extract action items.", "list"),
        QuickAction("Simplify", "Explain simply.", "help")
    )
)

/**
 * Internal exception used to break the streaming loop when a tool call is detected.
 */
private class ToolCallHandledException(
    val query: String,
    val id: String,
    val responseText: String,
    val reasoningText: String,
    val toolCalls: List<ToolCall>?
) : Exception()

/**
 * ViewModel for the Wholesome Island chat interface.
 * Handles efficient local LLM integration and real-time web search.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("wholesome_island_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val searchEngineListAdapter = moshi.adapter<List<SearchEngineConfig>>(
        Types.newParameterizedType(List::class.java, SearchEngineConfig::class.java)
    )
    
    private var repository: OllamaRepository? = null
    private var errorTimerJob: Job? = null
    private var infoTimerJob: Job? = null
    private var activeChatJob: Job? = null
    private val idGenerator = AtomicLong(System.currentTimeMillis())
    private var modelSupportsTools: Boolean = true
    
    private val searchClient = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _uiState = MutableStateFlow(ChatUiState(
        serverUrl = prefs.getString("server_url", "") ?: "",
        apiKey = prefs.getString("api_key", "") ?: "",
        searchEngines = loadSearchEngines(),
        selectedSearchEngineId = prefs.getString("selected_search_engine_id", ChatConstants.DEFAULT_SEARCH_ENGINE_ID) ?: ChatConstants.DEFAULT_SEARCH_ENGINE_ID,
        selectedModel = prefs.getString("selected_model", null),
        isWebSearchEnabled = prefs.getBoolean("web_search_enabled", true),
        isStreamingEnabled = prefs.getBoolean("streaming_enabled", true),
        verbosity = prefs.getFloat("verbosity", 0.5f)
    ))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val searchRegex = Regex("""web_search\s*\(\s*(?:query\s*=\s*)?["'](.+?)["']\s*\)|\[SEARCH:\s*["'](.+?)["']\]|(?:tool_code|call|use|query|search|lookup)\s+web_search\s+query:?\s*["'](.+?)["']""", RegexOption.IGNORE_CASE)

    init {
        updateRepository(_uiState.value.serverUrl, _uiState.value.apiKey)
        showStartupNotification()
    }

    private fun loadSearchEngines(): List<SearchEngineConfig> {
        val json = prefs.getString("search_engines_json", null)
        return if (json != null) {
            try {
                searchEngineListAdapter.fromJson(json) ?: listOf(ChatConstants.DEFAULT_DDG_CONFIG)
            } catch (e: Exception) {
                listOf(ChatConstants.DEFAULT_DDG_CONFIG)
            }
        } else {
            listOf(ChatConstants.DEFAULT_DDG_CONFIG)
        }
    }

    private fun saveSearchEngines(engines: List<SearchEngineConfig>) {
        val json = searchEngineListAdapter.toJson(engines)
        prefs.edit().putString("search_engines_json", json).apply()
    }

    private fun showStartupNotification() {
        val state = _uiState.value
        when {
            state.serverUrl.isBlank() || !state.serverUrl.startsWith("http") -> {
                _uiState.update { it.copy(infoMessage = "Welcome! Please enter your Ollama server address in settings.") }
            }
            state.selectedModel == null -> {
                _uiState.update { it.copy(infoMessage = "Server connected! Now select a model in settings to start.") }
            }
            else -> {
                _uiState.update { it.copy(infoMessage = "Ready to chat.") }
                infoTimerJob?.cancel()
                infoTimerJob = viewModelScope.launch {
                    delay(5000)
                    _uiState.update { it.copy(infoMessage = null) }
                }
            }
        }
    }

    private fun updateRepository(url: String, key: String) {
        if (url.isBlank() || !url.startsWith("http")) {
            repository = null
            _uiState.update { it.copy(isServerHealthy = null) }
            return
        }
        
        try {
            repository = OllamaRepository(OllamaServiceFactory.create(url, key))
            checkHealthAndLoadModels()
        } catch (e: Exception) {
            repository = null
            _uiState.update { it.copy(isServerHealthy = false, error = "Invalid server URL format.") }
        }
    }

    fun setConnectionDetails(url: String, key: String) {
        prefs.edit().putString("server_url", url).putString("api_key", key).apply()
        _uiState.update { it.copy(serverUrl = url, apiKey = key, infoMessage = null) }
        updateRepository(url, key)
    }

    fun addSearchEngine(name: String, type: String, url: String, apiKey: String, authHeader: String) {
        val newEngine = SearchEngineConfig(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            url = url,
            apiKey = apiKey,
            authHeader = authHeader
        )
        val newList = _uiState.value.searchEngines + newEngine
        _uiState.update { it.copy(searchEngines = newList) }
        saveSearchEngines(newList)
    }

    fun removeSearchEngine(id: String) {
        val engine = _uiState.value.searchEngines.find { it.id == id }
        if (engine?.isDeletable == false) return

        val newList = _uiState.value.searchEngines.filter { it.id != id }
        var newSelectedId = _uiState.value.selectedSearchEngineId
        if (newSelectedId == id) {
            newSelectedId = ChatConstants.DEFAULT_SEARCH_ENGINE_ID
        }
        
        _uiState.update { it.copy(searchEngines = newList, selectedSearchEngineId = newSelectedId) }
        saveSearchEngines(newList)
        prefs.edit().putString("selected_search_engine_id", newSelectedId).apply()
    }

    fun setSelectedSearchEngine(id: String) {
        prefs.edit().putString("selected_search_engine_id", id).apply()
        _uiState.update { it.copy(selectedSearchEngineId = id) }
    }

    fun toggleWebSearch(enabled: Boolean) {
        prefs.edit().putBoolean("web_search_enabled", enabled).apply()
        _uiState.update { it.copy(isWebSearchEnabled = enabled) }
    }

    fun toggleStreaming(enabled: Boolean) {
        prefs.edit().putBoolean("streaming_enabled", enabled).apply()
        _uiState.update { it.copy(isStreamingEnabled = enabled) }
    }

    fun setVerbosity(value: Float) {
        prefs.edit().putFloat("verbosity", value).apply()
        _uiState.update { it.copy(verbosity = value) }
    }

    private fun checkHealthAndLoadModels() {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isServerHealthy = null) }
            val healthResult = repo.healthCheck()
            if (healthResult.isSuccess) {
                _uiState.update { it.copy(isServerHealthy = true) }
                loadModels()
            } else {
                _uiState.update { it.copy(isServerHealthy = false, isLoading = false, error = "Server unreachable. Check your URL and API Key.") }
            }
        }
    }

    fun loadModels() {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.getModels()
            _uiState.update {
                result.fold(
                    onSuccess = { models ->
                        val currentSelected = prefs.getString("selected_model", null)
                        it.copy(availableModels = models, selectedModel = currentSelected, isLoading = false)
                    },
                    onFailure = { e ->
                        setError("Failed to load models: ${e.message}")
                        it.copy(isLoading = false)
                    }
                )
            }
        }
    }

    fun attachImages(base64Images: List<String>) {
        _uiState.update { it.copy(attachedImagesBase64 = base64Images) }
    }

    fun clearAttachedImages() {
        _uiState.update { it.copy(attachedImagesBase64 = emptyList()) }
    }

    fun selectModel(model: String) {
        prefs.edit().putString("selected_model", model).apply()
        _uiState.update { it.copy(selectedModel = model, infoMessage = null) }
        modelSupportsTools = true 
    }

    fun clearMessages() {
        _uiState.update { it.copy(messages = emptyList(), error = null) }
    }

    fun deleteMessage(id: Long) {
        _uiState.update { it.copy(messages = it.messages.filter { it.id != id }) }
    }

    fun dismissError() {
        errorTimerJob?.cancel()
        _uiState.update { it.copy(error = null) }
    }

    fun dismissInfo() {
        infoTimerJob?.cancel()
        _uiState.update { it.copy(infoMessage = null) }
    }

    private fun setError(message: String?) {
        errorTimerJob?.cancel()
        val displayMessage = if (message.isNullOrBlank() || message == "null") "An unknown error occurred" else message
        _uiState.update { it.copy(error = displayMessage) }
        errorTimerJob = viewModelScope.launch {
            delay(10000)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun cancelRequest() {
        activeChatJob?.cancel()
        activeChatJob = null
        _uiState.update { it.copy(isLoading = false, isSearching = false, searchQuery = null, progressMessage = ChatConstants.IDLE_PROGRESS) }
    }

    private suspend fun performWebSearch(query: String): String = withContext(Dispatchers.IO + NonCancellable) {
        val state = _uiState.value
        val engine = state.searchEngines.find { it.id == state.selectedSearchEngineId } ?: ChatConstants.DEFAULT_DDG_CONFIG
        
        withContext(Dispatchers.Main) { 
            _uiState.update { it.copy(isSearching = true, isLoading = true, progressMessage = "Searching ${engine.name}...") } 
        }

        try {
            when(engine.type) {
                "DUCKDUCKGO" -> {
                    val url = "https://html.duckduckgo.com/html/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
                    val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
                    searchClient.newCall(request).execute().use { response ->
                        val html = response.body?.string() ?: ""
                        val doc = Jsoup.parse(html)
                        val results = doc.select(".result").take(5).map { 
                            "**${it.select(".result__a").text()}**\n${it.select(".result__snippet").text()}" 
                        }
                        if (results.isEmpty()) "No results found." else results.joinToString("\n\n")
                    }
                }
                "API_GET" -> {
                    val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                    val finalUrl = if (engine.url.contains("{query}")) {
                        engine.url.replace("{query}", encodedQuery)
                    } else {
                        val separator = if (engine.url.contains("?")) "&" else "?"
                        "${engine.url}${separator}q=$encodedQuery"
                    }
                    
                    val requestBuilder = Request.Builder().url(finalUrl)
                    if (engine.apiKey.isNotBlank()) {
                        requestBuilder.addHeader(engine.authHeader, if (engine.authHeader.lowercase() == "authorization") "Bearer ${engine.apiKey}" else engine.apiKey)
                    }
                    
                    searchClient.newCall(requestBuilder.build()).execute().use { response ->
                        val body = response.body?.string() ?: ""
                        if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                            "Search Results (JSON Raw):\n$body"
                        } else {
                            "Search Results:\n$body"
                        }
                    }
                }
                "API_POST" -> {
                    val mediaType = "application/json".toMediaType()
                    val jsonBody = if (engine.url.contains("serper.dev")) {
                        "{\"q\":\"$query\"}"
                    } else {
                        "{\"query\":\"$query\"}"
                    }
                    
                    val requestBuilder = Request.Builder()
                        .url(engine.url)
                        .post(jsonBody.toRequestBody(mediaType))
                    
                    if (engine.apiKey.isNotBlank()) {
                        requestBuilder.addHeader(engine.authHeader, if (engine.authHeader.lowercase() == "authorization") "Bearer ${engine.apiKey}" else engine.apiKey)
                    }
                    if (engine.url.contains("serper.dev") && engine.authHeader.lowercase() != "x-api-key") {
                        requestBuilder.addHeader("X-API-KEY", engine.apiKey)
                    }

                    searchClient.newCall(requestBuilder.build()).execute().use { response ->
                        val body = response.body?.string() ?: ""
                        "Search Results (POST):\n$body"
                    }
                }
                else -> "Error: Unknown search engine type."
            }
        } catch (e: Exception) {
            "Search error: ${e.message}"
        }
    }

    fun performQuickAction(action: QuickAction) {
        sendMessage(action.prompt)
    }

    private fun getWebSearchTool(): List<Map<String, Any>> = listOf(
        mapOf(
            "type" to "function",
            "function" to mapOf(
                "name" to "web_search",
                "description" to "Search the web for up-to-date information.",
                "parameters" to mapOf(
                    "type" to "object",
                    "properties" to mapOf("query" to mapOf("type" to "string")),
                    "required" to listOf("query")
                )
            )
        )
    )

    fun sendMessage(text: String) {
        val state = _uiState.value
        val model = state.selectedModel ?: run {
            setError("No model selected. Please select one in settings.")
            return
        }
        val newUserMessage = ChatUiMessage(idGenerator.incrementAndGet(), "user", text, imagesBase64 = state.attachedImagesBase64)
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val systemContent = buildString {
            append("TODAY'S DATE: $currentDate\n\n")
            append(ChatConstants.BASE_SYSTEM_PROMPT)
            if (state.isWebSearchEnabled) {
                append("\n\n")
                append(ChatConstants.WEB_SEARCH_INSTRUCTION)
                if (modelSupportsTools) append(ChatConstants.TOOL_MODE_PROMPT)
                else append(ChatConstants.NO_TOOL_MODE_PROMPT)
            }
        }

        val history = mutableListOf<ChatMessage>()
        history.add(ChatMessage(role = "system", content = systemContent))
        val imageCutoff = state.messages.size - 2
        history.addAll(state.messages.mapIndexed { index, msg -> 
            ChatMessage(role = msg.role, content = msg.text, reasoningContent = msg.reasoning, images = if (index >= imageCutoff) msg.imagesBase64.ifEmpty { null } else null, toolCalls = msg.toolCalls)
        })
        history.add(ChatMessage("user", text, null, state.attachedImagesBase64.ifEmpty { null }, null, null))

        _uiState.update { it.copy(messages = it.messages + newUserMessage, isLoading = true, progressMessage = ChatConstants.IDLE_PROGRESS, error = null, attachedImagesBase64 = emptyList()) }
        performSendMessage(model, history, state.isWebSearchEnabled, state.isStreamingEnabled)
    }

    private fun performSendMessage(model: String, history: List<ChatMessage>, webSearch: Boolean, stream: Boolean) {
        activeChatJob?.cancel()
        activeChatJob = viewModelScope.launch {
            val repo = repository ?: return@launch
            var currentHistory = history
            var shouldProcess = true

            while (shouldProcess && isActive) {
                shouldProcess = false
                val tools = if (webSearch && modelSupportsTools) getWebSearchTool() else null
                
                try {
                    if (stream) {
                        var responseText = ""
                        var reasoningText = ""
                        var capturedToolCalls: List<ToolCall>? = null
                        val msgId = idGenerator.incrementAndGet()
                        _uiState.update { it.copy(messages = it.messages + ChatUiMessage(msgId, "assistant", "")) }

                        val response = repo.sendChatStream(model, currentHistory, webSearch, modelSupportsTools, tools)
                        if (!response.isSuccessful) {
                            val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                            if (err.contains("not support tools")) {
                                modelSupportsTools = false
                                _uiState.update { it.copy(messages = it.messages.filter { it.id != msgId }) }
                                shouldProcess = true
                                continue
                            }
                            setError(err)
                            _uiState.update { it.copy(isLoading = false) }
                            return@launch
                        }

                        try {
                            repo.parseChatStream(response.body()!!)
                                .catch { e -> setError(e.message); _uiState.update { it.copy(isLoading = false) } }
                                .collect { chunk ->
                                    responseText += (chunk.content ?: "")
                                    reasoningText += (chunk.reasoningContent ?: "")
                                    if (chunk.toolCalls != null) capturedToolCalls = chunk.toolCalls
                                    
                                    val textMatch = searchRegex.find(responseText) ?: if (reasoningText.isNotEmpty()) searchRegex.find(reasoningText) else null
                                    
                                    if (webSearch && ((capturedToolCalls != null && capturedToolCalls!!.isNotEmpty()) || textMatch != null)) {
                                        val query = if (capturedToolCalls?.isNotEmpty() == true) {
                                            capturedToolCalls?.firstOrNull()?.function?.arguments?.get("query")?.toString()
                                        } else {
                                            textMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }
                                        } ?: ""
                                        
                                        if (query.isNotEmpty()) {
                                            throw ToolCallHandledException(query, capturedToolCalls?.firstOrNull()?.id ?: "manual_id", responseText, reasoningText, capturedToolCalls)
                                        }
                                    }

                                    _uiState.update { current ->
                                        current.copy(messages = current.messages.map { 
                                            if (it.id == msgId) it.copy(text = responseText, reasoning = reasoningText.ifEmpty { null }, toolCalls = capturedToolCalls) else it 
                                        })
                                    }
                                }
                            _uiState.update { it.copy(isLoading = false) }
                        } catch (e: ToolCallHandledException) {
                            _uiState.update { it.copy(messages = it.messages.filter { it.id != msgId }) }
                            val searchResult = performWebSearch(e.query)
                            val assistantTurn = ChatMessage(role = "assistant", content = if (modelSupportsTools) null else e.responseText, reasoningContent = e.reasoningText, toolCalls = e.toolCalls)
                            val toolTurn = if (modelSupportsTools) {
                                ChatMessage(role = "tool", content = "SEARCH_RESULTS:\n$searchResult", toolCallId = e.id)
                            } else {
                                ChatMessage(role = "user", content = "SEARCH_RESULTS:\n$searchResult\n\nPlease answer based on these results.")
                            }
                            currentHistory = currentHistory + assistantTurn + toolTurn
                            _uiState.update { it.copy(isLoading = true, isSearching = false, progressMessage = ChatConstants.SYNTHESIZING_PROGRESS) }
                            shouldProcess = true
                            continue
                        }
                    } else {
                        val response = repo.sendChat(model, currentHistory, webSearch, false, modelSupportsTools, tools)
                        if (!response.isSuccessful) {
                            setError(response.errorBody()?.string() ?: "HTTP ${response.code()}")
                            _uiState.update { it.copy(isLoading = false) }
                            return@launch
                        }
                        val assistantMsg = response.body()?.message ?: return@launch
                        val text = assistantMsg.content ?: ""
                        val reasoning = assistantMsg.reasoningContent ?: ""
                        val textMatch = searchRegex.find(text) ?: if (reasoning.isNotEmpty()) searchRegex.find(reasoning) else null
                        val toolQuery = if (assistantMsg.toolCalls?.isNotEmpty() == true) {
                            assistantMsg.toolCalls?.firstOrNull()?.function?.arguments?.get("query")?.toString()
                        } else {
                            textMatch?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }
                        }

                        if (webSearch && !toolQuery.isNullOrEmpty()) {
                            val searchResult = performWebSearch(toolQuery)
                            val assistantTurn = ChatMessage(role = "assistant", content = if (modelSupportsTools) null else text, reasoningContent = reasoning, toolCalls = assistantMsg.toolCalls)
                            val toolTurn = if (modelSupportsTools) {
                                ChatMessage(role = "tool", content = "SEARCH_RESULTS:\n$searchResult", toolCallId = assistantMsg.toolCalls?.firstOrNull()?.id ?: "manual_id")
                            } else {
                                ChatMessage(role = "user", content = "SEARCH_RESULTS:\n$searchResult\n\nPlease answer based on these results.")
                            }
                            currentHistory = currentHistory + assistantTurn + toolTurn
                            _uiState.update { it.copy(isLoading = true, isSearching = false, progressMessage = ChatConstants.SYNTHESIZING_PROGRESS) }
                            shouldProcess = true
                            continue
                        } else {
                            val msgId = idGenerator.incrementAndGet()
                            _uiState.update { it.copy(messages = it.messages + ChatUiMessage(msgId, "assistant", text, reasoning.ifEmpty { null }), isLoading = false) }
                        }
                    }
                } catch (e: Exception) {
                    if (e !is CancellationException) {
                        setError(e.localizedMessage ?: e.message)
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    return@launch
                }
            }
        }
    }
}
