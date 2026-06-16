package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.MessageEntity
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatTitle: String,
    chatAvatar: String,
    chatType: String, // "ONE_TO_ONE", "GROUP", "CHANNEL", "COMMUNITY", "HYPER_AI"
    messages: List<MessageEntity>,
    smartReplies: List<String>,
    isTyping: Boolean,
    isWallpaperEnabled: Boolean,
    onSendMessage: (text: String, replyToId: String?, replyToText: String?, type: String, attachment: String?, duration: Int, pollOpts: String?) -> Unit,
    onCallClick: (isVideo: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var rawInputText by remember { mutableStateOf("") }
    var replyingToMessageId by remember { mutableStateOf<String?>(null) }
    var replyingToMsgText by remember { mutableStateOf<String?>(null) }

    // Dialog attachments controls
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showPollCreator by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOption1 by remember { mutableStateOf("") }
    var pollOption2 by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom when messages count updates
    LaunchedEffect(key1 = messages.size, key2 = isTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { /* edit preferences */ }
                    ) {
                        AsyncImage(
                            model = chatAvatar,
                            contentDescription = "Contact photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SlateCard)
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = chatTitle,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Verified Badge for Raymond, Tucci & special business bots
                                if (chatTitle.contains("Raymond") || chatTitle.contains("Tucci") || chatTitle.contains("Elon") || chatTitle.contains("Hyper")) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified cyber status badge",
                                        tint = CyberGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isTyping) "typing secure response..." else "AES-256 standard active",
                                fontSize = 11.sp,
                                color = if (isTyping) CyberGreen else Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back icon", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onCallClick(false) }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Audio call launcher", tint = CyberGreen)
                    }
                    IconButton(onClick = { onCallClick(true) }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video call launcher", tint = CyberGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkMidnight)
            )
        },
        containerColor = DarkMidnight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Message Feed
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Mock custom wallpaper design (if toggled)
                if (isWallpaperEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(Color(0xFF102A24), DarkMidnight)
                                )
                            )
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp)
                        .testTag("chat_messages_feed"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(messages) { message ->
                        val isOutgoing = message.senderId == "self"
                        MessageBubble(
                            id = message.id,
                            senderName = message.senderName,
                            text = message.text,
                            type = message.type,
                            timestamp = message.timestamp,
                            status = message.status,
                            replyToText = message.replyToText,
                            attachments = message.attachments,
                            duration = message.voiceDuration,
                            pollOpts = message.pollOptions,
                            isOutgoing = isOutgoing,
                            onReplyClick = {
                                replyingToMessageId = message.id
                                replyingToMsgText = message.text
                            }
                        )
                    }
                }
            }

            // Quick Smart Replies Overlay
            if (rawInputText.isEmpty() && smartReplies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    smartReplies.forEach { reply ->
                        Box(
                            modifier = Modifier
                                .background(SlateCard, RoundedCornerShape(16.dp))
                                .clickable {
                                    onSendMessage(reply, null, null, "TEXT", null, 0, null)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = reply, color = CyberGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Replying Banner
            AnimatedVisibility(visible = replyingToMessageId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IncomingSlate)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Replying to:", color = CyberGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(replyingToMsgText ?: "", color = Color.LightGray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = {
                        replyingToMessageId = null
                        replyingToMsgText = null
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel reply", tint = Color.Gray)
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkMidnight)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(SlateCard, RoundedCornerShape(26.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji Keyboard launcher dummy
                    IconButton(onClick = { /* trigger keyboard emoji */ }) {
                        Icon(imageVector = Icons.Default.SentimentSatisfiedAlt, contentDescription = "Emoji picker", tint = Color.Gray)
                    }

                    // Input Text
                    TextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        placeholder = { Text("Message...", color = Color.Gray, fontSize = 15.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = CyberGreen
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    // Attachment paperclip
                    IconButton(onClick = { showAttachmentSheet = !showAttachmentSheet }) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = "Attach file", tint = Color.Gray)
                    }

                    // Camera quick capture
                    if (rawInputText.isEmpty()) {
                        IconButton(onClick = {
                            onSendMessage("Captured photo standard high-res", null, null, "IMAGE", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400", 0, null)
                        }) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Snap camera", tint = Color.Gray)
                        }
                    }
                }

                // Send FAB / Mic dummy
                FloatingActionButton(
                    onClick = {
                        if (rawInputText.isNotEmpty()) {
                            onSendMessage(rawInputText, replyingToMessageId, replyingToMsgText, "TEXT", null, 0, null)
                            rawInputText = ""
                            replyingToMessageId = null
                            replyingToMsgText = null
                        } else {
                            // Send custom 5s Voice update mock!
                            onSendMessage("Simulated Voice message", null, null, "VOICE", null, 6, null)
                        }
                    },
                    containerColor = CyberGreen,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_msg_fab")
                ) {
                    Icon(
                        imageVector = if (rawInputText.isNotEmpty()) Icons.Default.Send else Icons.Default.Mic,
                        contentDescription = "Send/Mic trigger"
                    )
                }
            }

            // Attachment list drawer
            AnimatedVisibility(visible = showAttachmentSheet) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateCard)
                        .padding(vertical = 16.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentIcon(icon = Icons.Default.Poll, text = "Poll", backgroundColor = Color(0xFF673AB7)) {
                        showPollCreator = true
                        showAttachmentSheet = false
                    }
                    AttachmentIcon(icon = Icons.Default.Description, text = "Document", backgroundColor = Color(0xFF2196F3)) {
                        onSendMessage("Hyper-secure Contract.pdf (1.2 MB)", null, null, "DOCUMENT", "SECURE_PDF", 0, null)
                        showAttachmentSheet = false
                    }
                    AttachmentIcon(icon = Icons.Default.LocationOn, text = "Location", backgroundColor = Color(0xFF4CAF50)) {
                        onSendMessage("Location: Tucci Cyber Nation Headquarters ( Kampala, Uganda )", null, null, "LOCATION", "KAMPALA", 0, null)
                        showAttachmentSheet = false
                    }
                }
            }

            // Poll creator Dialog
            if (showPollCreator) {
                AlertDialog(
                    onDismissRequest = { showPollCreator = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (pollQuestion.isNotEmpty() && pollOption1.isNotEmpty() && pollOption2.isNotEmpty()) {
                                onSendMessage(
                                    pollQuestion,
                                    null,
                                    null,
                                    "POLL",
                                    null,
                                    0,
                                    "$pollOption1,$pollOption2"
                                )
                                pollQuestion = ""
                                pollOption1 = ""
                                pollOption2 = ""
                                showPollCreator = false
                            }
                        }) {
                            Text("Create Poll", color = CyberGreen)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPollCreator = false }) { Text("Cancel", color = Color.Gray) }
                    },
                    title = { Text("Create Interactive Poll") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pollQuestion,
                                onValueChange = { pollQuestion = it },
                                label = { Text("Question") },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberGreen)
                            )
                            OutlinedTextField(
                                value = pollOption1,
                                onValueChange = { pollOption1 = it },
                                label = { Text("Option 1") },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberGreen)
                            )
                            OutlinedTextField(
                                value = pollOption2,
                                onValueChange = { pollOption2 = it },
                                label = { Text("Option 2") },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberGreen)
                            )
                        }
                    },
                    containerColor = SlateCard,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }
    }
}

