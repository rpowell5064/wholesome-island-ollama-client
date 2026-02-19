package com.wholesomeisland.ollamaclient.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wholesomeisland.ollamaclient.data.remote.ChatMessage
import com.wholesomeisland.ollamaclient.data.remote.OllamaRepository
import com.wholesomeisland.ollamaclient.data.remote.OllamaServiceFactory
import com.wholesomeisland.ollamaclient.data.remote.ToolCall
import com.wholesomeisland.ollamaclient.data.remote.ToolFunction
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val serverUrl: String = ChatConstants.DEFAULT_SERVER_URL,
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
 * Implements the PRINCIPAL methodology for efficient local LLM integration.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("wholesome_island_prefs", Context.MODE_PRIVATE)
    
    private var repository: OllamaRepository? = null
    private var errorTimerJob: Job? = null
    private var infoTimerJob: Job? = null
    private var activeChatJob: Job? = null
    private val idGenerator = AtomicLong(System.currentTimeMillis())
    private var modelSupportsTools: Boolean = true
    
    private val searchClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val _uiState = MutableStateFlow(ChatUiState(
        serverUrl = prefs.getString("server_url", ChatConstants.DEFAULT_SERVER_URL) ?: ChatConstants.DEFAULT_SERVER_URL
    ))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val searchRegex = Regex("""web_search\s*\(\s*(?:query\s*=\s*)?["'](.+?)["']\s*\)|\[SEARCH:\s*["'](.+?)["']\]|(?:tool_code|call|use|query|search|lookup)\s+web_search\s+query:?\s*["'](.+?)["']""", RegexOption.IGNORE_CASE)

    init {
        updateRepository(_uiState.value.serverUrl)
        showStartupNotification()
    }

    /**
     * Shows the initial onboarding/info message.
     */
    private fun showStartupNotification() {
        _uiState.update { it.copy(infoMessage = "Web Search & Streaming are on by default.") }
        infoTimerJob?.cancel()
        infoTimerJob = viewModelScope.launch {
            delay(5000)
            _uiState.update { it.copy(infoMessage = null) }
        }
    }

    /**
     * Re-initializes the Ollama repository with a new base URL.
     */
    private fun updateRepository(url: String) {
        repository = OllamaRepository(OllamaServiceFactory.create(url))
        checkHealthAndLoadModels()
    }

    /**
     * Updates the server URL, saves it to persistent storage, and refreshes the model list.
     */
    fun setServerUrl(url: String) {
        prefs.edit().putString("server_url", url).apply()
        _uiState.update { it.copy(serverUrl = url) }
        updateRepository(url)
    }

    /**
     * Toggles the web search capability.
     */
    fun toggleWebSearch(enabled: Boolean) {
        _uiState.update { it.copy(isWebSearchEnabled = enabled) }
    }

    /**
     * Toggles whether the response is streamed or returned all at once.
     */
    fun toggleStreaming(enabled: Boolean) {
        _uiState.update { it.copy(isStreamingEnabled = enabled) }
    }

    /**
     * Adjusts the verbosity instruction in the system prompt.
     */
    fun setVerbosity(value: Float) {
        _uiState.update { it.copy(verbosity = value) }
    }

    /**
     * Checks if the Ollama server is reachable.
     */
    private fun checkHealthAndLoadModels() {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isServerHealthy = null) }
            val healthResult = repo.healthCheck()
            if (healthResult.isSuccess) {
                _uiState.update { it.copy(isServerHealthy = true) }
                loadModels()
            } else {
                _uiState.update { it.copy(isServerHealthy = false, isLoading = false, error = "Server unreachable.") }
            }
        }
    }

    /**
     * Fetches the list of available models from the Ollama server.
     */
    fun loadModels() {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repo.getModels()
            _uiState.update {
                result.fold(
                    onSuccess = { models ->
                        val defaultModel = if (models.contains("gemma3:12b")) "gemma3:12b" else models.firstOrNull()
                        it.copy(availableModels = models, selectedModel = defaultModel, isLoading = false)
                    },
                    onFailure = { e ->
                        setError("Failed to load models: ${e.message}")
                        it.copy(isLoading = false)
                    }
                )
            }
        }
    }

    /**
     * Stores a list of Base64 encoded images to be sent with the next message.
     */
    fun attachImages(base64Images: List<String>) {
        _uiState.update { it.copy(attachedImagesBase64 = base64Images) }
    }

    /**
     * Clears all currently attached images.
     */
    fun clearAttachedImages() {
        _uiState.update { it.copy(attachedImagesBase64 = emptyList()) }
    }

    /**
     * Changes the active model used for chat.
     */
    fun selectModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
        modelSupportsTools = true 
    }

    /**
     * Resets the conversation history.
     */
    fun clearMessages() {
        _uiState.update { it.copy(messages = emptyList(), error = null) }
    }

    /**
     * Removes a single message from the history by its ID.
     */
    fun deleteMessage(id: Long) {
        _uiState.update { it.copy(messages = it.messages.filter { it.id != id }) }
    }

    /**
     * Hides the current error message.
     */
    fun dismissError() {
        errorTimerJob?.cancel()
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Hides the current info message.
     */
    fun dismissInfo() {
        infoTimerJob?.cancel()
        _uiState.update { it.copy(infoMessage = null) }
    }

    /**
     * Displays an error message in the UI for a limited time.
     */
    private fun setError(message: String?) {
        errorTimerJob?.cancel()
        val displayMessage = if (message.isNullOrBlank() || message == "null") "An unknown error occurred" else message
        _uiState.update { it.copy(error = displayMessage) }
        errorTimerJob = viewModelScope.launch {
            delay(10000)
            _uiState.update { it.copy(error = null) }
        }
    }

    /**
     * Cancels the active chat request and resets the loading state.
     */
    fun cancelRequest() {
        activeChatJob?.cancel()
        activeChatJob = null
        _uiState.update { it.copy(isLoading = false, isSearching = false, searchQuery = null, progressMessage = ChatConstants.IDLE_PROGRESS) }
    }

    /**
     * Performs a web search using DuckDuckGo and returns formatted snippets.
     */
    private suspend fun performWebSearch(query: String): String = withContext(Dispatchers.IO + NonCancellable) {
        withContext(Dispatchers.Main) { 
            _uiState.update { it.copy(isSearching = true, isLoading = true, progressMessage = "Searching web for '$query'...") } 
        }
        try {
            val url = "https://html.duckduckgo.com/html/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()
            
            searchClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "Search failed: HTTP ${response.code}"
                val html = response.body?.string() ?: ""
                
                withContext(Dispatchers.Main) { 
                    _uiState.update { it.copy(progressMessage = ChatConstants.ANALYZING_PROGRESS) } 
                }
                
                val doc = Jsoup.parse(html)
                val results = mutableListOf<String>()
                doc.select(".result").take(5).forEach { res ->
                    val title = res.select(".result__a").text()
                    val snippet = res.select(".result__snippet").text()
                    if (title.isNotEmpty()) results.add("**$title**\n$snippet")
                }

                if (results.isEmpty()) "No search results found." 
                else results.joinToString("\n\n")
            }
        } catch (e: Exception) {
            "Search error: ${e.localizedMessage ?: e.message}"
        }
    }

    /**
     * Triggers a message based on a Quick Action preset.
     */
    fun performQuickAction(action: QuickAction) {
        sendMessage(action.prompt)
    }

    /**
     * Helper to get the JSON definition of the web_search tool.
     */
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

    /**
     * Prepares and starts the message sending process.
     * Implements KV Cache stabilization and history trimming for efficiency.
     */
    fun sendMessage(text: String) {
        val state = _uiState.value
        val model = state.selectedModel ?: return
        val attachedImages = state.attachedImagesBase64
        val webSearch = state.isWebSearchEnabled
        val stream = state.isStreamingEnabled

        val newUserMessage = ChatUiMessage(idGenerator.incrementAndGet(), "user", text, imagesBase64 = attachedImages)
        
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val systemContent = buildString {
            append("TODAY'S DATE: $currentDate\n\n")
            append(ChatConstants.BASE_SYSTEM_PROMPT)
            if (webSearch) {
                append("\n\n")
                append(ChatConstants.WEB_SEARCH_INSTRUCTION)
                if (modelSupportsTools) append(ChatConstants.TOOL_MODE_PROMPT)
                else append(ChatConstants.NO_TOOL_MODE_PROMPT)
            }
        }

        val history = mutableListOf<ChatMessage>()
        history.add(ChatMessage(role = "system", content = systemContent))
        
        // Trim history: only keep images for the last 2 turns to save tokens/latency
        val imageCutoff = state.messages.size - 2
        
        history.addAll(state.messages.mapIndexed { index, msg -> 
            ChatMessage(
                role = msg.role, 
                content = msg.text, 
                reasoningContent = msg.reasoning, 
                images = if (index >= imageCutoff) msg.imagesBase64.ifEmpty { null } else null, 
                toolCalls = msg.toolCalls
            )
        })

        history.add(ChatMessage("user", text, null, attachedImages.ifEmpty { null }, null, null))

        _uiState.update { it.copy(messages = it.messages + newUserMessage, isLoading = true, progressMessage = ChatConstants.IDLE_PROGRESS, error = null, attachedImagesBase64 = emptyList()) }

        performSendMessage(model, history, webSearch, stream)
    }

    /**
     * Core message processing loop. Handles streaming, tool call detection, 
     * search execution, and final answer synthesis in a single continuous flow.
     */
    private fun performSendMessage(model: String, history: List<ChatMessage>, webSearch: Boolean, stream: Boolean) {
        activeChatJob?.cancel()
        activeChatJob = viewModelScope.launch {
            val repo = repository ?: return@launch
            _uiState.update { it.copy(isLoading = true, progressMessage = ChatConstants.IDLE_PROGRESS) }

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
                                    
                                    if ((capturedToolCalls != null && capturedToolCalls!!.isNotEmpty()) || textMatch != null) {
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
                        // Non-streaming logic (similar loop structure)
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

                        if (!toolQuery.isNullOrEmpty()) {
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
