package com.example.pawmate_ils.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.SettingsManager
import com.example.pawmate_ils.ThemeManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelterProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }


    //FOR IMAGE HANDLING to set image immediately kahit nag uupload pa sa cloud ang image
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    //observe uploaded pets


    // Theme logic
    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = Color.Gray
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)

    // Lively Data Observation
    val userOnlineData by authViewModel.userData.collectAsState()
    var editableName by remember(userOnlineData?.name) {
        mutableStateOf(userOnlineData?.name ?: settings.getUsername())
    }

    // Image Picker logic
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { localUri ->
            // Directly upload and save to the cloud
            authViewModel.uploadToCloudinary(context, localUri) { permanentUrl ->
                // Update Firestore so it manifests for the shelter instantly
                authViewModel.updateProfile(newPhotoUri = permanentUrl) { success, _ ->
                    if (success) {
                        // Update local cache for offline speed
                        settings.setProfilePhotoUri(permanentUrl)
                    }
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                // Header: Circle Logo + Name Field
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { imagePicker.launch("image/*") }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val photoUrl = userOnlineData?.photoUri
                            if (!photoUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .offset(x = (-3).dp, y = (-3).dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit photo",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editableName,
                        onValueChange = { editableName = it },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(color = textColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, cursorColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                authViewModel.updateProfile(newName = editableName) { success, _ ->
                                    if (success) settings.setUsername(editableName)
                                }
                            }) {
                                Icon(Icons.Default.Edit, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                    Text("Animal Shelter", fontSize = 14.sp, color = secondaryTextColor, modifier = Modifier.padding(top = 8.dp))
                }

                // Stats Section


                // Settings Section
                Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.padding(bottom = 16.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                    Column {
                        ModernSettingsItem(icon = Icons.Default.Notifications, label = "Notifications", subtitle = "Manage alerts", hasSwitch = true, isEnabled = settings.isNotificationsEnabled(), textColor = textColor, primaryColor = primaryColor, onToggle = { settings.setNotificationsEnabled(!settings.isNotificationsEnabled()) })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        ModernSettingsItem(icon = Icons.Default.Settings, label = "Dark Mode", subtitle = "Toggle theme", hasSwitch = true, isEnabled = isDarkMode, textColor = textColor, primaryColor = primaryColor, onToggle = {
                            ThemeManager.toggleDarkMode()
                            isDarkMode = ThemeManager.isDarkMode
                        })
                    }
                }

                // General Section (Account, Help, About)
                Text("General", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.padding(bottom = 16.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardColor)) {
                    Column {
                        ModernSettingsItem(icon = Icons.Default.Person, label = "Account Settings", subtitle = "Manage info", hasSwitch = false, textColor = textColor, primaryColor = primaryColor, onClick = { navController.navigate("account_settings") })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        ModernSettingsItem(icon = Icons.Default.Info, label = "Help & Support", subtitle = "Get assistance", hasSwitch = false, textColor = textColor, primaryColor = primaryColor, onClick = { navController.navigate("help_support") })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))
                        ModernSettingsItem(icon = Icons.Default.Settings, label = "About PawMate", subtitle = "App version", hasSwitch = false, textColor = textColor, primaryColor = primaryColor, onClick = { navController.navigate("about_app") })
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// Uniform Helper Composables


@Composable
fun ModernSettingsItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, subtitle: String, hasSwitch: Boolean, isEnabled: Boolean = false, textColor: Color, primaryColor: Color, onClick: (() -> Unit)? = null, onToggle: (() -> Unit)? = null) {
    Row(modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(primaryColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = primaryColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = textColor)
            Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        }
        if (hasSwitch) {
            Switch(checked = isEnabled, onCheckedChange = { onToggle?.invoke() }, colors = SwitchDefaults.colors(checkedTrackColor = primaryColor))
        } else {
            Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}