package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.UserEntity
import com.example.data.db.ChatEntity
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkMidnight
import com.example.ui.theme.SlateCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Admin Roles
enum class AdminRole(val displayName: String, val level: String) {
    SUPER_ADMIN("Super Admin", "Lvl 4 Override"),
    MODERATOR("System Moderator", "Lvl 2 Rules"),
    SUPPORT_AGENT("Support Agent", "Lvl 1 Support")
}

// Support Ticket Model
data class SupportTicket(
    val id: String,
    val userName: String,
    val category: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val status: String, // PENDING, ASSIGNED, RESOLVED
    val description: String,
    val timestamp: String,
    var agentNotes: String = ""
)

// Content Report Model
data class ContentReport(
    val id: String,
    val context: String,
    val reporter: String,
    val reportedUser: String,
    val riskScore: Int, // AI Abuse risk evaluation percentage
    val status: String = "FLAGGED"
)

// Log Audit Entry
data class AuditLogEntry(
    val timestamp: String,
    val admin: String,
    val action: String,
    val ipAddress: String,
    val severity: String // INFO, WARNING, SECURITY_ALERT
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    currentAdminName: String,
    adminRole: AdminRole,
    onLogout: () -> Unit,
    allUsersFromDb: List<UserEntity>,
    allChatsFromDb: List<ChatEntity>,
    onResetUserPassword: (String) -> Unit,
    onVerifyUser: (String) -> Unit,
    onSuspendUser: (String, Boolean) -> Unit,
    onDeleteUser: (String) -> Unit,
    onCreateChannel: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedTabSection by remember { mutableStateOf(0) } // 0: Overviews, 1: Users, 2: Channels, 3: Support, 4: Reports, 5: DB, 6: Security, 7: Rules

    // Broadcast state
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastMsg by remember { mutableStateOf("") }
    var emergencyBroadcastMode by remember { mutableStateOf(false) }

    // Maintenance Mode & settings
    var maintenanceModeEnabled by remember { mutableStateOf(false) }
    var fileLimitMb by remember { mutableStateOf(50f) }
    var dbEncryptionKey by remember { mutableStateOf("AES256-SUPER-KEY-TUCCI") }

    // Mock reports setup
    val initialReports = remember {
        mutableStateListOf(
            ContentReport("REP-082", "Spam crypto scheme links posted in Main Room", "Kawooya Raymond", "crypto_bot_9x", 85),
            ContentReport("REP-110", "Aggressive spamming during system sync", "Elon Musk", "anonymous_alpha", 95),
            ContentReport("REP-412", "Flagged phishing prompt targeting admin", "Support Agent 1", "hack_central", 98),
            ContentReport("REP-003", "Possible false positive system loop", "Hyper AI", "user_102", 34)
        )
    }

    // Mock Support Tickets setup
    val ticketsList = remember {
        mutableStateListOf(
            SupportTicket("TKT-502", "Elon Musk", "Premium Activation", "HIGH", "PENDING", "Paid via crypto wallet, Premium badge is not showing in profile. Please activate.", "10:45 AM"),
            SupportTicket("TKT-311", "Tucci Dev 1", "Account Recovery", "MEDIUM", "ASSIGNED", "Forgot my decryption passphrase during database sync. Need restoration.", "Yesterday"),
            SupportTicket("TKT-108", "Ann Jenkins", "Verification Request", "LOW", "RESOLVED", "Requesting verified checkmark for official Tucci Cyber blog channel.", "2 Days ago")
        )
    }

    // Interactive Logs
    val auditLogList = remember {
        mutableStateListOf(
            AuditLogEntry("14:52:10", "Kawooya Raymond", "Triggered Full DB Replication backup cycle", "192.168.1.100", "INFO"),
            AuditLogEntry("14:50:01", "Hyper AI", "Abuse shield automatically flagged user hack_central", "10.0.4.15", "SECURITY_ALERT"),
            AuditLogEntry("14:48:32", "System Cron", "Garbage collection cleared 24MB temporary blobs", "127.0.0.1", "INFO"),
            AuditLogEntry("14:12:00", "Kawooya Raymond", "Updated Tucci Cyber guidelines rules document", "192.168.1.100", "WARNING")
        )
    }

    // Dynamic Network Stats list
    var onlineUsersCount by remember { mutableStateOf(14) }
    var totalRegistrations by remember { mutableStateOf(342) }
    var totalDailyMessages by remember { mutableStateOf(1402) }
    var totalDailyCalls by remember { mutableStateOf(189) }

    // SQL Console simulation states
    var sqlCommandInput by remember { mutableStateOf("SELECT * FROM users WHERE status = 'banned';") }
    val sqlTerminalLogs = remember {
        mutableStateListOf<String>(
            "Supabase PostgreSQL Terminal ready.",
            "Client: pg-crypto-tucci-secured-v4",
            "Type your query below and click Execute Query."
        )
    }

    // Simulate real-time ticker
    LaunchedEffect(Unit) {
        while (true) {
            delay(12000)
            onlineUsersCount += (-2..3).random()
            totalDailyMessages += (1..5).random()
            if (Math.random() > 0.7) {
                auditLogList.add(
                    0,
                    AuditLogEntry(
                        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
                        "Supreme Security Bot",
                        "Heartbeat sync with Supabase replica: OK",
                        "109.22.45.19",
                        "INFO"
                    )
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkMidnight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TUCCI CYBER NATION | CONTROL CENTER",
                            color = CyberGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hyper Chat Admin",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkMidnight),
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF166534))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = adminRole.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            onLogout()
                            Toast.makeText(context, "Logged out from cyber terminal securely", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out", tint = Color.LightGray)
                    }
                }
            )
        },
        bottomBar = {
            // Horizontal scrollable premium admin tab bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111))
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AdminTabButton(title = "Dashboard", isSelected = selectedTabSection == 0, icon = Icons.Default.Dashboard) { selectedTabSection = 0 }
                AdminTabButton(title = "Users", isSelected = selectedTabSection == 1, icon = Icons.Default.Person) { selectedTabSection = 1 }
                AdminTabButton(title = "Channels", isSelected = selectedTabSection == 2, icon = Icons.Default.Layers) { selectedTabSection = 2 }
                AdminTabButton(title = "Support Ticket", isSelected = selectedTabSection == 3, icon = Icons.Default.ConfirmationNumber) { selectedTabSection = 3 }
                AdminTabButton(title = "AI Moderation", isSelected = selectedTabSection == 4, icon = Icons.Default.Security) { selectedTabSection = 4 }
                AdminTabButton(title = "SQL Console", isSelected = selectedTabSection == 5, icon = Icons.Default.Terminal) { selectedTabSection = 5 }
                AdminTabButton(title = "Security Logs", isSelected = selectedTabSection == 6, icon = Icons.Default.Shield) { selectedTabSection = 6 }
                AdminTabButton(title = "Rules", isSelected = selectedTabSection == 7, icon = Icons.Default.LibraryBooks) { selectedTabSection = 7 }
            }
        },
        floatingActionButton = {
            if (selectedTabSection == 0 || selectedTabSection == 7) {
                FloatingActionButton(
                    onClick = { showBroadcastDialog = true },
                    containerColor = CyberGreen,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("admin_broadcast_fab")
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = "Broadcast Push Notification")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkMidnight)
        ) {
            Crossfade(targetState = selectedTabSection, label = "admin_screens_transition") { tabId ->
                when (tabId) {
                    0 -> RenderOverviewSection(
                        currentAdmin = currentAdminName,
                        adminRole = adminRole,
                        onlineCount = onlineUsersCount,
                        registeredCount = totalRegistrations,
                        msgCount = totalDailyMessages,
                        callCount = totalDailyCalls,
                        reportsCount = initialReports.size,
                        maintenanceMode = maintenanceModeEnabled,
                        onToggleMaintenance = { maintenanceModeEnabled = it },
                        logs = auditLogList,
                        onTriggerSync = {
                            Toast.makeText(context, "Starting live Supabase database sync...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> RenderUsersManagement(
                        users = allUsersFromDb,
                        onResetPassword = { uid ->
                            onResetUserPassword(uid)
                            auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Reset user recovery credential mapping: $uid", "192.168.1.100", "WARNING"))
                        },
                        onVerify = { uid ->
                            onVerifyUser(uid)
                            auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Awarded global verified status: $uid", "192.168.1.100", "INFO"))
                        },
                        onSuspend = { uid, susp ->
                            onSuspendUser(uid, susp)
                            val actionTxt = if (susp) "Enforced account temporary suspension" else "Revoked restriction/ban status"
                            auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "$actionTxt: $uid", "192.168.1.100", "WARNING"))
                        },
                        onDelete = { uid ->
                            onDeleteUser(uid)
                            auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Permanently purged storage/records: $uid", "192.168.1.100", "SECURITY_ALERT"))
                        }
                    )
                    2 -> RenderChannelsCommunities(
                        chats = allChatsFromDb.filter { it.type == "GROUP" || it.type == "CHANNEL" },
                        onCreateChan = { channelName ->
                            onCreateChannel(channelName)
                            auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Created official broadcast channel: $channelName", "192.168.1.100", "INFO"))
                        }
                    )
                    3 -> RenderSupportTickets(
                        tickets = ticketsList,
                        onResolveTicket = { index ->
                            val t = ticketsList[index]
                            ticketsList[index] = t.copy(status = "RESOLVED")
                            Toast.makeText(context, "Ticket ${t.id} successfully completed", Toast.LENGTH_SHORT).show()
                            auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Closed manual helpdesk ticket ${t.id}", "192.168.1.100", "INFO"))
                        }
                    )
                    4 -> RenderAiAndModeration(
                        reports = initialReports,
                        onResolveReport = { reportId, action ->
                            initialReports.removeAll { it.id == reportId }
                            Toast.makeText(context, "Decision executed: $action", Toast.LENGTH_SHORT).show()
                            auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Overrode Content Report $reportId with decision: $action", "192.168.1.100", "WARNING"))
                        }
                    )
                    5 -> RenderDatabaseConsole(
                        logs = sqlTerminalLogs,
                        cmdInput = sqlCommandInput,
                        onCmdChange = { sqlCommandInput = it },
                        onRunQuery = { cmd ->
                            sqlTerminalLogs.add("> $cmd")
                            if (cmd.isBlank()) return@RenderDatabaseConsole
                            val formattedCmd = cmd.trim().lowercase()
                            if (formattedCmd.startsWith("select")) {
                                if (formattedCmd.contains("users")) {
                                    sqlTerminalLogs.add("Fetched ${allUsersFromDb.size} records from system.db: Result Success.")
                                    allUsersFromDb.forEach {
                                        sqlTerminalLogs.add("  - id: ${it.id} | name: ${it.name} | isVerified: ${it.isVerified}")
                                    }
                                } else if (formattedCmd.contains("chats")) {
                                    sqlTerminalLogs.add("Fetched ${allChatsFromDb.size} records from system.db: Result Success.")
                                    allChatsFromDb.forEach {
                                        sqlTerminalLogs.add("  - id: ${it.id} | title: ${it.title} | type: ${it.type}")
                                    }
                                } else {
                                    sqlTerminalLogs.add("Query completed successfully. Returned 0 results.")
                                }
                            } else if (formattedCmd.startsWith("backup")) {
                                sqlTerminalLogs.add("PostgreSQL database compressed packet generated. Target: Supabase server.")
                                sqlTerminalLogs.add("Replication checksum: SHA256-${UUID.randomUUID().toString().take(12)}")
                            } else if (formattedCmd.contains("delete") || formattedCmd.contains("drop")) {
                                if (adminRole == AdminRole.SUPER_ADMIN) {
                                    sqlTerminalLogs.add("WARNING: Destructive statement handled with Admin override pass. Execution Successful.")
                                } else {
                                    sqlTerminalLogs.add("ERROR: Security Exception. DROP/DELETE requires Role Level Level 4 Super Admin.")
                                }
                            } else {
                                sqlTerminalLogs.add("Instruction acknowledged. Database response verified 100% OK.")
                            }
                        },
                        onBackup = {
                            sqlTerminalLogs.add("> SYSTEM_BACKUP_TRIGGER")
                            sqlTerminalLogs.add("Saving secure crypt schema locally...")
                            sqlTerminalLogs.add("DB successfully archived: hyperchat_backup_replica_" + System.currentTimeMillis() + ".sql")
                            Toast.makeText(context, "Archive binary saved successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    6 -> RenderSecurityCenter(
                        logs = auditLogList,
                        fileLimitMb = fileLimitMb,
                        onFileLimitChange = { fileLimitMb = it },
                        encryptionKey = dbEncryptionKey,
                        onEncryptionKeyChange = { dbEncryptionKey = it }
                    )
                    7 -> RenderRulesGuidelines(
                        broadcastMsg = broadcastMsg,
                        onBroadcastMsgChange = { broadcastMsg = it },
                        emergencyMode = emergencyBroadcastMode,
                        onToggleEmergency = { emergencyBroadcastMode = it },
                        onSendGlobal = {
                            if (broadcastMsg.isNotBlank()) {
                                Toast.makeText(context, "Broadcasting encrypted payload payload to all endpoints...", Toast.LENGTH_LONG).show()
                                auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Disseminated Network Announcement: $broadcastMsg", "192.168.1.100", "WARNING"))
                                broadcastMsg = ""
                                showBroadcastDialog = false
                            }
                        }
                    )
                }
            }

            // Emergency / Broadcast global dialog
            if (showBroadcastDialog) {
                AlertDialog(
                    onDismissRequest = { showBroadcastDialog = false },
                    containerColor = Color(0xFF161616),
                    icon = { Icon(imageVector = Icons.Default.Campaign, contentDescription = "Campaign Icon", tint = CyberGreen, modifier = Modifier.size(40.dp)) },
                    title = { Text("Global Server Broadcast", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "This broadcast pushes an immediate notification and dashboard message payload to all active client devices running the Hyper Chat app.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = broadcastMsg,
                                onValueChange = { broadcastMsg = it },
                                placeholder = { Text("Type announcement text...", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CyberGreen,
                                    unfocusedBorderColor = Color.Gray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = emergencyBroadcastMode,
                                    onCheckedChange = { emergencyBroadcastMode = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Black,
                                        checkedTrackColor = CyberGreen
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mark Emergency Broadcast",
                                    color = if (emergencyBroadcastMode) CyberGreen else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (broadcastMsg.isNotBlank()) {
                                    Toast.makeText(context, "Direct broadcast disseminated!", Toast.LENGTH_SHORT).show()
                                    auditLogList.add(0, AuditLogEntry(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()), currentAdminName, "Direct alert broadcast: $broadcastMsg", "192.168.1.100", "WARNING"))
                                    broadcastMsg = ""
                                    showBroadcastDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black)
                        ) {
                            Text("Broadcast Now", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBroadcastDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AdminTabButton(title: String, isSelected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) CyberGreen else Color(0x13FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) Color.Black else Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

// ---------------- OVERVIEW SCREEN ----------------
@Composable
fun RenderOverviewSection(
    currentAdmin: String,
    adminRole: AdminRole,
    onlineCount: Int,
    registeredCount: Int,
    msgCount: Int,
    callCount: Int,
    reportsCount: Int,
    maintenanceMode: Boolean,
    onToggleMaintenance: (Boolean) -> Unit,
    logs: List<AuditLogEntry>,
    onTriggerSync: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43))))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Welcome, Commander",
                    color = CyberGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = currentAdmin,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
                Text(
                    text = "Role Authorization Level: ${adminRole.displayName} / ${adminRole.level}",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onTriggerSync,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sync Icon", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Connect Supabase", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (maintenanceMode) Color(0xFFEF4444) else Color(0x22FFFFFF))
                            .clickable { onToggleMaintenance(!maintenanceMode) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(if (maintenanceMode) Color.White else CyberGreen, CircleShape))
                            Text(
                                text = if (maintenanceMode) "MAINTENANCE MODE: LIVE" else "MAINTENANCE MODE: OFF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Stats Grid
        Text("SYSTEM TELEMETRY METRICS", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 2.sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(title = "Total Registrants", count = registeredCount.toString(), desc = "+4 today", color = Color.Cyan, modifier = Modifier.weight(1f))
            MetricCard(title = "Online Active", count = onlineCount.toString(), desc = "Active endpoints", color = CyberGreen, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(title = "Daily messages", count = msgCount.toString(), desc = "Secured messages", color = Color(0xFF10B981), modifier = Modifier.weight(1f))
            MetricCard(title = "Daily Calls", count = callCount.toString(), desc = "P2P connections", color = Color(0xFFFFD700), modifier = Modifier.weight(1f))
        }

        // Analytical Canvas Chart
        Text("REAL-TIME TRAFFIC & REVENUE GRAPH", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 2.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF121212))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Grid background guidelines
                for (i in 1..4) {
                    val y = height * (i / 4f)
                    drawLine(
                        color = Color(0x0FFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // Smooth green traffic line
                val trafficPoints = listOf(
                    Offset(0f, height * 0.8f),
                    Offset(width * 0.15f, height * 0.6f),
                    Offset(width * 0.3f, height * 0.75f),
                    Offset(width * 0.45f, height * 0.45f),
                    Offset(width * 0.6f, height * 0.5f),
                    Offset(width * 0.75f, height * 0.25f),
                    Offset(width * 0.9f, height * 0.35f),
                    Offset(width, height * 0.15f)
                )

                val trafficPath = Path().apply {
                    moveTo(trafficPoints.first().x, trafficPoints.first().y)
                    for (i in 1 until trafficPoints.size) {
                        val prev = trafficPoints[i - 1]
                        val curr = trafficPoints[i]
                        val conX1 = (prev.x + curr.x) / 2f
                        val conY1 = prev.y
                        val conX2 = (prev.x + curr.x) / 2f
                        val conY2 = curr.y
                        cubicTo(conX1, conY1, conX2, conY2, curr.x, curr.y)
                    }
                }

                // Fill under trend line
                val trafficFillPath = Path().apply {
                    addPath(trafficPath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = trafficFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(CyberGreen.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                drawPath(
                    path = trafficPath,
                    color = CyberGreen,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )

                // Secondary cyan revenue peak line
                val revenuePoints = listOf(
                    Offset(0f, height * 0.9f),
                    Offset(width * 0.2f, height * 0.8f),
                    Offset(width * 0.4f, height * 0.7f),
                    Offset(width * 0.6f, height * 0.4f),
                    Offset(width * 0.8f, height * 0.55f),
                    Offset(width, height * 0.3f)
                )

                val revenuePath = Path().apply {
                    moveTo(revenuePoints.first().x, revenuePoints.first().y)
                    for (i in 1 until revenuePoints.size) {
                        val prev = revenuePoints[i - 1]
                        val curr = revenuePoints[i]
                        val conX1 = (prev.x + curr.x) / 2f
                        val conY1 = prev.y
                        val conX2 = (prev.x + curr.x) / 2f
                        val conY2 = curr.y
                        cubicTo(conX1, conY1, conX2, conY2, curr.x, curr.y)
                    }
                }

                drawPath(
                    path = revenuePath,
                    color = Color.Cyan,
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(CyberGreen, CircleShape))
                    Text("Traffic Logs", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color.Cyan, CircleShape))
                    Text("Revenue (Plus Sub)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Action Logs feed
        Text("SYSTEM AUDIT FEED", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 2.sp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161616))
                .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp)),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            logs.take(4).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        when (item.severity) {
                                            "SECURITY_ALERT" -> Color.Red
                                            "WARNING" -> Color.Yellow
                                            else -> CyberGreen
                                        },
                                        CircleShape
                                    )
                            )
                            Text(text = item.timestamp, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = item.admin, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = item.action, color = Color.LightGray, fontSize = 12.sp)
                    }
                    Text(
                        text = item.ipAddress,
                        color = Color.DarkGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x08FFFFFF))
                )
            }
        }
    }
}

@Composable
fun MetricCard(title: String, count: String, desc: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF161616))
            .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(text = title.uppercase(), color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------- USER MANAGEMENT SECTION ----------------
@Composable
fun RenderUsersManagement(
    users: List<UserEntity>,
    onResetPassword: (String) -> Unit,
    onVerify: (String) -> Unit,
    onSuspend: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var userDetailsDialogTarget by remember { mutableStateOf<UserEntity?>(null) }
    val filteredUsers = users.filter { it.name.contains(query, ignoreCase = true) || it.id.contains(query, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search system users...", color = Color.Gray) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0x0DFFFFFF),
                unfocusedContainerColor = Color(0x0DFFFFFF),
                focusedBorderColor = CyberGreen,
                unfocusedBorderColor = Color(0x1AFFFFFF)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text("ENROLLED USERS (${filteredUsers.size})", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.5.sp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUsers) { user ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161616))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp))
                        .clickable { userDetailsDialogTarget = user }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = user.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (user.isVerified) {
                                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Verified Icon", tint = CyberGreen, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(text = "id: ${user.id} | ip: ${user.phone.take(if (user.phone.length > 9) 9 else user.phone.length)}**", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = if (user.isOnline) "🟢 Online" else "⚪ Offline",
                                    color = if (user.isOnline) CyberGreen else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Chevron Right Icon", tint = Color.LightGray)
                    }
                }
            }
        }
    }

    if (userDetailsDialogTarget != null) {
        val user = userDetailsDialogTarget!!
        AlertDialog(
            onDismissRequest = { userDetailsDialogTarget = null },
            containerColor = Color(0xFF161616),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = user.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Text(text = "Manage ID: ${user.id}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("User Profile Actions", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0x13FFFFFF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Real Name: ${user.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Database Credentials status: Decrypted AES verified", color = Color.Gray, fontSize = 11.sp)
                            Text("Subscription Tier: ${if (user.isVerified) "Premium Plus VIP" else "Standard Customer"}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onVerify(user.id)
                                userDetailsDialogTarget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Shield Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onResetPassword(user.id)
                                userDetailsDialogTarget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset Pwd", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSuspend(user.id, true)
                                userDetailsDialogTarget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B), contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Suspend Ban", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onDelete(user.id)
                                userDetailsDialogTarget = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Purge DB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { userDetailsDialogTarget = null }) {
                    Text("Close", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// ---------------- COMMUNITIES & CHANNELS SCREEN ----------------
@Composable
fun RenderChannelsCommunities(
    chats: List<ChatEntity>,
    onCreateChan: (String) -> Unit
) {
    var newChanName by remember { mutableStateOf("") }
    var filterTypeGroup by remember { mutableStateOf(false) }

    val filteredChats = chats.filter {
        if (filterTypeGroup) it.type == "GROUP" else it.type == "CHANNEL"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CREATE BROADCAST OR COMMUNITY CHANNEL", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newChanName,
                onValueChange = { newChanName = it },
                placeholder = { Text("Enter channel/group target title...", color = Color.Gray, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0x0DFFFFFF),
                    unfocusedContainerColor = Color(0x0DFFFFFF),
                    focusedBorderColor = CyberGreen,
                    unfocusedBorderColor = Color(0x1AFFFFFF)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = {
                    if (newChanName.isNotBlank()) {
                        onCreateChan(newChanName)
                        newChanName = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Deploy", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!filterTypeGroup) CyberGreen else Color(0x1AFFFFFF))
                    .clickable { filterTypeGroup = false }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Channels (${chats.count { it.type == "CHANNEL" }})", color = if (!filterTypeGroup) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (filterTypeGroup) CyberGreen else Color(0x1AFFFFFF))
                    .clickable { filterTypeGroup = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Group Spaces (${chats.count { it.type == "GROUP" }})", color = if (filterTypeGroup) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredChats) { chat ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161616))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = chat.avatarUrl,
                                contentDescription = chat.title,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                            )
                            Column {
                                Text(text = chat.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "Endpoint: ${chat.id} | Mode: Security Enforced",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22FFFFFF))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("ACTIVE", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- HELP TICKETS SUPPORT SCREEN ----------------
@Composable
fun RenderSupportTickets(
    tickets: List<SupportTicket>,
    onResolveTicket: (Int) -> Unit
) {
    var ticketChatPopupTarget by remember { mutableStateOf<SupportTicket?>(null) }
    var chatReplyMsg by remember { mutableStateOf("") }
    val mockChatHistoryForTicket = remember {
        mutableStateListOf(
            "Agent: System admin online. Welcome to Tucci Helpdesk support clearance. How may we guide your account today?",
            "User: Hi, I executed a verified subscription payment, yet my badge didn't update",
            "Agent: Checking the hash ledger..."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MANUAL CUSTOMER HELP TICKETS", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tickets.size) { index ->
                val ticket = tickets[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161616))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = ticket.id, color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (ticket.status == "PENDING") Color(0xFF78350F) else Color(0xFF14532D))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = ticket.status, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(text = "Submitter: ${ticket.userName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Category: ${ticket.category} | Priority: ${ticket.priority}", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = ticket.description, color = Color.LightGray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    ticketChatPopupTarget = ticket
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "Ticket Live Chat Icon", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Chat Support", fontSize = 11.sp)
                            }

                            if (ticket.status != "RESOLVED") {
                                Button(
                                    onClick = { onResolveTicket(index) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Complete Icon", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Complete Ticket", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (ticketChatPopupTarget != null) {
        val t = ticketChatPopupTarget!!
        AlertDialog(
            onDismissRequest = { ticketChatPopupTarget = null },
            containerColor = Color(0xFF161616),
            title = { Text("Direct Link: ${t.userName} (${t.id})", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Black, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(mockChatHistoryForTicket) { txt ->
                                Text(
                                    text = txt,
                                    color = if (txt.startsWith("Agent")) CyberGreen else Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = chatReplyMsg,
                        onValueChange = { chatReplyMsg = it },
                        placeholder = { Text("Type reply onto secure pipe...", color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberGreen,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chatReplyMsg.isNotBlank()) {
                            mockChatHistoryForTicket.add("Agent: ${chatReplyMsg.trim()}")
                            chatReplyMsg = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black)
                ) {
                    Text("Send Live")
                }
            },
            dismissButton = {
                TextButton(onClick = { ticketChatPopupTarget = null }) {
                    Text("Close Panel", color = Color.Gray)
                }
            }
        )
    }
}

// ---------------- CONT MODERATION / AI SCREEN ----------------
@Composable
fun RenderAiAndModeration(
    reports: List<ContentReport>,
    onResolveReport: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("HYPER AUTOMATED CONTENT THREAT OVERRULES", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)

        // Rule AI Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF310707), Color(0xFF0F0101))))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = "Security Shield Icon", tint = Color.Red)
                    Text("HYPER AI SHIELD INTEGRATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Automatic scam, abuse, and phishing vectors algorithm score calculated recursively with custom Gemini models.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reports) { report ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161616))
                        .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = report.id, color = CyberGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (report.riskScore > 80) Color(0xFF7F1D1D) else Color(0xFF78350F))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "RISK SCORE: ${report.riskScore}%", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(text = "Reported Content context:", color = Color.Gray, fontSize = 11.sp)
                        Text(text = "\"${report.context}\"", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Sender: ${report.reportedUser} | Flagged by: ${report.reporter}", color = Color.LightGray, fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onResolveReport(report.id, "ALLOW_AND_VERIFY") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("False Positive / Pass", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onResolveReport(report.id, "BAN_ACCOUNT_AND_DELETE") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Execute Ban & Purge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SQL CONSOLE SCREEN ----------------
@Composable
fun RenderDatabaseConsole(
    logs: List<String>,
    cmdInput: String,
    onCmdChange: (String) -> Unit,
    onRunQuery: (String) -> Unit,
    onBackup: () -> Unit
) {
    val localKeyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("SECURE SQL & SUPABASE COMMAND SHEET", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)

        // Console screen output
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = false
            ) {
                items(logs) { log ->
                    Text(
                        text = log,
                        color = if (log.startsWith(">")) CyberGreen else if (log.startsWith("ERROR")) Color.Red else Color.LightGray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = cmdInput,
            onValueChange = onCmdChange,
            placeholder = { Text("Write SQL statement directly...", color = Color.Gray, fontSize = 11.sp) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                onRunQuery(cmdInput)
                localKeyboard?.hide()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyberGreen,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBackup,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFFFFFF), contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Backup, contentDescription = "Backup Database Icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hot Backup", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    onRunQuery(cmdInput)
                    localKeyboard?.hide()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Execute Query Icon", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Execute Statement", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------- SECURITY CENTER SCREEN ----------------
@Composable
fun RenderSecurityCenter(
    logs: List<AuditLogEntry>,
    fileLimitMb: Float,
    onFileLimitChange: (Float) -> Unit,
    encryptionKey: String,
    onEncryptionKeyChange: (String) -> Unit
) {
    var enabled2FA by remember { mutableStateOf(true) }
    var enforceGeoRiskLocationFilters by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("CYBER DEFENSE & KEY MANAGEMENT", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)

        // API Key encryption settings
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Supabase Database Secret Salt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = encryptionKey,
                    onValueChange = onEncryptionKeyChange,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberGreen,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Enforce options
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161616))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Security Protocol Toggles", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("De-authorize suspicious IPs (GeoRisk)", color = Color.LightGray, fontSize = 13.sp)
                    Switch(checked = enforceGeoRiskLocationFilters, onCheckedChange = { enforceGeoRiskLocationFilters = it }, colors = SwitchDefaults.colors(checkedTrackColor = CyberGreen))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Require 2FA Authentication", color = Color.LightGray, fontSize = 13.sp)
                    Switch(checked = enabled2FA, onCheckedChange = { enabled2FA = it }, colors = SwitchDefaults.colors(checkedTrackColor = CyberGreen))
                }
            }
        }

        // Limits slide
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF161616))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Max Chat Blob upload size", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${fileLimitMb.toInt()} MB", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Slider(
                    value = fileLimitMb,
                    onValueChange = onFileLimitChange,
                    valueRange = 10f..500f,
                    colors = SliderDefaults.colors(thumbColor = CyberGreen, activeTrackColor = CyberGreen)
                )
            }
        }
    }
}

// ---------------- RULES, TERMS & POLICY SECTION ----------------
@Composable
fun RenderRulesGuidelines(
    broadcastMsg: String,
    onBroadcastMsgChange: (String) -> Unit,
    emergencyMode: Boolean,
    onToggleEmergency: (Boolean) -> Unit,
    onSendGlobal: () -> Unit
) {
    var termsOfServiceEdit by remember { mutableStateOf("Tucci Cyber Nation networks are strictly end-to-end encrypted under AES-256 standard protocols. Reverse engineering or malicious node injection results in instant terminal bans.") }
    var guidelinesTextEdit by remember { mutableStateOf("1. Zero Tolerance on spam bots.\n2. Respect individual privacy pipelines.\n3. Abuse report score threshold >= 80 triggers automated account quarantine via Hyper AI.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("SYSTEM TERMS & COMMUNITY CODES", color = Color.Gray, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp)

        Text("Edit Terms Of Service agreement", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = termsOfServiceEdit,
            onValueChange = { termsOfServiceEdit = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyberGreen,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Text("Edit Rules & Moderation Guidelines", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        OutlinedTextField(
            value = guidelinesTextEdit,
            onValueChange = { guidelinesTextEdit = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = CyberGreen,
                unfocusedBorderColor = Color.DarkGray
            ),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Button(
            onClick = {
                // Mock policy update
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publish Policy Update", fontWeight = FontWeight.Bold)
        }
    }
}
