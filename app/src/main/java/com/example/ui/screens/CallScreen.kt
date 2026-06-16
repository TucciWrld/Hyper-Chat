package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlin.random.Random

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CallScreen(
    userName: String,
    userAvatar: String,
    callType: String, // "VOICE" or "VIDEO"
    direction: String, // "INCOMING" or "OUTGOING"
    onAccept: () -> Unit,
    onEndCall: (durationSec: Int) -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOn by remember { mutableStateOf(callType == "VIDEO") }
    var isNoiseSuppressionOn by remember { mutableStateOf(true) }
    var selectedFilterName by remember { mutableStateOf("Normal") } // "Normal", "Cyber Blur", "Neon Sparkles"
    var isConnected by remember { mutableStateOf(direction == "INCOMING") } // Simulate accept or ringing
    var callTimeSec by remember { mutableStateOf(0) }

    // Ringing vs connected simulation
    LaunchedEffect(key1 = isConnected) {
        if (!isConnected) {
            delay(2000) // Ringing for 2 seconds then self connect for demo
            isConnected = true
        }
    }

    // Timer trigger
    LaunchedEffect(key1 = isConnected) {
        if (isConnected) {
            while (true) {
                delay(1000)
                callTimeSec++
            }
        }
    }

    val displayTimer = {
        val mins = callTimeSec / 60
        val secs = callTimeSec % 60
        String.format("%02d:%02d", mins, secs)
    }

    // Ambient background brush
    val backdropBrush = Brush.verticalGradient(
        colors = when (selectedFilterName) {
            "Cyber Blur" -> listOf(Color(0xFF041913), Color(0xFF05111F))
            "Neon Sparkles" -> listOf(Color(0xFF220A2F), Color(0xFF0C141A))
            else -> listOf(DarkMidnight, Color(0xFF0F1A24))
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backdropBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("calling_screen_container")
    ) {
        // Holographic Video Feed or Filter HUD overlays if Video is Active
        if (isVideoOn) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background simulated feed showing cyber textures
                AsyncImage(
                    model = if (selectedFilterName == "Normal") userAvatar else "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=400",
                    contentDescription = "Simulated feed",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Black transparent gradient on top
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                )

                // Dynamic Sparkle Floating particles mockup
                if (selectedFilterName == "Neon Sparkles") {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Magenta.copy(alpha = 0.15f))) {
                        Text("✨ Hologram Sparkles Active ✨", color = Color.Magenta, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
                    }
                } else if (selectedFilterName == "Cyber Blur") {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Cyan.copy(alpha = 0.1f))) {
                        Text("⚡ Fast Cyber Frame Enhancement active", color = Color.Cyan, fontSize = 12.sp, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }

        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = "Locked", tint = CyberGreen, modifier = Modifier.size(12.dp))
                Text("End-to-End Encrypted Call (Tucci Protocol)", color = Color.Gray, fontSize = 9.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isVideoOn) {
                // Big Avatar for Voice Calls
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                    // Pulsing Ring Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scale = 1f + (callTimeSec % 4) * 0.1f
                        drawCircle(
                            color = CyberGreen.copy(alpha = 0.15f / scale),
                            radius = size.minDimension / 2 * scale
                        )
                    }
                    AsyncImage(
                        model = userAvatar,
                        contentDescription = "Remote User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(SlateCard)
                    )
                }
            }

            Text(
                text = userName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isConnected) "Connected - ${displayTimer()}" else "Ringing secure line...",
                color = if (isConnected) CyberGreen else Color.LightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Remote user Thumbnail (Picture-In-Picture presentation)
        if (isVideoOn) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(90.dp, 130.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 24.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                        contentDescription = "Self Feed Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "You",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(CyberGreen, RoundedCornerShape(topEnd = 4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }

        // Bottom Controls HUD area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Noise reduction & Filters control row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Noise Suppression
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { isNoiseSuppressionOn = !isNoiseSuppressionOn }) {
                    IconButton(
                        onClick = { isNoiseSuppressionOn = !isNoiseSuppressionOn },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = if (isNoiseSuppressionOn) CyberGreen else SlateCard)
                    ) {
                        Icon(
                            imageVector = if (isNoiseSuppressionOn) Icons.Default.HearingDisabled else Icons.Default.Hearing,
                            contentDescription = "Noise cancellation",
                            tint = if (isNoiseSuppressionOn) Color.Black else Color.White
                        )
                    }
                    Text(text = if (isNoiseSuppressionOn) "Noise Suppress On" else "Noise Standard", color = Color.Gray, fontSize = 10.sp)
                }

                // Video filter choices selector trigger
                if (isVideoOn) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Normal", "Cyber Blur", "Neon Sparkles").forEach { filter ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (selectedFilterName == filter) CyberGreen else SlateCard,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedFilterName = filter }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        color = if (selectedFilterName == filter) Color.Black else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(text = "Holographic Call Filters", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

            // Main Core Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute
                IconButton(
                    onClick = { isMuted = !isMuted },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = if (isMuted) Color.Red else SlateCard),
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }

                // Hang Up (RED button)
                IconButton(
                    onClick = { onEndCall(callTimeSec) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("hangup_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call Connection",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Switch Camera or call video mode toggle
                IconButton(
                    onClick = { isVideoOn = !isVideoOn },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = if (isVideoOn) CyberGreen else SlateCard),
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(
                        imageVector = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Video camera Toggle",
                        tint = if (isVideoOn) Color.Black else Color.White
                    )
                }
            }
        }
    }
}
