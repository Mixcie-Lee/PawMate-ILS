package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.ThemeManager
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.rememberAsyncImagePainter
import com.example.pawmate_ils.SettingsManager
import androidx.compose.ui.platform.LocalContext
import com.example.pawmate_ils.Firebase_Utils.LikedPetsViewModel
import com.example.pawmate_ils.GemManager



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(navController: NavController, username: String = "User") {
    val context = LocalContext.current
    LaunchedEffect(Unit) { GemManager.init(context) }
    val likedPetsViewModel: LikedPetsViewModel = viewModel()
    // Observe gem count
    val gemCount by GemManager.gemCount.collectAsState()
    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }
    val settings = remember { SettingsManager(context) }
    var notificationsEnabled by remember { mutableStateOf(settings.isNotificationsEnabled()) }
    var privacyEnabled by remember { mutableStateOf(settings.isPrivacyEnabled()) }
    var editableName by remember { mutableStateOf(settings.getUsername()) }
    var profilePhotoUri by remember { mutableStateOf(settings.getProfilePhotoUri()?.let { Uri.parse(it) }) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            profilePhotoUri = uri
            settings.setProfilePhotoUri(uri.toString())
        }
    }
    val likedPets by likedPetsViewModel.likedPets.collectAsState()
    val likedPetsCount = likedPets.size



    LaunchedEffect(Unit) {
        isDarkMode = ThemeManager.isDarkMode
    }
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color.Gray else Color.Gray
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentColor = if (isDarkMode) Color(0xFFB39DDB) else Color(0xFFDDA0DD)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.8f),
                                        primaryColor.copy(alpha = 0.6f)
                                    )
                                )
                            )
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(profilePhotoUri),
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editableName,
                        onValueChange = { editableName = it },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            cursorColor = primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { settings.setUsername(editableName) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Save",
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )

                    Text(
                        text = "Dog lover",
                        fontSize = 14.sp,
                        color = secondaryTextColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = likedPetsCount.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "LIKED PETS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = accentColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = gemCount.toString(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "GEMS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ModernSettingsItem(
                            icon = Icons.Default.Person,
                            label = "Notifications",
                            subtitle = "Manage your notifications",
                            hasSwitch = true,
                            isEnabled = notificationsEnabled,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = primaryColor,
                            onToggle = {
                                notificationsEnabled = !notificationsEnabled
                                settings.setNotificationsEnabled(notificationsEnabled)
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = if (isDarkMode) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f)
                        )

                        ModernSettingsItem(
                            icon = Icons.Default.Person,
                            label = "Privacy",
                            subtitle = "Control your privacy settings",
                            hasSwitch = true,
                            isEnabled = privacyEnabled,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = primaryColor,
                            onToggle = {
                                privacyEnabled = !privacyEnabled
                                settings.setPrivacyEnabled(privacyEnabled)
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = if (isDarkMode) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f)
                        )

                        ModernSettingsItem(
                            icon = Icons.Default.Person,
                            label = "Dark Mode",
                            subtitle = "Toggle dark theme",
                            hasSwitch = true,
                            isEnabled = isDarkMode,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = primaryColor,
                            onToggle = {
                                ThemeManager.toggleDarkMode()
                                isDarkMode = ThemeManager.isDarkMode
                            }
                        )
                    }
                }

                Text(
                    text = "General",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ModernSettingsItem(
                            icon = Icons.Default.Person,
                            label = "Account Settings",
                            subtitle = "Manage your account",
                            hasSwitch = false,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = primaryColor,
                            onClick = { navController.navigate("account_settings") }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = if (isDarkMode) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f)
                        )

                        ModernSettingsItem(
                            icon = Icons.Default.Person,
                            label = "Help & Support",
                            subtitle = "Get help and contact us",
                            hasSwitch = false,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = primaryColor,
                            onClick = { navController.navigate("help_support") }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = if (isDarkMode) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f)
                        )

                        ModernSettingsItem(
                            icon = Icons.Default.Person,
                            label = "About",
                            subtitle = "App version and info",
                            hasSwitch = false,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = primaryColor,
                            onClick = { navController.navigate("about_app") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    hasSwitch: Boolean,
    isEnabled: Boolean = false,
    textColor: Color = Color.Black,
    secondaryTextColor: Color = Color.Gray,
    primaryColor: Color = Color(0xFFFFB6C1),
    onClick: (() -> Unit)? = null,
    onToggle: (() -> Unit)? = null
) {
    var switchState by remember { mutableStateOf(isEnabled) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { mod ->
                if (!hasSwitch && onClick != null) {
                    mod.clickableWithoutRipple { onClick() }
                } else {
                    mod
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        if (hasSwitch) {
            Switch(
                checked = switchState,
                onCheckedChange = {
                    switchState = it
                    onToggle?.invoke()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        } else {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = secondaryTextColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ModernSettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    hasSwitch: Boolean,
    isEnabled: Boolean = false,
    textColor: Color = Color.Black,
    secondaryTextColor: Color = Color.Gray,
    primaryColor: Color = Color(0xFFFFB6C1),
    onClick: (() -> Unit)? = null,
    onToggle: (() -> Unit)? = null
) {
    var switchState by remember { mutableStateOf(isEnabled) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { mod ->
                if (!hasSwitch && onClick != null) {
                    mod.clickableWithoutRipple { onClick() }
                } else {
                    mod
                }
            }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(primaryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = secondaryTextColor.copy(alpha = 0.7f)
            )
        }

        if (hasSwitch) {
            Switch(
                checked = switchState,
                onCheckedChange = {
                    switchState = it
                    onToggle?.invoke()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        } else {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = secondaryTextColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        ) { onClick() }
    )
}
