package com.wholesomeisland.ollamaclient.ui.theme

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.CodeBlockStyle
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle
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
    onSetVerbosity: (VerbosityLevel) -> Unit,
    onQuickAction: (QuickAction) -> Unit,
    onTryAgain: (String) -> Unit,
    onDismissInfo: () -> Unit,
    onAddSearchEngine: (String, String, String, String, String) -> Unit,
    onRemoveSearchEngine: (String) -> Unit,
    onSelectSearchEngine: (String) -> Unit,
    onRetryConnection: () -> Unit = {},
    onDiscoverServers: () -> Unit = {},
    onNewChat: () -> Unit = {},
    onSelectContext: (String) -> Unit = {},
    onDeleteContext: (String) -> Unit = {},
    onAddPrompt: (String, String) -> Unit = { _, _ -> },
    onUpdatePrompt: (String, String, String) -> Unit = { _, _, _ -> },
    onDeletePrompt: (String) -> Unit = {},
    onSelectPrompt: (String) -> Unit = {}
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

    var showSettingsSheet by remember { mutableStateOf(false) }
    var showAddEngineDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var termsText by remember { mutableStateOf("Loading...") }
    
    var showPromptEditDialog by remember { mutableStateOf<PromptConfig?>(null) }
    var showAddPromptDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

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

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF222222)
        ) {
            SettingsContent(
                state = state,
                onUpdateConnection = onUpdateConnection,
                onDiscoverServers = onDiscoverServers,
                onAddSearchEngine = { showAddEngineDialog = true },
                onRemoveSearchEngine = onRemoveSearchEngine,
                onSelectSearchEngine = onSelectSearchEngine,
                onToggleWebSearch = onToggleWebSearch,
                onToggleStreaming = onToggleStreaming,
                onAddPrompt = { showAddPromptDialog = true },
                onEditPrompt = { showPromptEditDialog = it },
                onDeletePrompt = onDeletePrompt,
                onSelectPrompt = onSelectPrompt,
                purple = purple
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF222222)) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(16.dp)
                ) {
                    Text("Chat History", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onNewChat(); scope.launch { drawerState.close() } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = purple)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("New Chat")
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
                        state.contexts.sortedByDescending { it.lastUpdated }.forEach { chatContext ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (state.currentContextId == chatContext.id) Color.White.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { onSelectContext(chatContext.id); scope.launch { drawerState.close() } }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = chatContext.name,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(onClick = { onDeleteContext(chatContext.id) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(Modifier.height(16.dp))
                    
                    Text("Support & Legal", style = MaterialTheme.typography.labelLarge, color = purple)
                    Spacer(Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text("Terms & Privacy") },
                        selected = false,
                        onClick = { showTermsDialog = true },
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray) }
                    )
                    NavigationDrawerItem(
                        label = { Text("GitHub Repository") },
                        selected = false,
                        onClick = { uriHandler.openUri("https://github.com/rpowell5064/wholesome-island-ollama-client/tree/master") },
                        icon = { Icon(Icons.Default.Code, contentDescription = null, tint = Color.Gray) }
                    )
                    NavigationDrawerItem(
                        label = { Text("Buy Me a Coffee") },
                        selected = false,
                        onClick = { uriHandler.openUri("https://buymeacoffee.com/rpowell5064") },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFFF4081)) }
                    )
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Wholesome Island 🌴", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.width(8.dp))
                                // Connection Status Dot
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .clickable { onRetryConnection() }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (state.isServerHealthy) {
                                                    true -> Color.Green
                                                    false -> Color.Red
                                                    else -> Color.Gray
                                                }
                                            )
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = when (state.isServerHealthy) {
                                            true -> "Connected"
                                            false -> "Error"
                                            else -> "Connecting..."
                                        },
                                        color = when (state.isServerHealthy) {
                                            true -> Color.Green.copy(alpha = 0.8f)
                                            false -> Color.Red.copy(alpha = 0.8f)
                                            else -> Color.Gray
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = mainBackground),
                    navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = Color.White) } },
                    actions = {
                        IconButton(onClick = { showSettingsSheet = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    
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

                    // Quick Actions
                    if (state.messages.any { it.role == "assistant" } && !state.isLoading) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.quickActions) { action ->
                                AssistChip(
                                    onClick = { onQuickAction(action) },
                                    label = { Text(action.label, color = Color.White, style = MaterialTheme.typography.labelMedium) },
                                    leadingIcon = {
                                        val icon = when (action.icon) {
                                            "summarize" -> Icons.Default.AutoAwesome
                                            "list" -> Icons.AutoMirrored.Filled.FormatListBulleted
                                            "help" -> Icons.Default.Lightbulb
                                            else -> Icons.Default.Bolt
                                        }
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = purple)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    border = null,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    if (state.attachedImagesBase64.isNotEmpty()) {
                        LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            items(state.attachedImagesBase64) { base64 ->
                                val bitmap = remember(base64) {
                                    val decodedString = Base64.decode(base64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                }
                                Box(modifier = Modifier.padding(end = 8.dp)) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = onClearAttachedImages,
                                        modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    ) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                                }
                            }
                        }
                    }

                    // Integrated Model and Verbosity Controls
                    ChatControlsRow(state, onSelectModel, onSetVerbosity, purple)

                    var input by remember { mutableStateOf("") }
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPickImage) { Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray) }
                        TextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask anything...", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        IconButton(onClick = { if (input.isNotBlank() || state.attachedImagesBase64.isNotEmpty()) { onSend(input.trim()); input = ""; keyboardController?.hide(); focusManager.clearFocus() } }, enabled = !state.isLoading) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = if (input.isNotBlank() || state.attachedImagesBase64.isNotEmpty()) purple else Color.DarkGray)
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
    
    if (showAddPromptDialog) {
        var name by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPromptDialog = false },
            title = { Text("Add System Prompt") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Prompt Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content, 
                        onValueChange = { content = it }, 
                        label = { Text("Prompt Content") }, 
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                        minLines = 8
                    )
                }
            },
            confirmButton = {
                Button(onClick = { if (name.isNotBlank() && content.isNotBlank()) { onAddPrompt(name, content); showAddPromptDialog = false } }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddPromptDialog = false }) { Text("Cancel") } }
        )
    }

    showPromptEditDialog?.let { prompt ->
        val isReadOnly = !prompt.isDeletable
        var name by remember { mutableStateOf(prompt.name) }
        var content by remember { mutableStateOf(prompt.content) }
        AlertDialog(
            onDismissRequest = { showPromptEditDialog = null },
            title = { Text(if (isReadOnly) "View System Prompt" else "Edit System Prompt") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("Prompt Name") }, 
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = isReadOnly
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content, 
                        onValueChange = { content = it }, 
                        label = { Text("Prompt Content") }, 
                        modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                        minLines = 8,
                        readOnly = isReadOnly
                    )
                }
            },
            confirmButton = {
                if (!isReadOnly) {
                    Button(onClick = { if (name.isNotBlank() && content.isNotBlank()) { onUpdatePrompt(prompt.id, name, content); showPromptEditDialog = null } }) { Text("Save") }
                } else {
                    Button(onClick = { showPromptEditDialog = null }) { Text("Close") }
                }
            },
            dismissButton = {
                if (!isReadOnly) {
                    TextButton(onClick = { showPromptEditDialog = null }) { Text("Cancel") }
                }
            }
        )
    }
}

