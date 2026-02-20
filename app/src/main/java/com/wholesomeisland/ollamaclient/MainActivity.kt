package com.wholesomeisland.ollamaclient

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wholesomeisland.ollamaclient.ui.theme.ChatScreen
import com.wholesomeisland.ollamaclient.ui.theme.ChatViewModel
import com.wholesomeisland.ollamaclient.ui.theme.OllamaTheme

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            OllamaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatScreenRoot(chatViewModel)
                }
            }
        }
    }
}

@Composable
fun ChatScreenRoot(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bytes = context.contentResolver.openInputStream(it)?.use { input ->
                input.readBytes()
            }
            if (bytes != null) {
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                viewModel.attachImages(listOf(base64))
            }
        }
    }

    ChatScreen(
        state = state,
        onSend = { viewModel.sendMessage(it) },
        onPickImage = { imagePicker.launch("image/*") },
        onUpdateConnection = { url, key -> viewModel.setConnectionDetails(url, key) },
        onSelectModel = { model -> viewModel.selectModel(model) },
        onClearChat = { viewModel.clearMessages() },
        onDeleteMessage = { id -> viewModel.deleteMessage(id) },
        onShareMessage = { text ->
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            context.startActivity(shareIntent)
        },
        onDismissError = { viewModel.dismissError() },
        onClearAttachedImages = { viewModel.clearAttachedImages() },
        onToggleWebSearch = { viewModel.toggleWebSearch(it) },
        onCancelRequest = { viewModel.cancelRequest() },
        onToggleStreaming = { viewModel.toggleStreaming(it) },
        onSetVerbosity = { viewModel.setVerbosity(it) },
        onQuickAction = { viewModel.performQuickAction(it) },
        onTryAgain = { viewModel.sendMessage(it) },
        onDismissInfo = { viewModel.dismissInfo() },
        onAddSearchEngine = { name, type, url, key, authHeader -> viewModel.addSearchEngine(name, type, url, key, authHeader) },
        onRemoveSearchEngine = { id -> viewModel.removeSearchEngine(id) },
        onSelectSearchEngine = { id -> viewModel.setSelectedSearchEngine(id) }
    )
}
