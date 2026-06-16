package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.CallEntity
import com.example.data.db.ChatEntity
import com.example.data.db.StatusEntity
import com.example.data.db.UserEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentTab: Int,
    chats: List<ChatEntity>,
    users: List<UserEntity>,
    calls: List<CallEntity>,
    statuses: List<StatusEntity>,
    searchQuery: String,
    onTabSelected: (Int) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectChat: (String?) -> Unit,
    onStartCall: (userId: String, isVideo: Boolean) -> Unit,
    onPostStatus: (text: String?, img: String?, type: String) -> Unit,
    onViewStatus: (StatusEntity) -> Unit,
    onCreateChatWithUser: (UserEntity) -> Unit,
    onCreateGroup: (title: String, userIds: List<String>) -> Unit,
    onClearCallHistory: () -> Unit,
    onDeleteChat: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onTriggerNewChatDialog: (Boolean) -> Unit,
    showNewChatDialog: Boolean
) {
    val scope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    var showGroupCreator by remember { mutableStateOf(false) }
    var groupTitle by remember { mutableStateOf("") }
    val selectedGroupMembers = remember { mutableStateListOf<String>() }

    // Onboarding welcome badge trigger
    var showWelcomeBanner by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(4000)
        showWelcomeBanner = false
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(DarkMidnight)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "TUCCI CYBER NATION",
                                color = CyberGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hyper Chat",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 28.sp,
                                modifier = Modifier.testTag("app_title")
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkMidnight),
                    actions = {
                        // Search trigger
                        IconButton(onClick = { /* already focused/integrated */ }) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.LightGray)
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More menu", tint = Color.LightGray)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SlateCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Group Creator", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    showGroupCreator = true
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.GroupAdd, contentDescription = "Add Gp", tint = CyberGreen) }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All Call Log", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onClearCallHistory()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear call", tint = Color.Red) }
                            )
                            DropdownMenuItem(
                                text = { Text("App Preferences", color = Color.White) },
                                onClick = {
                                    showMenu = false
                                    onSettingsClick()
                                },
                                leadingIcon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = CyberGreen) }
                            )
                        }
                    }
                )

                // Inline Search Text Field (Modern Material 3 Minimal Design)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search conversations...", color = Color.Gray, fontSize = 14.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "SearchIcon", tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x0DFFFFFF),
                        unfocusedContainerColor = Color(0x0DFFFFFF),
                        disabledContainerColor = Color(0x0DFFFFFF),
                        focusedBorderColor = Color(0x33FFFFFF),
                        unfocusedBorderColor = Color(0x1AFFFFFF),
                        cursorColor = CyberGreen
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("global_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF111111),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = Color(0x0DFFFFFF), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .navigationBarsPadding()
            ) {
                // Chats
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { onTabSelected(0) },
                    icon = {
                        Box {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "Chats")
                            val unreadTotal = chats.sumOf { it.unreadCount }
                            if (unreadTotal > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(CyberGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = unreadTotal.toString(), color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    label = { Text("Chats", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberGreen,
                        indicatorColor = CyberGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                // Updates
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { onTabSelected(1) },
                    icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Updates") },
                    label = { Text("Updates", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberGreen,
                        indicatorColor = CyberGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                // Spaces
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { onTabSelected(2) },
                    icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = "Spaces") },
                    label = { Text("Spaces", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberGreen,
                        indicatorColor = CyberGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                // Calls
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { onTabSelected(3) },
                    icon = { Icon(imageVector = Icons.Default.Call, contentDescription = "Calls") },
                    label = { Text("Calls", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberGreen,
                        indicatorColor = CyberGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0) {
                // Chats Screen FAB (Launch Chat creation chooser)
                FloatingActionButton(
                    onClick = { onTriggerNewChatDialog(true) },
                    containerColor = CyberGreen,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.testTag("launcher_new_chat_fab")
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = "New Interactive Chat")
                }
            } else if (currentTab == 1) {
                // Updates Screen FAB (Add dynamic ephemeral Status)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(
                        onClick = { onPostStatus("Drafting system update payload: 100% functional Compose design.", null, "TEXT") },
                        containerColor = SlateCard,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Draft status", modifier = Modifier.size(20.dp))
                    }
                    FloatingActionButton(
                        onClick = {
                            onPostStatus(
                                "Live look at Raymond & Tucci's development mainframe.",
                                "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=400",
                                "IMAGE"
                            )
                        },
                        containerColor = CyberGreen,
                        contentColor = Color.Black,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Camera status")
                    }
                }
            } else if (currentTab == 3) {
                // Calls FAB (Dial new direct voice link)
                FloatingActionButton(
                    onClick = {
                        if (users.isNotEmpty()) {
                            onStartCall(users.first().id, false)
                        }
                    },
                    containerColor = CyberGreen,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Initiate instant dialer call")
                }
            }
        },
        containerColor = DarkMidnight
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> ChatsTab(
                    chats = chats.filter {
                        searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
                    },
                    onSelectChat = onSelectChat,
                    onDeleteChat = onDeleteChat,
                    showWelcomeBanner = showWelcomeBanner
                )
                1 -> UpdatesTab(
                    statuses = statuses,
                    onViewStatus = onViewStatus,
                    onPostStatus = onPostStatus
                )
                2 -> CommunitiesTab()
                3 -> CallsTab(
                    calls = calls,
                    users = users,
                    onStartCall = onStartCall
                )
            }

            // New Chat Dialog Chooser
            if (showNewChatDialog) {
                AlertDialog(
                    onDismissRequest = { onTriggerNewChatDialog(false) },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { onTriggerNewChatDialog(false) }) { Text("Close", color = Color.Gray) }
                    },
                    title = {
                        Text("Select Secure Recipient", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    },
                    text = {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Hyper AI Option first
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SlateCard, RoundedCornerShape(12.dp))
                                        .clickable {
                                            // Create chat mock with AI
                                            onCreateChatWithUser(
                                                UserEntity(
                                                    id = "hyper_ai",
                                                    name = "Hyper AI Agent",
                                                    avatarUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150",
                                                    phone = "+1 (800) HYPER-AI",
                                                    statusMessage = "Hyper-cyber secure automation",
                                                    isOnline = true,
                                                    lastSeen = "Online"
                                                )
                                            )
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(CyberGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI", tint = Color.Black)
                                    }
                                    Column {
                                        Text("Hyper AI Personal Assistant", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Gemini Ultra dynamic code and translate", color = CyberGreen, fontSize = 11.sp)
                                    }
                                }
                            }

                            items(users) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCreateChatWithUser(user)
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = user.avatarUrl,
                                        contentDescription = user.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(SlateCard)
                                    )
                                    Column {
                                        Text(user.name, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Active contact secure hash", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    },
                    containerColor = SlateCard,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }

            // New Group Creator Dialog
            if (showGroupCreator) {
                AlertDialog(
                    onDismissRequest = { showGroupCreator = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (groupTitle.isNotEmpty() && selectedGroupMembers.isNotEmpty()) {
                                    onCreateGroup(groupTitle, selectedGroupMembers.toList())
                                    groupTitle = ""
                                    selectedGroupMembers.clear()
                                    showGroupCreator = false
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = CyberGreen)
                        ) {
                            Text("Form Crypt Circle")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showGroupCreator = false }) { Text("Abort", color = Color.Gray) }
                    },
                    title = { Text("Assemble Secure Group Chat", color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = groupTitle,
                                onValueChange = { groupTitle = it },
                                label = { Text("Group Name") },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberGreen)
                            )

                            Text("Select Contacts to enroll:", color = Color.Gray, fontSize = 12.sp)

                            LazyColumn(
                                modifier = Modifier.height(180.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(users) { user ->
                                    val isChecked = selectedGroupMembers.contains(user.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isChecked) selectedGroupMembers.remove(user.id)
                                                else selectedGroupMembers.add(user.id)
                                            }
                                            .background(if (isChecked) SlateCard else Color.Transparent, RoundedCornerShape(8.dp))
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                if (isChecked) selectedGroupMembers.remove(user.id)
                                                else selectedGroupMembers.add(user.id)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = CyberGreen)
                                        )
                                        Text(user.name, color = Color.White, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    },
                    containerColor = SlateCard
                )
            }
        }
    }
}