@Composable
fun ChatControlsRow(
    state: ChatUiState,
    onSelectModel: (String) -> Unit,
    onSetVerbosity: (VerbosityLevel) -> Unit,
    accentColor: Color
) {
    var showModelDropdown by remember { mutableStateOf(false) }
    var showVerbosityDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Model Selection Dropdown
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { showModelDropdown = true },
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = state.selectedModel ?: "Select Model",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            DropdownMenu(
                expanded = showModelDropdown,
                onDismissRequest = { showModelDropdown = false },
                modifier = Modifier.background(Color(0xFF333333))
            ) {
                state.availableModels.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model, color = Color.White, style = MaterialTheme.typography.bodySmall) },
                        onClick = { onSelectModel(model); showModelDropdown = false }
                    )
                }
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Verbosity Selection Dropdown
        Box(modifier = Modifier.weight(1f)) {
            Surface(
                onClick = { showVerbosityDropdown = true },
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = state.verbosity.label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            DropdownMenu(
                expanded = showVerbosityDropdown,
                onDismissRequest = { showVerbosityDropdown = false },
                modifier = Modifier.background(Color(0xFF333333))
            ) {
                VerbosityLevel.values().forEach { level ->
                    DropdownMenuItem(
                        text = { 
                            Column {
                                Text(level.label, color = Color.White, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = when(level) {
                                        VerbosityLevel.CONCISE -> "Short & snappy"
                                        VerbosityLevel.BALANCED -> "Standard response"
                                        VerbosityLevel.DETAILED -> "Deep & thorough"
                                    },
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            }
                        },
                        onClick = { onSetVerbosity(level); showVerbosityDropdown = false }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsContent(
    state: ChatUiState,
    onUpdateConnection: (String, String, String) -> Unit,
    onDiscoverServers: () -> Unit,
    onAddSearchEngine: () -> Unit,
    onRemoveSearchEngine: (String) -> Unit,
    onSelectSearchEngine: (String) -> Unit,
    onToggleWebSearch: (Boolean) -> Unit,
    onToggleStreaming: (Boolean) -> Unit,
    onAddPrompt: () -> Unit,
    onEditPrompt: (PromptConfig) -> Unit,
    onDeletePrompt: (String) -> Unit,
    onSelectPrompt: (String) -> Unit,
    purple: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Ollama Server", style = MaterialTheme.typography.labelLarge, color = purple)
        var urlInput by remember { mutableStateOf(state.serverUrl) }
        var portInput by remember { mutableStateOf(state.serverPort) }
        var apiKeyInput by remember { mutableStateOf(state.apiKey) }
        
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Server URL") },
            placeholder = { Text("http://0.0.0.0", color = Color.Gray.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        OutlinedTextField(
            value = portInput,
            onValueChange = { portInput = it },
            label = { Text("Port") },
            placeholder = { Text("11434", color = Color.Gray.copy(alpha = 0.5f)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            label = { Text("API Key (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onUpdateConnection(urlInput, portInput, apiKeyInput) }, modifier = Modifier.fillMaxWidth()) { Text("Update Connection") }

        Spacer(Modifier.height(16.dp))
        
        if (state.isDiscovering) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Scanning Wi-Fi...", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            OutlinedButton(onClick = onDiscoverServers, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Find Servers on LAN")
            }
        }
        
        state.discoveredIps.forEach { ip ->
            TextButton(
                onClick = { onUpdateConnection(ip, "11434", apiKeyInput) },
                modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Green)
                    Spacer(Modifier.width(8.dp))
                    Text("Use Found Server: $ip", color = purple, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("System Prompts", style = MaterialTheme.typography.labelLarge, color = purple, modifier = Modifier.weight(1f))
            IconButton(onClick = onAddPrompt) { Icon(Icons.Default.Add, contentDescription = "Add prompt", tint = purple) }
        }
        state.customPrompts.forEach { prompt ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onSelectPrompt(prompt.id) }.padding(vertical = 4.dp)) {
                RadioButton(selected = state.selectedPromptId == prompt.id, onClick = { onSelectPrompt(prompt.id) })
                Column(modifier = Modifier.weight(1f)) {
                    Text(prompt.name, color = if (state.selectedPromptId == prompt.id) Color.White else Color.Gray)
                    Text(prompt.content, color = Color.Gray, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { onEditPrompt(prompt) }) { 
                    Icon(
                        if (prompt.isDeletable) Icons.Default.Edit else Icons.Default.Visibility, 
                        contentDescription = if (prompt.isDeletable) "Edit" else "View", 
                        tint = Color.Gray, 
                        modifier = Modifier.size(20.dp)
                    ) 
                }
                if (prompt.isDeletable) {
                    IconButton(onClick = { onDeletePrompt(prompt.id) }) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(20.dp)) }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Web Search", modifier = Modifier.weight(1f), color = Color.White)
            Switch(checked = state.isWebSearchEnabled, onCheckedChange = onToggleWebSearch)
        }

        if (state.isWebSearchEnabled) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Search Engines", style = MaterialTheme.typography.labelLarge, color = purple, modifier = Modifier.weight(1f))
                IconButton(onClick = onAddSearchEngine) { Icon(Icons.Default.Add, contentDescription = "Add search engine", tint = purple) }
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
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Streaming", modifier = Modifier.weight(1f), color = Color.White)
            Switch(checked = state.isStreamingEnabled, onCheckedChange = onToggleStreaming)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ResponseBlock(message: ChatUiMessage, onDelete: () -> Unit, onShare: (String) -> Unit, onTryAgain: (String) -> Unit, aiBackground: Color, accentColor: Color) {
    val isUser = message.role == "user"
    val clipboardManager = LocalClipboardManager.current
    
    // Don't show empty assistant message boxes
    if (message.role == "assistant" && message.text.isEmpty() && message.reasoning.isNullOrEmpty()) {
        return
    }
    
    Surface(color = if (isUser) Color.Transparent else aiBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isUser && message.imagesBase64.isNotEmpty()) {
                LazyRow(modifier = Modifier.padding(bottom = 12.dp)) {
                    items(message.imagesBase64) { base64 ->
                        val bitmap = remember(base64) {
                            val decodedString = Base64.decode(base64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            if (!message.reasoning.isNullOrBlank()) {
                val displayReasoning = remember(message.reasoning) {
                    if (message.reasoning.length > 2000) message.reasoning.take(2000) + "... (truncated)" else message.reasoning
                }
                Surface(color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Thought", style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Bold)
                        Text(displayReasoning, color = Color.LightGray.copy(alpha = 0.8f), fontStyle = FontStyle.Italic)
                    }
                }
            }
            if (isUser) {
                SelectionContainer {
                    Text(message.text, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                if (message.text.isNotEmpty()) {
                    AssistantMessageContent(message.text, accentColor)
                }
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

@Composable
private fun AssistantMessageContent(text: String, accentColor: Color) {
    val imageRegex = remember { Regex("<startImage>(.*?)<endImage>", RegexOption.DOT_MATCHES_ALL) }
    
    val parts = remember(text) {
        val result = mutableListOf<MessagePart>()
        var lastIndex = 0
        imageRegex.findAll(text).forEach { match ->
            if (match.range.first > lastIndex) {
                result.add(MessagePart.Text(text.substring(lastIndex, match.range.first)))
            }
            result.add(MessagePart.Image(match.groupValues[1].trim()))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < text.length) {
            result.add(MessagePart.Text(text.substring(lastIndex)))
        }
        result
    }

    Column {
        parts.forEach { part ->
            when (part) {
                is MessagePart.Text -> {
                    if (part.content.isNotBlank()) {
                        val displayContent = remember(part.content) {
                            if (part.content.length > 10000) part.content.take(10000) + "... (truncated)" else part.content
                        }
                        RichText(
                            style = RichTextStyle(
                                stringStyle = RichTextStringStyle(
                                    linkStyle = SpanStyle(
                                        color = Color(0xFF64B5F6), // Readable light blue for dark theme
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ),
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
                            Markdown(displayContent)
                        }
                    }
                }
                is MessagePart.Image -> {
                    val bitmap = remember(part.base64) {
                        try {
                            val decodedString = Base64.decode(part.base64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Generated Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

private sealed class MessagePart {
    data class Text(val content: String) : MessagePart()
    data class Image(val base64: String) : MessagePart()
}
