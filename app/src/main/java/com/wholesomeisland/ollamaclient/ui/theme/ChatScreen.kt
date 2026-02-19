package com.wholesomeisland.ollamaclient.ui.theme

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import kotlinx.coroutines.launch
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    onSend: (String) -> Unit,
    onPickImage: () -> Unit,
    onChangeServerUrl: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onClearChat: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onShareMessage: (String) -> Unit,
    onDismissError: () -> Unit,
    onClearAttachedImages: () -> Unit,
    onToggleWebSearch: (Boolean) -> Unit,
    onCancelRequest: () -> Unit,
    onToggleStreaming: (Boolean) -> Unit,
    onSetVerbosity: (Float) -> Unit,
    onQuickAction: (QuickAction) -> Unit,
    onTryAgain: (String) -> Unit,
    onDismissInfo: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    val mainBackground = Color(0xFF1A1A1A)
    val responseBackground = Color(0xFF252525)
    
    val purple = MaterialTheme.colorScheme.primary
    val lightGray = Color(0xFFBDBDBD)
    val darkGray = Color(0xFF424242)

    var showTermsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.messages.size, state.isLoading, state.isSearching) {
        if ((state.isLoading || state.isSearching) && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Last Updated: February 19, 2026\n\n" +
                        "1. Acceptance of Terms\n" +
                        "By downloading or using Wholesome Island (the \"App\"), you agree to these Terms of Service. Wholesome Island is an independent client interface designed for the Ollama framework. It is not affiliated with, endorsed by, or partnered with the Ollama project, Meta, Google, or any other AI model provider.\n\n" +
                        "2. Nature of Service: Local-First AI\n" +
                        "Wholesome Island is a locally-hosted client interface.\n" +
                        "• User-Provided Infrastructure: You are responsible for hosting and maintaining your own Ollama server. The App does not provide the AI models; it facilitates interaction with models you have installed on your own hardware.\n" +
                        "• Performance: AI response quality, speed, and accuracy are entirely dependent on your local hardware and the specific model weights (e.g., Llama, Gemma, Mistral) you choose to run.\n\n" +
                        "3. Future-Proof Web Search & Agentic Tools\n" +
                        "The App includes a \"Web Search\" feature utilizing Jsoup to retrieve real-time information via DuckDuckGo.\n" +
                        "• Knowledge Retrieval: This feature is designed to \"future-proof\" your local models by bridging the gap between their training cutoff and current events.\n" +
                        "• Third-Party Data: Search queries are processed directly via DuckDuckGo. By using this feature, you acknowledge that your search queries pulling from DuckDuckGo’s privacy standards.\n" +
                        "• No Warranty on Results: The developer is not responsible for the accuracy of web search results or the AI's interpretation of that data.\n\n" +
                        "4. Privacy & Data Integrity\n" +
                        "• No Middleware: Communication occurs directly between your mobile device and your Ollama server. No chat data, system prompts, or images are ever transmitted to the App developer.\n" +
                        "• Local Storage: Your chat history and server configurations are stored locally on your device.\n" +
                        "• Vision Data: If using Vision-enabled models, images are processed on your own hardware via your Ollama instance.\n\n" +
                        "5. Voluntary Support (The \"Tip Jar\")\n" +
                        "• Donation-Based: All core features of Wholesome Island are provided for free.\n" +
                        "• Support the Developer: Tips made via \"Buy Me a Coffee\" or other links are strictly voluntary donations to support the independent development and future-proofing of this tool.\n" +
                        "• No Refunds: Tips are non-refundable and do not constitute a contract for guaranteed uptime, specific future features, or technical support.\n\n" +
                        "6. Intellectual Property\n" +
                        "• \"Ollama,\" \"Llama,\" \"Gemma,\" and other model names are the trademarks of their respective owners.\n" +
                        "• Wholesome Island is a third-party tool; its use of these names does not imply any trademark infringement or official affiliation.\n\n" +
                        "7. Limitation of Liability\n" +
                        "To the maximum extent permitted by law, the developer shall not be liable for any damages—including data loss, hardware strain, or server issues—arising from the use of the App or its connection to your local AI environment.\n\n" +
                        "8. Changes to Terms\n" +
                        "The developer reserves the right to update these terms to reflect new \"future-proof\" features or tools. Continued use of the App constitutes acceptance of any updated terms.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color(0xFF333333),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF222222)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Settings", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = when (state.isServerHealthy) {
                                true -> Color(0xFF4CAF50)
                                false -> Color(0xFFF44336)
                                null -> Color.Gray
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                                    .semantics { contentDescription = "Server status indicator" }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = when (state.isServerHealthy) {
                                    true -> "Online"
                                    false -> "Offline"
                                    null -> "Checking..."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))

                    var ipInput by remember { mutableStateOf(state.serverUrl.removePrefix("http://").substringBeforeLast(":")) }
                    var portInput by remember { mutableStateOf(state.serverUrl.substringAfterLast(":", "11434")) }
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() || it == '.' }) {
                                    ipInput = newValue
                                }
                            },
                            label = { Text("IP Address") },
                            placeholder = { Text("192.168.1.5") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purple,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = purple,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                                    portInput = newValue
                                }
                            },
                            label = { Text("Port") },
                            modifier = Modifier.width(100.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = purple,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedLabelColor = purple,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            val finalPort = if (portInput.isBlank()) "11434" else portInput
                            onChangeServerUrl("http://$ipInput:$finalPort") 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = purple)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm server connection")
                        Spacer(Modifier.width(8.dp))
                        Text("Connect Server")
                    }

                    Spacer(Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Web Search", modifier = Modifier.weight(1f), color = Color.White)
                        Switch(
                            checked = state.isWebSearchEnabled,
                            onCheckedChange = onToggleWebSearch,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = purple,
                                checkedTrackColor = purple.copy(alpha = 0.5f),
                                uncheckedThumbColor = lightGray,
                                uncheckedTrackColor = darkGray
                            ),
                            modifier = Modifier.semantics { contentDescription = "Enable or disable real-time web search" }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Streaming", modifier = Modifier.weight(1f), color = Color.White)
                        Switch(
                            checked = state.isStreamingEnabled,
                            onCheckedChange = onToggleStreaming,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = purple,
                                checkedTrackColor = purple.copy(alpha = 0.5f),
                                uncheckedThumbColor = lightGray,
                                uncheckedTrackColor = darkGray
                            ),
                            modifier = Modifier.semantics { contentDescription = "Enable or disable real-time response streaming" }
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Model Verbosity", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Slider(
                        value = state.verbosity,
                        onValueChange = onSetVerbosity,
                        colors = SliderDefaults.colors(
                            thumbColor = purple,
                            activeTrackColor = purple,
                            inactiveTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.semantics { contentDescription = "Adjust model verbosity from brief to detailed" }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Brief", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("Detailed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Model", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    
                    if (state.availableModels.isEmpty()) {
                        Text("No models found", color = Color.DarkGray)
                    } else {
                        state.availableModels.forEach { model ->
                            NavigationDrawerItem(
                                label = { Text(model) },
                                selected = model == state.selectedModel,
                                onClick = { 
                                    onSelectModel(model)
                                    scope.launch { drawerState.close() }
                                },
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).semantics { contentDescription = "Select AI model $model" },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = purple.copy(alpha = 0.1f),
                                    unselectedContainerColor = Color.Transparent,
                                    selectedTextColor = purple,
                                    unselectedTextColor = Color.Gray
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Text("Support & Legal", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(Modifier.height(8.dp))

                    NavigationDrawerItem(
                        label = { Text("Buy Me a Coffee", fontWeight = FontWeight.Bold) },
                        selected = false,
                        onClick = { uriHandler.openUri("https://buymeacoffee.com/rpowell5064") },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4081)) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color(0xFF333333).copy(alpha = 0.5f),
                            unselectedTextColor = Color.White
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).semantics { contentDescription = "Support the developer via Buy Me a Coffee" }
                    )

                    Spacer(Modifier.height(8.dp))

                    NavigationDrawerItem(
                        label = { Text("GitHub Repository") },
                        selected = false,
                        onClick = { uriHandler.openUri("https://github.com/rpowell5064/wholesome-island-ollama-client/tree/master") },
                        icon = { Icon(Icons.Default.Code, contentDescription = null, tint = Color.Gray) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).semantics { contentDescription = "View the source code on GitHub" }
                    )

                    Spacer(Modifier.height(8.dp))

                    NavigationDrawerItem(
                        label = { Text("Terms of Service") },
                        selected = false,
                        onClick = { showTermsDialog = true },
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray) },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = Color.Gray
                        ),
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).semantics { contentDescription = "Open the Terms of Service dialog" }
                    )

                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = onClearChat,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.LightGray)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear Chat", color = Color.LightGray)
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = mainBackground,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Wholesome Island 🌴", color = Color.White, fontWeight = FontWeight.Bold)
                            if (state.selectedModel != null) {
                                Text(
                                    state.selectedModel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.semantics { contentDescription = "Active model: ${state.selectedModel}" }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = mainBackground,
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open settings menu")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).background(mainBackground)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (state.messages.any { it.role == "assistant" && it.text.isNotEmpty() }) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.quickActions) { action ->
                                SuggestionChip(
                                    onClick = { 
                                        onQuickAction(action)
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    },
                                    label = { Text(action.label, color = Color.White) },
                                    border = null,
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF333333)),
                                    modifier = Modifier.semantics { contentDescription = "Quick action: ${action.label}" }
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true
                    ) {
                        if (state.isLoading || state.isSearching) {
                            item {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth().semantics(mergeDescendants = true) {
                                        liveRegion = LiveRegionMode.Polite
                                    },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    if (state.isSearching) {
                                        Icon(
                                            Icons.Default.Search, 
                                            contentDescription = null, 
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = state.progressMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontStyle = FontStyle.Italic,
                                        color = Color.Gray,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = onCancelRequest,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.StopCircle,
                                            contentDescription = "Cancel current AI request",
                                            tint = Color.DarkGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        items(state.messages.reversed(), key = { it.id }) { msg ->
                            if (msg.text.isNotBlank() || msg.imagesBase64.isNotEmpty() || !msg.reasoning.isNullOrBlank()) {
                                ResponseBlock(
                                    message = msg,
                                    onDelete = { onDeleteMessage(msg.id) },
                                    onShare = { onShareMessage(msg.text) },
                                    onTryAgain = { onTryAgain(msg.text) },
                                    aiBackground = responseBackground,
                                    accentColor = purple
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF333333))

                    if (state.attachedImagesBase64.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.attachedImagesBase64) { base64 ->
                                val bitmap = remember(base64) {
                                    val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                }
                                Box {
                                    bitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "User attached image",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    IconButton(
                                        onClick = onClearAttachedImages,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove attached image", tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    var input by remember { mutableStateOf("") }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onPickImage) {
                            Icon(Icons.Default.Image, contentDescription = "Attach an image to your message", tint = Color.Gray)
                        }
                        TextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask anything...", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            label = { Text("Chat message") }
                        )
                        IconButton(
                            onClick = {
                                if (input.isNotBlank() || state.attachedImagesBase64.isNotEmpty()) {
                                    onSend(input.trim())
                                    input = ""
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            },
                            enabled = !state.isLoading
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message to AI", tint = if (input.isNotBlank()) MaterialTheme.colorScheme.primary else Color.DarkGray)
                        }
                    }
                }

                // Startup notification
                AnimatedVisibility(
                    visible = state.infoMessage != null,
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Announcement: ${state.infoMessage}" }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = state.infoMessage ?: "", color = Color.White, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = onDismissInfo, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss announcement", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Error overlay
                AnimatedVisibility(
                    visible = state.error != null,
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF422222)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Error alert: ${state.error}" }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = state.error ?: "", color = Color(0xFFFFB4AB), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = onDismissError) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss error message", tint = Color(0xFFFFB4AB))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResponseBlock(
    message: ChatUiMessage,
    onDelete: () -> Unit,
    onShare: (String) -> Unit,
    onTryAgain: (String) -> Unit,
    aiBackground: Color,
    accentColor: Color
) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current

    Surface(
        color = if (isUser) Color.Transparent else aiBackground,
        shape = if (isUser) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = if (isUser) "Your message: ${message.text}" else "AI response: ${message.text}"
            }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            if (!message.reasoning.isNullOrBlank()) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp).semantics(mergeDescendants = true) { 
                        contentDescription = "AI thinking process: ${message.reasoning}" 
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Thought", style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Bold)
                        Text(
                            text = message.reasoning,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (message.imagesBase64.isNotEmpty()) {
                message.imagesBase64.forEach { base64 ->
                    val bitmap = remember(base64) {
                        val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Attached image in conversation",
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .sizeIn(maxWidth = 400.dp, maxHeight = 400.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            if (message.text.isNotBlank()) {
                if (isUser) {
                    SelectionContainer {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color.White,
                                lineHeight = 26.sp
                            )
                        )
                    }
                } else {
                    RichText(
                        style = RichTextStyle(
                            codeBlockStyle = CodeBlockStyle(
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.LightGray
                                ),
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            )
                        )
                    ) {
                        Markdown(content = message.text)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (isUser) {
                    IconButton(onClick = { onTryAgain(message.text) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Resend this message to the AI", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
                IconButton(onClick = { 
                    // Improved copy logic to strip markdown ticks if requested
                    val textToCopy = if (message.text.contains("```")) {
                        // Extract content between triple backticks if present
                        val regex = "```(?:[a-zA-Z]*\\n)?([\\s\\S]*?)```".toRegex()
                        val matches = regex.findAll(message.text)
                        if (matches.any()) {
                            matches.joinToString("\n\n") { it.groupValues[1].trim() }
                        } else {
                            message.text
                        }
                    } else {
                        message.text
                    }
                    clipboardManager.setText(AnnotatedString(textToCopy)) 
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy code or message text to clipboard", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { onShare(message.text) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share message text", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { onDelete() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete this message", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
