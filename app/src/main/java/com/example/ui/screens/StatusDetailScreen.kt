package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkMidnight
import com.example.ui.theme.SlateCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDetailScreen(
    statusId: String,
    userName: String,
    userAvatar: String,
    mediaUrl: String?,
    text: String?,
    type: String, // "TEXT" or "IMAGE"
    timestamp: String,
    viewers: String,
    reactions: String,
    onReactionAdded: (emoji: String) -> Unit,
    onClose: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var currentReplyText by remember { mutableStateOf("") }
    val isImage = !mediaUrl.isNullOrEmpty() || type == "IMAGE"

    // Increment progress to auto close story after 5s
    LaunchedEffect(key1 = statusId) {
        progress = 0f
        while (progress < 1.0f) {
            delay(50)
            progress += 0.01f
        }
        onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkMidnight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("status_story_viewer")
    ) {
        // Main Story Canvas
        if (isImage) {
            AsyncImage(
                model = mediaUrl,
                contentDescription = "Status Story Graphic",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // dark shadow cover
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.6f))))
            )
        } else {
            // Text status with abstract gradient backplate
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF0F1A24), Color(0xFF074D3D)))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text ?: "",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        // Overlay Text on Top of Image Status
        if (isImage && !text.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // PROGRESS BARS AT THE TOP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LinearProgressIndicator(
                progress = progress,
                color = CyberGreen,
                trackColor = Color.DarkGray.copy(alpha = 0.5f),
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }

        // HEADER ROW (User Details)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 28.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = userAvatar,
                    contentDescription = "Status poster avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateCard)
                )
                Column {
                    Text(text = userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = timestamp, color = Color.LightGray, fontSize = 11.sp)
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.testTag("close_status_btn")) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close View", tint = Color.White)
            }
        }

        // BOTTOM HUD (Reply Bar + Reactions viewer)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Reaction summary
            if (reactions.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Reactions:", color = Color.Gray, fontSize = 10.sp)
                    reactions.split(",").forEach { emoji ->
                        Text(text = emoji, fontSize = 14.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Outlined input
                OutlinedTextField(
                    value = currentReplyText,
                    onValueChange = { currentReplyText = it },
                    placeholder = { Text("Reply message...", color = Color.Gray, fontSize = 14.sp) },
                    textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGreen,
                        unfocusedBorderColor = Color.DarkGray,
                        focusedLabelColor = CyberGreen,
                        cursorColor = CyberGreen
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("status_reply_input"),
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        if (currentReplyText.isNotEmpty()) {
                            IconButton(onClick = {
                                onReactionAdded("💬")
                                currentReplyText = ""
                            }) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = CyberGreen)
                            }
                        }
                    }
                )

                // Quick emoji taps
                listOf("❤️", "🔥", "😂", "👏").forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SlateCard)
                            .clickable { onReactionAdded(emoji) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 18.sp)
                    }
                }
            }

            // Viewers summary
            if (viewers.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = "Views", tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Text(text = "Seen by: $viewers", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
