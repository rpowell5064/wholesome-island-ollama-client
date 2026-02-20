package com.wholesomeisland.ollamaclient

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wholesomeisland.ollamaclient.ui.theme.ChatScreen
import com.wholesomeisland.ollamaclient.ui.theme.ChatViewModel
import com.wholesomeisland.ollamaclient.ui.theme.OllamaTheme
import kotlinx.coroutines.delay

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
fun StreamingSplashScreen(onFinish: () -> Unit) {
    val fullText = "Wholesome Island"
    var displayedText by remember { mutableStateOf("") }
    
    // Blinking cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    LaunchedEffect(Unit) {
        delay(500)
        fullText.forEachIndexed { index, _ ->
            displayedText = fullText.substring(0, index + 1)
            delay(100) // Streaming speed
        }
        delay(1200) // Hold after finishing
        onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_palm_tree_emoji),
                contentDescription = null,
                modifier = Modifier.size(140.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayedText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp
                    ),
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold
                )
                // AI-style cursor
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .width(12.dp)
                        .height(24.dp)
                        .background(Color(0xFF1A1A1A).copy(alpha = cursorAlpha))
                )
            }
        }
    }
}

@Composable
fun ChatScreenRoot(viewModel: ChatViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        StreamingSplashScreen(onFinish = { showSplash = false })
    } else {
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
}
