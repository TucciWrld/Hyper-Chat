package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun SettingsScreen(
    currentName: String,
    currentAvatar: String,
    isFingerprint: Boolean,
    isPasscode: Boolean,
    disappearingMins: Int,
    isWallpaper: Boolean,
    accentName: String,
    storageMb: Double,
    onProfileUpdate: (name: String, avatar: String) -> Unit,
    onFingerprintChange: (Boolean) -> Unit,
    onPasscodeChange: (Boolean) -> Unit,
    onDisappearingChange: (Int) -> Unit,
    onWallpaperChange: (Boolean) -> Unit,
    onAccentChange: (String) -> Unit,
    onClearCache: () -> Unit,
    onBack: () -> Unit
) {
    var isEditingProfile by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(currentName) }
    var editAvatar by remember { mutableStateOf(currentAvatar) }
    var isClearingCacheSimulated by remember { mutableStateOf(false) }
    var showSuccessClearDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy Safeguards", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_card_box")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AsyncImage(
                            model = currentAvatar,
                            contentDescription = "My Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = currentName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Secure Profile Session Active", color = CyberGreen, fontSize = 12.sp)
                        }
                        IconButton(onClick = { isEditingProfile = !isEditingProfile }) {
                            Icon(imageVector = if (isEditingProfile) Icons.Default.Close else Icons.Default.Edit, contentDescription = "Edit Profile Toggle", tint = CyberGreen)
                        }
                    }

                    // Profile Editor expandable panel
                    if (isEditingProfile) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Your Username", color = Color.Gray) },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text),
                                modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberGreen,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = CyberGreen,
                                    unfocusedLabelColor = Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = editAvatar,
                                onValueChange = { editAvatar = it },
                                label = { Text("Avatar Photo URL", color = Color.Gray) },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                modifier = Modifier.fillMaxWidth().testTag("edit_avatar_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberGreen,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = CyberGreen,
                                    unfocusedLabelColor = Color.Gray
                                )
                            )

                            Button(
                                onClick = {
                                    if (editName.isNotBlank() && editAvatar.isNotBlank()) {
                                        onProfileUpdate(editName, editAvatar)
                                        isEditingProfile = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("save_profile_btn")
                            ) {
                                Text("Commit Profile Crypt Details 🔑", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Storage Analyzer Card
            Text(text = "Storage & Cache Manager", color = CyberGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("storage_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.SdStorage, contentDescription = "Storage", tint = CyberGreen)
                            Column {
                                Text("Disk Usage Analyzer", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Text("Media, downloads, chats database", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Text(text = "${String.format("%.1f", storageMb)} MB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    LinearProgressIndicator(
                        progress = if (storageMb > 10.0) 0.65f else 0.05f,
                        color = CyberGreen,
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PieChart, contentDescription = "Breakdown", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(
                            text = if (storageMb > 10) "Breakdown: GIFs & Stickers (142MB) | Videos (112MB) | DB (88MB)" else "Breakdown: App Database Cache Only (4.2MB)",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            isClearingCacheSimulated = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("clear_cache_btn"),
                        enabled = !isClearingCacheSimulated && storageMb > 10.0
                    ) {
                        if (isClearingCacheSimulated) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Purging Large Cached Media... Please Hold")
                        } else {
                            Text(if (storageMb > 10) "Deep Clean Large Cache Files" else "Database Already Optimal")
                        }
                    }
                }
            }

            // Simulated active cache clearance side-effect
            LaunchedEffect(key1 = isClearingCacheSimulated) {
                if (isClearingCacheSimulated) {
                    delay(2000)
                    onClearCache()
                    isClearingCacheSimulated = false
                    showSuccessClearDialog = true
                }
            }

            if (showSuccessClearDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessClearDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showSuccessClearDialog = false }) {
                            Text("Awesome", color = CyberGreen)
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clean", tint = CyberGreen) },
                    title = { Text("Cache Deep Purged") },
                    text = { Text("Hyper Chat deleted 337.9 MB of redundant sticker caches, video frames, and diagnostic logs. Database performance optimized!") },
                    containerColor = SlateCard,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }

            // Privacy & Customization Card
            Text(text = "Privacy & Aesthetic Locks", color = CyberGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Fingerprint lock toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = "Fingerprint", tint = CyberGreen)
                            Column {
                                Text("Biometric Lock (Fingerprint)", color = Color.White)
                                Text("Require touch confirmation on startup", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = isFingerprint,
                            onCheckedChange = onFingerprintChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = CyberGreen.copy(alpha = 0.4f))
                        )
                    }

                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                    // Passcode toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = CyberGreen)
                            Column {
                                Text("Numeric Passcode Lock Shield", color = Color.White)
                                Text("Enforces passcode on cold boots", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = isPasscode,
                            onCheckedChange = onPasscodeChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = CyberGreen.copy(alpha = 0.4f))
                        )
                    }

                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                    // Disappearing duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Disappearing", tint = CyberGreen)
                            Column {
                                Text("Disappearing Chat Cycles", color = Color.White)
                                Text("Self destruct incoming media files", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        // Slider or buttons for disappearing
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(0, 5, 60).forEach { mins ->
                                Button(
                                    onClick = { onDisappearingChange(mins) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (disappearingMins == mins) CyberGreen else SlateCard,
                                        contentColor = if (disappearingMins == mins) Color.Black else Color.White
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text(text = if (mins == 0) "Off" else "${mins}m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                    // Accent customizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = "Accent", tint = CyberGreen)
                            Column {
                                Text("Brand Brand Accent Color", color = Color.White)
                                Text("Currently: $accentName", color = CyberGreen, fontSize = 11.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(CyberGreen)
                                    .clickable { onAccentChange("Emerald Green") }
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF20C997))
                                    .clickable { onAccentChange("Mint Blue") }
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9800))
                                    .clickable { onAccentChange("Warm Amber") }
                            )
                        }
                    }

                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                    // Custom Wallpaper customizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Default.Wallpaper, contentDescription = "Wallpaper", tint = CyberGreen)
                            Column {
                                Text("Chat Custom Wallpapers", color = Color.White)
                                Text("Abstract aesthetic backdrop in chats", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = isWallpaper,
                            onCheckedChange = onWallpaperChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = CyberGreen.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // Developer Glowing Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Terminal, contentDescription = "Terminal", tint = CyberGreen, modifier = Modifier.size(36.dp))
                    Text(
                        text = "HYPER CHAT SYSTEM ENGINE",
                        color = CyberGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "This application is custom Engineered and Developed natively by",
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tucci Cyber Nation\n&\nKawooya Raymond",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Secure AES-256 Storage Engine enabled | SQLite Room DB active | Direct Gemini REST Endpoint enabled\n© 2026. All Security Keys Reserved.",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
