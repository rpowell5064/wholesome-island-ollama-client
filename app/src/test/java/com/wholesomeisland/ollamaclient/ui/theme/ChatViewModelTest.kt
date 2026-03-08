package com.wholesomeisland.ollamaclient.ui.theme

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.wholesomeisland.ollamaclient.data.remote.OllamaRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val application: Application = mockk()
    private val sharedPrefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    
    private lateinit var viewModel: ChatViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { application.getSharedPreferences("wholesome_island_prefs", Context.MODE_PRIVATE) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.putFloat(any(), any()) } returns editor
        every { sharedPrefs.getString("server_url", "http://0.0.0.0") } returns "http://0.0.0.0"
        every { sharedPrefs.getString("server_port", "11434") } returns "11434"
        every { sharedPrefs.getString("api_key", "") } returns ""
        every { sharedPrefs.getString("search_engines_json", null) } returns null
        every { sharedPrefs.getString("selected_search_engine_id", any()) } returns ChatConstants.DEFAULT_SEARCH_ENGINE_ID
        every { sharedPrefs.getString("selected_model", null) } returns null
        every { sharedPrefs.getBoolean("web_search_enabled", true) } returns true
        every { sharedPrefs.getBoolean("streaming_enabled", true) } returns true
        every { sharedPrefs.getString("verbosity_level", any()) } returns VerbosityLevel.BALANCED.name
        every { sharedPrefs.getString("chat_contexts_json", null) } returns null
        every { sharedPrefs.getString("current_context_id", null) } returns null
        every { sharedPrefs.getString("custom_prompts_json", null) } returns null
        every { sharedPrefs.getString("selected_prompt_id", "default_prompt") } returns "default_prompt"
        
        mockkObject(com.wholesomeisland.ollamaclient.data.remote.OllamaServiceFactory)
        
        viewModel = ChatViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `initial state shows welcome message when server URL is blank`() {
        assertEquals("Welcome! Please enter your Ollama server address in settings.", viewModel.uiState.value.infoMessage)
        assertNull(viewModel.uiState.value.selectedModel)
    }

    @Test
    fun `setConnectionDetails updates state and saves to preferences`() {
        val newUrl = "http://1.2.3.4"
        val newPort = "11434"
        val newKey = "test-key"
        
        viewModel.setConnectionDetails(newUrl, newPort, newKey)
        
        assertEquals(newUrl, viewModel.uiState.value.serverUrl)
        assertEquals(newPort, viewModel.uiState.value.serverPort)
        assertEquals(newKey, viewModel.uiState.value.apiKey)
        verify { editor.putString("server_url", newUrl) }
        verify { editor.putString("server_port", newPort) }
        verify { editor.putString("api_key", newKey) }
        verify { editor.apply() }
    }

    @Test
    fun `toggleWebSearch updates state and saves preference`() {
        viewModel.toggleWebSearch(false)
        
        assertEquals(false, viewModel.uiState.value.isWebSearchEnabled)
        verify { editor.putBoolean("web_search_enabled", false) }
        verify { editor.apply() }
    }

    @Test
    fun `setVerbosity updates state and saves preference`() {
        viewModel.setVerbosity(VerbosityLevel.CONCISE)
        
        assertEquals(VerbosityLevel.CONCISE, viewModel.uiState.value.verbosity)
        verify { editor.putString("verbosity_level", VerbosityLevel.CONCISE.name) }
        verify { editor.apply() }
    }

    @Test
    fun `createNewChat generates new context and clears messages`() {
        val oldContextId = viewModel.uiState.value.currentContextId
        
        viewModel.createNewChat()
        
        val newContextId = viewModel.uiState.value.currentContextId
        assertNotEquals(oldContextId, newContextId)
        assertTrue(viewModel.uiState.value.messages.isEmpty())
        verify { editor.putString("chat_contexts_json", any()) }
    }

    @Test
    fun `addPrompt adds new prompt to list and saves`() {
        val name = "Test Persona"
        val content = "You are a test."
        
        viewModel.addPrompt(name, content)
        
        val prompts = viewModel.uiState.value.customPrompts
        assertTrue(prompts.any { it.name == name && it.content == content })
        verify { editor.putString("custom_prompts_json", any()) }
    }

    @Test
    fun `deletePrompt removes prompt and falls back to default if active`() {
        viewModel.addPrompt("To Delete", "Content")
        val addedId = viewModel.uiState.value.customPrompts.last().id
        viewModel.setSelectedPrompt(addedId)
        
        viewModel.deletePrompt(addedId)
        
        assertTrue(viewModel.uiState.value.customPrompts.none { it.id == addedId })
        assertEquals("default_prompt", viewModel.uiState.value.selectedPromptId)
    }

    @Test
    fun `addSearchEngine updates state and saves to preferences`() {
        val name = "Custom Search"
        val type = "API_GET"
        val url = "https://api.test.com?q={query}"
        
        viewModel.addSearchEngine(name, type, url, "key", "X-Key")
        
        val engines = viewModel.uiState.value.searchEngines
        assertTrue(engines.any { it.name == name && it.type == type })
        verify { editor.putString("search_engines_json", any()) }
    }

    @Test
    fun `selectModel updates state and clears info message`() {
        viewModel.selectModel("llama3")
        
        assertEquals("llama3", viewModel.uiState.value.selectedModel)
        assertNull(viewModel.uiState.value.infoMessage)
        verify { editor.putString("selected_model", "llama3") }
        verify { editor.apply() }
    }

    @Test
    fun `sendMessage shows error when no model is selected`() {
        viewModel.sendMessage("Hello")
        
        assertEquals("No model selected. Please select one.", viewModel.uiState.value.error)
        assertEquals(0, viewModel.uiState.value.messages.size)
    }
}
