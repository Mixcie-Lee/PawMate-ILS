package com.example.pawmate_ils.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.SettingsManager
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.R

// --- 1. THE MAIN SCREEN (Stateful) ---
@Composable
fun ShelterProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    adoptionCenterViewModel: AdoptionCenterViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }

    val userOnlineData by authViewModel.userData.collectAsState(initial = null)
    val petsCount by adoptionCenterViewModel.uploadedPetsCount.collectAsState(initial = 0)





    ShelterProfileContent(
        navController = navController,
        username = userOnlineData?.name ?: settings.getUsername(),
        photoUri = userOnlineData?.photoUri,
        petsCount = petsCount,
        onUpdateName = { newName ->
            authViewModel.updateProfile(newName = newName) { success, _ ->
                if (success) settings.setUsername(newName)
            }
        },
        onUploadPhoto = { uri ->
            authViewModel.uploadToCloudinary(context, uri) { permanentUrl ->
                authViewModel.updateProfile(newPhotoUri = permanentUrl) { success, _ ->
                    if (success) settings.setProfilePhotoUri(permanentUrl)
                }
            }
        },
        onLogout = {
            authViewModel.signOut(context) {
                navController.navigate("login") {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
        }
    )
}

// --- 2. THE STATELESS UI (Logic & Navigation Fixed) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterProfileContent(
    navController: NavController,
    username: String,
    photoUri: String?,
    petsCount: Int,
    onUpdateName: (String) -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onLogout: () -> Unit
) {
    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }
    var editableName by remember(username) { mutableStateOf(username) }

    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB8B8B8) else Color(0xFF5A5A5A)
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentPink = Color(0xFFE84D7A)
    val hotPinkBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF6B9D), Color(0xFFE84D7A), Color(0xFFFF8FA8))
    )

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onUploadPhoto(it) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text("Shelter Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = backgroundColor)
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // HEADER CARD
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(72.dp).clickable { imagePicker.launch("image/*") }) {
                            Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(primaryColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                if (!photoUri.isNullOrEmpty()) {
                                    AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Person, null, tint = primaryColor, modifier = Modifier.size(35.dp))
                                }
                            }
                            Box(modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).offset(x = (-2).dp, y = (-2).dp).clip(CircleShape).border(2.dp, Color.White, CircleShape).background(accentPink), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Edit, null, tint = Color.White, modifier = Modifier.size(15.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                            OutlinedTextField(
                                value = editableName,
                                onValueChange = { editableName = it },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = Color.Gray.copy(alpha = 0.25f), cursorColor = primaryColor),
                                shape = RoundedCornerShape(14.dp),
                                trailingIcon = {
                                    IconButton(onClick = { onUpdateName(editableName) }) {
                                        Icon(Icons.Default.Edit, null, tint = accentPink, modifier = Modifier.size(20.dp))
                                    }
                                }
                            )
                            Text("Official Animal Shelter", fontSize = 13.sp, color = secondaryTextColor, modifier = Modifier.padding(top = 6.dp, start = 4.dp))
                        }
                    }
                }

                // DASHBOARD BANNER
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Box(modifier = Modifier.fillMaxWidth().background(hotPinkBrush, RoundedCornerShape(20.dp)).padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pets, null, tint = Color.White, modifier = Modifier.size(34.dp))
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text("Shelter Dashboard", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Real-time pet and application tracking", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // STAT TILES
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShelterStatTile("Uploaded Pets", petsCount.toString(), Icons.Default.Pets, isDarkMode, primaryColor, accentPink, Modifier.weight(1f))
                    ShelterStatTile("Applications", "0", Icons.AutoMirrored.Filled.Assignment, isDarkMode, primaryColor, accentPink, Modifier.weight(1f), useCardBg = true)
                }

                Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.padding(bottom = 16.dp))

                // DARK MODE CARD
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(accentPink.copy(alpha = 0.15f)), Alignment.Center) {
                            Icon(imageVector = Icons.Default.Brightness4, contentDescription = null, tint = accentPink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Dark Mode", fontWeight = FontWeight.Medium, color = textColor)
                            Text("Toggle shelter theme", fontSize = 13.sp, color = secondaryTextColor)
                        }
                        Switch(checked = isDarkMode, onCheckedChange = { ThemeManager.toggleDarkMode(); isDarkMode = ThemeManager.isDarkMode }, colors = SwitchDefaults.colors(checkedTrackColor = accentPink))
                    }
                }

                Text("General", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.padding(bottom = 12.dp, top = 4.dp))

                // --- FIXED NAVIGATION CLICKS ---
                ShelterMenuCard(Icons.Default.AccountCircle, "Account Settings", "Shelter credentials", cardColor, textColor, secondaryTextColor, accentPink) {
                    navController.navigate("account_settings")
                }
                ShelterMenuCard(Icons.Default.Info, "About PawMate", "App Version 1.0", cardColor, textColor, secondaryTextColor, accentPink) {
                    navController.navigate("about_app")
                }

                // LOGOUT
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 28.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = cardColor), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), onClick = onLogout) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = accentPink)
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = accentPink, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

// --- 3. HELPER COMPONENTS (Navigation-Enabled) ---
@Composable
fun ShelterStatTile(label: String, count: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isDarkMode: Boolean, primaryColor: Color, accentColor: Color, modifier: Modifier = Modifier, useCardBg: Boolean = false) {
    val shape = RoundedCornerShape(22.dp)
    Box(modifier = modifier.height(120.dp).clip(shape).then(if (useCardBg) Modifier.background(if (isDarkMode) Color(0xFF2A2A2A) else Color.White) else Modifier.background(Brush.linearGradient(listOf(primaryColor, accentColor)))).border(1.dp, Color.White.copy(alpha = 0.15f), shape)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(if (useCardBg) primaryColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = if (useCardBg) accentColor else Color.White, modifier = Modifier.size(19.dp))
            }
            Column {
                Text(count, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = if (useCardBg) accentColor else Color.White)
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (useCardBg) Color.Gray else Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterMenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    accentPink: Color,
    onClick: () -> Unit // 🔥 Added Click Lambda
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick // 🔥 Enabled Click
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(accentPink.copy(alpha = 0.14f)), Alignment.Center) {
                Icon(icon, null, tint = accentPink, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                Text(subtitle, fontSize = 13.sp, color = secondaryTextColor.copy(alpha = 0.75f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = secondaryTextColor.copy(alpha = 0.45f), modifier = Modifier.size(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShelterProfilePreview() {
    ShelterProfileContent(
        navController = rememberNavController(),
        username = "Raym's Shelter",
        photoUri = null,
        petsCount = 10,
        onUpdateName = {},
        onUploadPhoto = {},
        onLogout = {}
    )
}