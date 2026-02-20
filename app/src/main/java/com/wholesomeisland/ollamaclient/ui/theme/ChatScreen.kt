package com.wholesomeisland.ollamaclient.ui.theme

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.wholesomeisland.ollamaclient.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    onSend: (String) -> Unit,
    onPickImage: () -> Unit,
    onUpdateConnection: (String, String, String) -> Unit,
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
    onDismissInfo: () -> Unit,
    onAddSearchEngine: (String, String, String, String, String) -> Unit,
    onRemoveSearchEngine: (String) -> Unit,
    onSelectSearchEngine: (String) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val mainBackground = Color(0xFF1A1A1A)
    val responseBackground = Color(0xFF252525)
    val purple = MaterialTheme.colorScheme.primary

    var showAddEngineDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var termsText by remember { mutableStateOf("Loading...") }

    LaunchedEffect(showTermsDialog) {
        if (showTermsDialog) {
            try {
                termsText = context.resources.openRawResource(R.raw.terms_of_service).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                termsText = "Error loading Terms of Service."
            }
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms & Privacy") },
            text = {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        RichText {
                            Markdown(termsText)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showTermsDialog = false }) { Text("Close") } },
            containerColor = Color(0xFF333333),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF222222)) {
                Column(
                    modifier = Modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp)
                ) {
                    Text("Settings", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.height(24.dp))

                    // Ollama Connection
                    Text("Ollama Server", style = MaterialTheme.typography.labelLarge, color = purple)
                    var urlInput by remember { mutableStateOf(state.serverUrl) }
                    var portInput by remember { mutableStateOf(state.serverPort) }
                    var apiKeyInput by remember { mutableStateOf(state.apiKey) }
                    OutlinedTextField(value = urlInput, onValueChange = { urlInput = it }, label = { Text("Server URL") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = portInput, onValueChange = { portInput = it }, label = { Text("Port") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    OutlinedTextField(value = apiKeyInput, onValueChange = { apiKeyInput = it }, label = { Text("API Key (Optional)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White))
                    Button(onClick = { onUpdateConnection(urlInput, portInput, apiKeyInput) }, modifier = Modifier.fillMaxWidth()) { Text("Update Connection") }

                    Spacer(Modifier.height(24.dp))

                    // Dynamic Search Engines
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Search Engines", style = MaterialTheme.typography.labelLarge, color = purple, modifier = Modifier.weight(1f))
                        IconButton(onClick = { showAddEngineDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add search engine", tint = purple) }
                    }
                    state.searchEngines.forEach { engine ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onSelectSearchEngine(engine.id) }.padding(vertical = 4.dp)) {
                            RadioButton(selected = state.selectedSearchEngineId == engine.id, onClick = { onSelectSearchEngine(engine.id) })
                            Text(engine.name, color = if (state.selectedSearchEngineId == engine.id) Color.White else Color.Gray, modifier = Modifier.weight(1f))
                            if (engine.isDeletable) {
                                IconButton(onClick = { onRemoveSearchEngine(engine.id) }) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Web Search", modifier = Modifier.weight(1f), color = Color.White)
                        Switch(checked = state.isWebSearchEnabled, onCheckedChange = onToggleWebSearch)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Streaming", modifier = Modifier.weight(1f), color = Color.White)
                        Switch(checked = state.isStreamingEnabled, onCheckedChange = onToggleStreaming)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text("Model Verbosity", color = Color.White)
                    Slider(value = state.verbosity, onValueChange = onSetVerbosity)
                    Spacer(Modifier.height(24.dp))
                    Text("Model", color = Color.White)
                    
                    if (state.isLoading && state.availableModels.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = purple)
                    } else if (state.availableModels.isEmpty()) {
                        Text("No models found. Check connection.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    state.availableModels.forEach { model ->
                        NavigationDrawerItem(label = { Text(model) }, selected = model == state.selectedModel, onClick = { onSelectModel(model); scope.launch { drawerState.close() } })
                    }

                    Spacer(Modifier.height(32.dp))
                    Text("Support & Legal", style = MaterialTheme.typography.labelLarge, color = purple)
                    Spacer(Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text("Buy Me a Coffee") },
                        selected = false,
                        onClick = { uriHandler.openUri("https://buymeacoffee.com/rpowell5064") },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4081)) }
                    )
                    NavigationDrawerItem(
                        label = { Text("GitHub Repository") },
                        selected = false,
                        onClick = { uriHandler.openUri("https://github.com/rpowell5064/wholesome-island-ollama-client/tree/master") },
                        icon = { Icon(Icons.Default.Code, contentDescription = null, tint = Color.Gray) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Terms & Privacy") },
                        selected = false,
                        onClick = { showTermsDialog = true },
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray) }
                    )

                    Spacer(Modifier.height(32.dp))
                    Button(onClick = onClearChat, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) { Text("Clear Chat", color = Color.LightGray) }
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
                            Text("Wholesome Island 🌴", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (state.selectedModel != null) {
                                Text(state.selectedModel, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = mainBackground),
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White) } }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val listState = rememberLazyListState()
                    LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(), reverseLayout = true) {
                        if (state.isLoading || state.isSearching) {
                            item {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(state.progressMessage, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = onCancelRequest) { Icon(Icons.Default.StopCircle, contentDescription = null, tint = Color.DarkGray) }
                                }
                            }
                        }
                        items(state.messages.reversed(), key = { it.id }) { msg ->
                            ResponseBlock(msg, { onDeleteMessage(msg.id) }, { onShareMessage(msg.text) }, { onTryAgain(msg.text) }, responseBackground, purple)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF333333))

                    var input by remember { mutableStateOf("") }
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPickImage) { Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray) }
                        TextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask anything...", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        IconButton(onClick = { if (input.isNotBlank()) { onSend(input.trim()); input = ""; keyboardController?.hide(); focusManager.clearFocus() } }, enabled = !state.isLoading) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = if (input.isNotBlank()) purple else Color.DarkGray)
                        }
                    }
                }

                // Info/Error Overlays
                if (state.infoMessage != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF333333)), modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(state.infoMessage, color = Color.White, modifier = Modifier.weight(1f))
                            IconButton(onClick = onDismissInfo) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray) }
                        }
                    }
                }
                if (state.error != null) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF422222)), modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(state.error, color = Color(0xFFFFB4AB), modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDismissError() }) { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFFB4AB)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddEngineDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(SearchEngineType.API_GET) }
        var url by remember { mutableStateOf("") }
        var apiKey by remember { mutableStateOf("") }
        var authHeader by remember { mutableStateOf("Authorization") }

        AlertDialog(
            onDismissRequest = { showAddEngineDialog = false },
            title = { Text("Add Search Engine") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Friendly Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Request Type", style = MaterialTheme.typography.labelSmall)
                    Row {
                        SearchEngineType.values().filter { it != SearchEngineType.DUCKDUCKGO }.forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t.name) }, modifier = Modifier.padding(end = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("API URL (use {query} for GET)") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("https://api.example.com/search?q={query}") })
                    OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key (Optional)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = authHeader, onValueChange = { authHeader = it }, label = { Text("Auth Header (Default: Authorization)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && url.isNotBlank()) {
                        onAddSearchEngine(name, type.name, url, apiKey, authHeader)
                        showAddEngineDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddEngineDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ResponseBlock(message: ChatUiMessage, onDelete: () -> Unit, onShare: (String) -> Unit, onTryAgain: (String) -> Unit, aiBackground: Color, accentColor: Color) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    
    Surface(color = if (isUser) Color.Transparent else aiBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (!message.reasoning.isNullOrBlank()) {
                Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Thought", style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Bold)
                        Text(message.reasoning, color = Color.LightGray.copy(alpha = 0.8f), fontStyle = FontStyle.Italic)
                    }
                }
            }
            if (isUser) {
                SelectionContainer { Text(message.text, color = Color.White, style = MaterialTheme.typography.bodyLarge) }
            } else {
                RichText { Markdown(message.text) }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                if (isUser) IconButton(onClick = { onTryAgain(message.text) }) { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = { 
                    val textToCopy = if (message.text.contains("```")) {
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
                }) { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = { onShare(message.text) }) { Icon(Icons.Default.Share, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }
            }
        }
    }
}
