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
        every { sharedPrefs.getString("server_url", "") } returns ""
        every { sharedPrefs.getString("selected_model", null) } returns null
        every { sharedPrefs.getBoolean("web_search_enabled", true) } returns true
        every { sharedPrefs.getBoolean("streaming_enabled", true) } returns true
        every { sharedPrefs.getFloat("verbosity", 0.5f) } returns 0.5f
        
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
    fun `setServerUrl updates state and saves to preferences`() {
        val newUrl = "http://1.2.3.4:11434"
        
        viewModel.setServerUrl(newUrl)
        
        assertEquals(newUrl, viewModel.uiState.value.serverUrl)
        verify { editor.putString("server_url", newUrl) }
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
        viewModel.setVerbosity(0.8f)
        
        assertEquals(0.8f, viewModel.uiState.value.verbosity)
        verify { editor.putFloat("verbosity", 0.8f) }
        verify { editor.apply() }
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
        
        assertEquals("No model selected. Please select one in settings.", viewModel.uiState.value.error)
        assertEquals(0, viewModel.uiState.value.messages.size)
    }

    @Test
    fun `clearMessages empties the message list`() {
        viewModel.clearMessages()
        assertTrue(viewModel.uiState.value.messages.isEmpty())
    }
    
    @Test
    fun `attachImages updates state correctly`() {
        val images = listOf("base64data")
        viewModel.attachImages(images)
        
        assertEquals(images, viewModel.uiState.value.attachedImagesBase64)
    }
}