@Composable
fun ChatsTab(
    chats: List<ChatEntity>,
    onSelectChat: (String?) -> Unit,
    onDeleteChat: (String) -> Unit,
    showWelcomeBanner: Boolean
) {
    if (chats.isEmpty()) {
        EmptyTabPlaceholder(
            icon = Icons.Default.ChatBubbleOutline,
            title = "No active conversations",
            tip = "Initiate your first secure cryptographic communication by clicking the Floating Chat Icon below."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            if (showWelcomeBanner) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = "Secure Key", tint = CyberGreen, modifier = Modifier.size(32.dp))
                            Column {
                                Text("Engineering Authenticated ✔", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Under continuous maintenance by Tucci & Raymond.", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            items(chats) { chat ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x0DFFFFFF))
                        .clickable { onSelectChat(chat.id) }
                        .padding(14.dp)
                        .testTag("chat_item_row_${chat.id}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = chat.avatarUrl,
                                contentDescription = chat.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SlateCard)
                            )
                            if (chat.title.contains("Raymond") || chat.title.contains("AI") || chat.title.contains("Tucci")) {
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .align(Alignment.BottomEnd)
                                        .background(CyberGreen, CircleShape)
                                        .border(2.dp, ObsidianBlack, CircleShape)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = chat.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (chat.title.contains("Raymond") || chat.title.contains("Tucci") || chat.title.contains("Elon")) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified status",
                                            tint = CyberGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "10:42 AM",
                                    color = if (chat.unreadCount > 0) CyberGreen else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (chat.id) {
                                        "chat_hyper_ai" -> "Ready to serve. Try asking: Help me code..."
                                        "chat_raymond" -> "The Supabase integration is fully functional now."
                                        "chat_tucci" -> "Raymond: New build version 2.0.4..."
                                        else -> "No messages in crypt history"
                                    },
                                    color = if (chat.unreadCount > 0) CyberGreen else Color.Gray,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(0.85f)
                                )

                                if (chat.unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(CyberGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = chat.unreadCount.toString(),
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdatesTab(
    statuses: List<StatusEntity>,
    onViewStatus: (StatusEntity) -> Unit,
    onPostStatus: (String?, String?, String) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Self status row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(54.dp)) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
                        contentDescription = "My Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, CyberGreen, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(CyberGreen)
                            .align(Alignment.BottomEnd)
                            .border(1.dp, DarkMidnight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "add status icon", tint = Color.Black, modifier = Modifier.size(14.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("My Ephemeral status", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Introduce security status updates", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        // Channels / Circles
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "LATEST CRYPT CIRCLE STATUSES",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                if (statuses.isEmpty()) {
                    Text("No visual status streams loaded yet.", color = Color.Gray, fontSize = 12.sp, fontStyle = FontStyle.Italic)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        items(statuses) { status ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clickable { onViewStatus(status) }
                                    .testTag("status_bubble_${status.id}")
                            ) {
                                AsyncImage(
                                    model = status.userAvatar,
                                    contentDescription = status.userName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, CyberGreen, CircleShape)
                                        .background(SlateCard)
                                )
                                Text(
                                    text = status.userName.split(" ").firstOrNull() ?: status.userName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subscribed Verified Channels list
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SUBSCRIBED CHANNELS", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    IconButton(onClick = { Toast.makeText(context, "Channel feeds are synchronized with backend", Toast.LENGTH_SHORT).show() }) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Verify channels list", tint = CyberGreen)
                    }
                }

                ChannelListItem(
                    title = "Tucci Cyber Nation News",
                    desc = "Encryption metrics point layout updates. Build 392 is fully safe.",
                    avatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150"
                )
                ChannelListItem(
                    title = "Kawooya Raymond Lab updates",
                    desc = "Dynamic Material Design grids incorporated with speed.",
                    avatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"
                )
            }
        }
    }
}

@Composable
fun ChannelListItem(title: String, desc: String, avatar: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = avatar,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Icon(imageVector = Icons.Default.Verified, contentDescription = "verified", tint = CyberGreen, modifier = Modifier.size(13.dp))
            }
            Text(desc, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CommunitiesTab() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(imageVector = Icons.Default.Groups, contentDescription = "Groups community icon", tint = CyberGreen, modifier = Modifier.size(96.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Custom Spaces & Encrypted Communities",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Secure multi-layered channels are maintained by Raymond & Tucci. Discuss cryptography, cyber defenses, and responsive grids smoothly.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black)
            ) {
                Text("Start Secure Space Hub", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CallsTab(
    calls: List<CallEntity>,
    users: List<UserEntity>,
    onStartCall: (userId: String, isVideo: Boolean) -> Unit
) {
    if (calls.isEmpty()) {
        EmptyTabPlaceholder(
            icon = Icons.Default.Call,
            title = "Zero secure voice logs",
            tip = "Choose your recipient security contact and launch end-to-end shielded audio/video lines safely."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            items(calls) { call ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = call.userAvatar,
                        contentDescription = call.userName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SlateCard)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(call.userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (call.userName.contains("Raymond") || call.userName.contains("Tucci")) {
                                Icon(imageVector = Icons.Default.Verified, contentDescription = "verified", tint = CyberGreen, modifier = Modifier.size(13.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (call.direction == "INCOMING") Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = call.direction,
                                tint = if (call.direction == "INCOMING") Color.Red else CyberGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            val callTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(call.timestamp))
                            Text("Standard secure audio link | $callTime", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    Row {
                        IconButton(onClick = { onStartCall(call.userId, false) }) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "call", tint = CyberGreen)
                        }
                        IconButton(onClick = { onStartCall(call.userId, true) }) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = "videocam", tint = CyberGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTabPlaceholder(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tip: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color.Gray, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = tip, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
    }
}

// Custom Modifier extension to ensure layout consistency
fun Modifier.fillViewportWidth() = this.fillMaxWidth()
