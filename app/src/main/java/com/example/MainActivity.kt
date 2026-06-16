package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.DarkMidnight
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SlateCard
import com.example.ui.viewmodel.HyperViewModel

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
    val chats by viewModel.chats.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())

    var isAdminAuthenticated by rememberSaveable { mutableStateOf(false) }
    var adminEmail by rememberSaveable { mutableStateOf("kawooya@tucci.cyber") }
    var adminPasscode by rememberSaveable { mutableStateOf("") }
    var admin2FACode by rememberSaveable { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AdminRole.SUPER_ADMIN) }

    var biometricScanning by remember { mutableStateOf(false) }

    if (isAdminAuthenticated) {
        AdminDashboardScreen(
            currentAdminName = if (adminEmail.contains("@")) adminEmail.substringBefore("@").replaceFirstChar { it.uppercase() } else "Kawooya Raymond",
            adminRole = selectedRole,
            onLogout = { isAdminAuthenticated = false },
            allUsersFromDb = users,
            allChatsFromDb = chats,
            onResetUserPassword = { viewModel.resetUserPassword(it) },
            onVerifyUser = { viewModel.verifyUserStatus(it) },
            onSuspendUser = { uid, susp -> viewModel.suspendUserStatus(uid, susp) },
            onDeleteUser = { viewModel.deleteUserForever(it) },
            onCreateChannel = { viewModel.createChannel(it) }
        )
    } else {
        // Admin Login Page
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkMidnight
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(
                        text = "TUCCI CYBER NATION | ENTERPRISE",
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hyper Chat Admin",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Secured Supabase Command Terminal",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Center Credential Inputs Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF161616))
                        .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "ADMINISTRATOR CREDENTIALS",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )

                        // Role Selection Row
                        Column {
                            Text("Select Node Authorization Role", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminRole.values().forEach { r ->
                                    val isSelected = selectedRole == r
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) CyberGreen else Color(0x0FFFFFFF))
                                            .border(1.dp, if (isSelected) CyberGreen else Color(0x0CFFFFFF), RoundedCornerShape(10.dp))
                                            .clickable { selectedRole = r }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = r.displayName.substringBefore(" "),
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Email Textfield
                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Agent Email identifier", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberGreen,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Passcode Field
                        OutlinedTextField(
                            value = adminPasscode,
                            onValueChange = { adminPasscode = it },
                            label = { Text("Pin Passcode key (4-digit)", color = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberGreen,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 2FA Authentication
                        OutlinedTextField(
                            value = admin2FACode,
                            onValueChange = { admin2FACode = it },
                            label = { Text("Secured 2-Factor Token (Enter any 4 digits)", color = Color.Gray) },
                            placeholder = { Text("e.g. 7843", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyberGreen,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Biometric Fingerprint Decryptor
                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(if (biometricScanning) CyberGreen.copy(alpha = 0.2f) else SlateCard)
                            .clickable {
                                biometricScanning = true
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Scan Fingerprint",
                            tint = if (biometricScanning) CyberGreen else Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (biometricScanning) "Decryption Hashing complete ✓" else "Tap Scanner to verify biometric key",
                        color = if (biometricScanning) CyberGreen else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (adminEmail.isBlank()) {
                                Toast.makeText(viewModel.getApplication(), "Please provide email ID", Toast.LENGTH_SHORT).show()
                            } else if (adminPasscode.length < 4) {
                                Toast.makeText(viewModel.getApplication(), "Please enter the 4-digit pincode passphrase", Toast.LENGTH_SHORT).show()
                            } else if (!biometricScanning) {
                                Toast.makeText(viewModel.getApplication(), "Touch Scanner to decrypt administrative session hashes first", Toast.LENGTH_SHORT).show()
                            } else {
                                isAdminAuthenticated = true
                                Toast.makeText(viewModel.getApplication(), "Supabase link initialized. Access Granted.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_terminal_login_btn")
                    ) {
                        Text(
                            text = "Authenticate Admin Node Session",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Developed by Tucci Cyber Nation & Kawooya Raymond under Supabase Realtime Protocols.",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
