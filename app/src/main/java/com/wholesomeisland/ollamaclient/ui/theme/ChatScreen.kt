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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.markdown.Markdown
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

    val mainBackground = Color(0xFF1A1A1A)
    val responseBackground = Color(0xFF252525)
    
    val purple = MaterialTheme.colorScheme.primary
    val lightGray = Color(0xFFBDBDBD)
    val darkGray = Color(0xFF424242)

    LaunchedEffect(state.messages.size, state.isLoading, state.isSearching) {
        if ((state.isLoading || state.isSearching) && state.messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
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

                    var urlInput by remember { mutableStateOf(state.serverUrl) }
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Server URL") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purple,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedLabelColor = purple,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            IconButton(onClick = { onChangeServerUrl(urlInput) }) {
                                Icon(Icons.Default.Check, contentDescription = "Update URL", tint = Color.White)
                            }
                        }
                    )

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
                            )
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
                            )
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
                        )
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
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
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
                            Text("Wholesome Island", color = Color.White, fontWeight = FontWeight.Bold)
                            if (state.selectedModel != null) {
                                Text(
                                    state.selectedModel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
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
                            Icon(Icons.Default.Menu, contentDescription = "Settings")
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
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF333333))
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
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                                            contentDescription = "Cancel",
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
                                            contentDescription = null,
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
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
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
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
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
                            )
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
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = if (input.isNotBlank()) MaterialTheme.colorScheme.primary else Color.DarkGray)
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = state.infoMessage ?: "", color = Color.White, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = onDismissInfo, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = state.error ?: "", color = Color(0xFFFFB4AB), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = onDismissError) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFFB4AB))
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
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = if (isUser) Color.Transparent else aiBackground,
        shape = if (isUser) RoundedCornerShape(0.dp) else RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { showMenu = true })
            }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            if (!message.reasoning.isNullOrBlank()) {
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
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
                            contentDescription = null,
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
                    RichText {
                        Markdown(content = message.text)
                    }
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Color(0xFF333333))
            ) {
                if (isUser) {
                    DropdownMenuItem(
                        text = { Text("Try Again", color = Color.White) },
                        onClick = { onTryAgain(message.text); showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Gray) }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Copy", color = Color.White) },
                    onClick = { clipboardManager.setText(AnnotatedString(message.text)); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.Gray) }
                )
                DropdownMenuItem(
                    text = { Text("Share", color = Color.White) },
                    onClick = { onShare(message.text); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.Gray) }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.White) },
                    onClick = { onDelete(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Gray) }
                )
            }
        }
    }
}
