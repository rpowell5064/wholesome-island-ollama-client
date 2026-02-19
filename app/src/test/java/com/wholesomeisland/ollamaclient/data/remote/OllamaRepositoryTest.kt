package com.wholesomeisland.ollamaclient.data.remote

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class OllamaRepositoryTest {

    private lateinit var api: OllamaApi
    private lateinit var repository: OllamaRepository

    @Before
    fun setup() {
        api = mockk()
        repository = OllamaRepository(api)
    }

    @Test
    fun `healthCheck returns success when api call succeeds`() = runBlocking {
        coEvery { api.healthCheck() } returns "".toResponseBody(null)

        val result = repository.healthCheck()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        coVerify { api.healthCheck() }
    }

    @Test
    fun `healthCheck returns failure when api call fails`() = runBlocking {
        coEvery { api.healthCheck() } throws Exception("Network error")

        val result = repository.healthCheck()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getVersion returns version string`() = runBlocking {
        coEvery { api.getVersion() } returns OllamaVersionResponse("0.1.2")

        val result = repository.getVersion()

        assertTrue(result.isSuccess)
        assertEquals("0.1.2", result.getOrThrow())
    }

    @Test
    fun `getModels returns list of model names`() = runBlocking {
        val response = OllamaTagsResponse(
            models = listOf(
                OllamaModelTag("llama3"),
                OllamaModelTag("gemma")
            )
        )
        coEvery { api.getModels() } returns response

        val result = repository.getModels()

        assertTrue(result.isSuccess)
        assertEquals(listOf("llama3", "gemma"), result.getOrThrow())
    }

    @Test
    fun `sendChat calls api with correct request`() = runBlocking {
        val history = listOf(ChatMessage("user", "Hello"))
        coEvery { api.chat(any()) } returns Response.success(ChatResponse("llama3", message = ChatMessage("assistant", "Hi"), done = true))

        val response = repository.sendChat("llama3", history)

        assertTrue(response.isSuccessful)
        assertEquals("Hi", response.body()?.message?.content)
        coVerify { api.chat(match { it.model == "llama3" && it.messages == history }) }
    }

    @Test
    fun `parseChatStream emits chat messages from response body`() = runBlocking {
        val json = """
            {"message": {"role": "assistant", "content": "Hello"}}
            {"message": {"role": "assistant", "content": " world"}}
        """.trimIndent()
        val responseBody = json.toResponseBody("application/x-ndjson".toMediaType())

        repository.parseChatStream(responseBody).test {
            assertEquals("Hello", awaitItem().content)
            assertEquals(" world", awaitItem().content)
            awaitComplete()
        }
    }
    
    @Test
    fun `parseGenerateStream emits strings from response body`() = runBlocking {
        val json = """
            {"response": "Hello"}
            {"response": " world"}
        """.trimIndent()
        val responseBody = json.toResponseBody("application/x-ndjson".toMediaType())

        repository.parseGenerateStream(responseBody).test {
            assertEquals("Hello", awaitItem())
            assertEquals(" world", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `parseChatStream skips empty lines and invalid json`() = runBlocking {
        val json = """
            
            {"message": {"role": "assistant", "content": "Hello"}}
            {invalid}
            {"message": {"role": "assistant", "content": "!"}}
        """.trimIndent()
        val responseBody = json.toResponseBody("application/x-ndjson".toMediaType())

        repository.parseChatStream(responseBody).test {
            assertEquals("Hello", awaitItem().content)
            assertEquals("!", awaitItem().content)
            awaitComplete()
        }
    }
}