@Composable
fun AttachmentIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White)
        }
        Text(text = text, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MessageBubble(
    id: String,
    senderName: String,
    text: String,
    type: String,
    timestamp: Long,
    status: String,
    replyToText: String?,
    attachments: String?,
    duration: Int,
    pollOpts: String?,
    isOutgoing: Boolean,
    onReplyClick: () -> Unit
) {
    var hasVotedOption1 by remember { mutableStateOf(false) }
    var hasVotedOption2 by remember { mutableStateOf(false) }

    val formattedTime = remember(timestamp) {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        format.format(date)
    }

    val alignment = if (isOutgoing) Alignment.End else Alignment.Start
    val containerBg = if (isOutgoing) OutgoingGreen else IncomingSlate
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReplyClick() },
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOutgoing) 16.dp else 2.dp,
                        bottomEnd = if (isOutgoing) 2.dp else 16.dp
                    )
                )
                .background(containerBg)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Sender Username if Group
                if (!isOutgoing) {
                    Text(
                        text = senderName,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                // Reply indicator nested bubble
                if (replyToText != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.28f))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text("Replied:", color = CyberGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(replyToText, color = Color.LightGray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // Render Content by Type
                when (type) {
                    "TEXT" -> {
                        Text(text = text, color = textColor, fontSize = 14.sp)
                    }
                    "IMAGE" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AsyncImage(
                                model = attachments ?: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400",
                                contentDescription = "Sent asset image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Text(text = text, color = textColor, fontSize = 13.sp)
                        }
                    }
                    "DOCUMENT" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = "Doc asset", tint = CyberGreen)
                            Column {
                                Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("SECURE AES-256 PDF | 1.2 MB", color = Color.Gray, fontSize = 9.sp)
                            }
                        }
                    }
                    "LOCATION" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Map marker", tint = CyberGreen)
                                Text("Secure Live Location", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(text, color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                    "VOICE" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { /* play audio mock sound effects */ },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CyberGreen)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play voice audio", tint = Color.Black)
                            }

                            // Waves Canvas Mockup
                            Row(
                                modifier = Modifier.width(110.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(0.4f, 0.8f, 0.5f, 0.9f, 0.3f, 0.7f, 0.4f, 0.9f, 0.5f, 0.2f, 0.8f).forEach { heightPercent ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height((24 * heightPercent).dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(if (isOutgoing) Color.White else CyberGreen)
                                    )
                                }
                            }

                            Text(
                                text = "0:0$duration",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    "POLL" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "📊 $text", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (pollOpts != null) {
                                val options = pollOpts.split(",")
                                if (options.size >= 2) {
                                    val opt1 = options[0]
                                    val opt2 = options[1]

                                    // Option 1 Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (hasVotedOption1) CyberGreen.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable {
                                                hasVotedOption1 = !hasVotedOption1
                                                if (hasVotedOption1) hasVotedOption2 = false
                                            }
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(opt1, color = Color.White, fontSize = 12.sp)
                                        Text(if (hasVotedOption1) "67% (4 votes)" else "33% (2 votes)", color = Color.Gray, fontSize = 10.sp)
                                    }

                                    // Option 2 Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(if (hasVotedOption2) CyberGreen.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
                                            .clickable {
                                                hasVotedOption2 = !hasVotedOption2
                                                if (hasVotedOption2) hasVotedOption1 = false
                                            }
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(opt2, color = Color.White, fontSize = 12.sp)
                                        Text(if (hasVotedOption2) "75% (6 votes)" else "25% (2 votes)", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Timestamp and Receipt ticks line
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = TextGray,
                        fontSize = 9.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (isOutgoing) {
                        Icon(
                            imageVector = if (status == "READ") Icons.Default.DoneAll else Icons.Default.Check,
                            contentDescription = "Receipt Status",
                            tint = if (status == "READ") CyberGreen else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
