package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkMidnight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateCard
import com.example.ui.viewmodel.HyperViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: HyperViewModel = viewModel()
                HyperChatAppContainer(viewModel)
            }
        }
    }
}

@Composable
fun HyperChatAppContainer(viewModel: HyperViewModel) {
    // Collect UI state from ViewModel
    val currentTab by viewModel.currentTab.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val activeStatus by viewModel.activeStatus.collectAsState()
    val showNewChatDialog by viewModel.showNewChatDialog.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userAvatar by viewModel.userAvatar.collectAsState()
    val isFingerprintEnabled by viewModel.isFingerprintEnabled.collectAsState()
    val isPasscodeEnabled by viewModel.isPasscodeEnabled.collectAsState()
    val disappearingDuration by viewModel.disappearingMessagesDuration.collectAsState()
    val isWallpaperActive by viewModel.isCustomWallpaperActive.collectAsState()
    val accentColorName by viewModel.accentColorName.collectAsState()
    val storageSizeMb by viewModel.storageSizeMb.collectAsState()

    val chats by viewModel.chats.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())
    val calls by viewModel.calls.collectAsState(initial = emptyList())
    val statuses by viewModel.statuses.collectAsState(initial = emptyList())
    val activeMessages by viewModel.activeChatMessages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val smartReplies by viewModel.smartReplies.collectAsState()

    // Screen State flow helpers
    var hasUnlockedSecurityByPass by rememberSaveable { mutableStateOf(false) }
    var showOnboarding by rememberSaveable { mutableStateOf(true) }
    var isShowingSettingsScreen by remember { mutableStateOf(false) }

    // Biometric scanner simulated ripple
    var biometricScanRipple by remember { mutableStateOf(false) }

    // Start Screen: Biometric / Passcode Lock Overlay Shield
    val isAppLocked = (isFingerprintEnabled || isPasscodeEnabled) && !hasUnlockedSecurityByPass

    if (isAppLocked) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_shield_lock_overlay"),
            color = DarkMidnight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header details
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield Active",
                        tint = CyberGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Hyper Chat Enforced Security",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AES-256 military-grade protection active",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Center click scanner representation
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(if (biometricScanRipple) CyberGreen.copy(alpha = 0.3f) else SlateCard)
                            .clickable {
                                biometricScanRipple = true
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Tap to Scan preset token",
                            tint = if (biometricScanRipple) CyberGreen else Color.LightGray,
                            modifier = Modifier.size(72.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (biometricScanRipple) "Decrypting secure hashes..." else "Scan Fingerprint to Unlock App",
                        color = if (biometricScanRipple) CyberGreen else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Launched effect for bypass simulation
                if (biometricScanRipple) {
                    LaunchedEffect(key1 = true) {
                        delay(1200)
                        hasUnlockedSecurityByPass = true
                        biometricScanRipple = false
                    }
                }

                // Help text
                Text(
                    text = "Tucci Cyber Nation & Kawooya Raymond Encryption Standard",
                    color = Color.DarkGray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    } else if (showOnboarding) {
        // Step Onboarding Form (Phone OTP verified selection preset)
        OnboardingScreen(
            currentName = userName,
            currentAvatar = userAvatar,
            onComplete = { name, avatar ->
                viewModel.updateProfile(name, avatar)
                showOnboarding = false
            }
        )
    } else if (isShowingSettingsScreen) {
        // Whole screen Settings overlay
        SettingsScreen(
            currentName = userName,
            currentAvatar = userAvatar,
            isFingerprint = isFingerprintEnabled,
            isPasscode = isPasscodeEnabled,
            disappearingMins = disappearingDuration,
            isWallpaper = isWallpaperActive,
            accentName = accentColorName,
            storageMb = storageSizeMb,
            onProfileUpdate = { name, avatar -> viewModel.updateProfile(name, avatar) },
            onFingerprintChange = { viewModel.toggleFingerprint(it) },
            onPasscodeChange = { viewModel.togglePasscode(it) },
            onDisappearingChange = { viewModel.setDisappearingMessages(it) },
            onWallpaperChange = { viewModel.setWallpaperActive(it) },
            onAccentChange = { viewModel.setAccentColor(it) },
            onClearCache = { viewModel.clearCache() },
            onBack = { isShowingSettingsScreen = false }
        )
    } else {
        // App workspace loaded with transitions
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(targetState = activeChatId, label = "active_pane_transition") { activeId ->
                if (activeId != null) {
                    // Render Chat Details Page
                    val activeChat = chats.find { it.id == activeId }
                    if (activeChat != null) {
                        ChatScreen(
                            chatTitle = activeChat.title,
                            chatAvatar = activeChat.avatarUrl,
                            chatType = activeChat.type,
                            messages = activeMessages,
                            smartReplies = smartReplies,
                            isTyping = isTyping,
                            isWallpaperEnabled = isWallpaperActive,
                            onSendMessage = { text, replyToId, replyToText, type, attachment, duration, pollOpts ->
                                viewModel.sendMessage(text, type, replyToId, replyToText, attachment, duration, false, pollOpts)
                            },
                            onCallClick = { isVideo ->
                                viewModel.startCall(activeChat.id.replace("chat_", ""), isVideo)
                            },
                            onBack = { viewModel.selectChat(null) }
                        )
                    } else {
                        viewModel.selectChat(null)
                    }
                } else {
                    // Render Dashboard workspace tabs (Chats, Updates/Statuses, Communities, Calls)
                    MainScreen(
                        currentTab = currentTab,
                        chats = chats,
                        users = users,
                        calls = calls,
                        statuses = statuses,
                        searchQuery = searchQuery,
                        onTabSelected = { viewModel.setTab(it) },
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onSelectChat = { viewModel.selectChat(it) },
                        onStartCall = { url, isVid -> viewModel.startCall(url, isVid) },
                        onPostStatus = { text, img, type -> viewModel.postStatus(text, img, type) },
                        onViewStatus = { viewModel.viewStatusItem(it) },
                        onCreateChatWithUser = { viewModel.createChatWithUser(it) },
                        onCreateGroup = { nameList, userIds -> viewModel.createGroupChat(nameList, userIds) },
                        onClearCallHistory = { viewModel.clearCallHistory() },
                        onDeleteChat = { viewModel.deleteChatById(it) },
                        onSettingsClick = { isShowingSettingsScreen = true },
                        onTriggerNewChatDialog = { viewModel.toggleNewChatDialog(it) },
                        showNewChatDialog = showNewChatDialog
                    )
                }
            }

            // FULLSCREEN SECURE IMMERSIVE CALL MODAL (Audio & Video)
            AnimatedVisibility(
                visible = activeCall != null,
                enter = slideInVertically { height -> height } + fadeIn(),
                exit = slideOutVertically { height -> height } + fadeOut()
            ) {
                activeCall?.let { call ->
                    CallScreen(
                        userName = call.userName,
                        userAvatar = call.userAvatar,
                        callType = call.type,
                        direction = call.direction,
                        onAccept = { viewModel.acceptCall() },
                        onEndCall = { dur -> viewModel.endCall(dur) }
                    )
                }
            }

            // FULLSCREEN IMMERSIVE STATUS STORY VIEW
            AnimatedVisibility(
                visible = activeStatus != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeStatus?.let { status ->
                    StatusDetailScreen(
                        statusId = status.id,
                        userName = status.userName,
                        userAvatar = status.userAvatar,
                        mediaUrl = status.mediaUrl,
                        text = status.text,
                        type = status.type,
                        timestamp = "Posted Today, 2:40 PM",
                        viewers = status.viewers,
                        reactions = status.reactions,
                        onReactionAdded = { emoji -> viewModel.addStatusReaction(status.id, emoji) },
                        onClose = { viewModel.viewStatusItem(null) }
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
