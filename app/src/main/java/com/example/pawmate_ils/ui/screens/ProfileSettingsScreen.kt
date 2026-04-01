package com.example.pawmate_ils.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.ThemeManager
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.example.pawmate_ils.SettingsManager
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.LikedPetsViewModel
import com.example.pawmate_ils.GemManager
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.ProfilePhotoDefaults
import com.example.pawmate_ils.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@SuppressLint("SimpleDateFormat")
private fun formatJoinedText(createdAtMs: Long): String? {
    if (createdAtMs <= 0L) return null
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(createdAtMs))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(navController: NavController, username: String = "User",
        authViewModel: AuthViewModel = viewModel()
) {

    val context = LocalContext.current
    LaunchedEffect(Unit) { GemManager.init(context) }
    val likedPetsViewModel: LikedPetsViewModel = viewModel()
    // Observe gem count
    val gemCount by GemManager.gemCount.collectAsState()
    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }
    val settings = remember { SettingsManager(context) }
    var editableName by remember { mutableStateOf(settings.getUsername()) }
    var profilePhotoUri by remember { mutableStateOf(settings.getProfilePhotoUri()?.let { Uri.parse(it) }) }

    val userOnlineData by authViewModel.userData.collectAsState()
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


    val likedPets by likedPetsViewModel.likedPets.collectAsState()
    val likedPetsCount = likedPets.size

    //Observe online




    LaunchedEffect(Unit) {
        isDarkMode = ThemeManager.isDarkMode
    }
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB8B8B8) else Color(0xFF5A5A5A)
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentPink = Color(0xFFE84D7A)
    val hotPinkBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF6B9D), Color(0xFFE84D7A), Color(0xFFFF8FA8))
    )

    val joinedSubtitle = formatJoinedText(userOnlineData?.createdAt ?: 0L)
        ?.let { "Joined in $it" }
        ?: "Dog lover"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clickable { imagePicker.launch("image/*") }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .then(
                                        if (!userOnlineData?.photoUri.isNullOrEmpty()) {
                                            Modifier.background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        primaryColor.copy(alpha = 0.85f),
                                                        primaryColor.copy(alpha = 0.55f)
                                                    )
                                                )
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val photoUrl = userOnlineData?.photoUri
                                val userGender = userOnlineData?.gender ?: "Other"
                                if (!photoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Profile Photo",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val placeholderRes =
                                        ProfilePhotoDefaults.placeholderResForGender(userGender)
                                    Image(
                                        painter = painterResource(placeholderRes),
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .offset(x = (-2).dp, y = (-2).dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                                    .background(accentPink),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp)
                        ) {
                            OutlinedTextField(
                                value = editableName,
                                onValueChange = { editableName = it },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.25f),
                                    cursorColor = primaryColor,
                                    focusedContainerColor = cardColor,
                                    unfocusedContainerColor = cardColor
                                ),
                                shape = RoundedCornerShape(14.dp),
                                trailingIcon = {
                                    IconButton(onClick = { settings.setUsername(editableName) }) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Save name",
                                            tint = accentPink,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )
                            Text(
                                text = joinedSubtitle,
                                fontSize = 13.sp,
                                color = secondaryTextColor,
                                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(hotPinkBrush, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.diamond),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(34.dp)
                                )
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(
                                        text = "PawMate Gems",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Your gem balance",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = gemCount.toString(),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val statTileShape = RoundedCornerShape(22.dp)
                val statBorderLiked = Color.White.copy(alpha = if (isDarkMode) 0.18f else 0.28f)
                val statBorderGems = if (isDarkMode) primaryColor.copy(alpha = 0.28f) else primaryColor.copy(alpha = 0.14f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(118.dp)
                            .clip(statTileShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = if (isDarkMode) 0.95f else 1f),
                                        accentPink.copy(alpha = if (isDarkMode) 0.88f else 0.92f)
                                    )
                                )
                            )
                            .border(1.dp, statBorderLiked, statTileShape)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = likedPetsCount.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 30.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Liked pets",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.2.sp,
                                        lineHeight = 14.sp,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(118.dp)
                            .clip(statTileShape)
                            .background(cardColor)
                            .border(1.dp, statBorderGems, statTileShape)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = if (isDarkMode) 0.22f else 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.diamond),
                                    contentDescription = null,
                                    tint = accentPink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = gemCount.toString(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentPink,
                                    lineHeight = 30.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Gems",
                                    style = TextStyle(
                                        color = secondaryTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.2.sp,
                                        lineHeight = 14.sp,
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
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
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ModernSettingsItem(
                            icon = Icons.Default.Brightness4,
                            label = "Dark Mode",
                            subtitle = "Toggle dark theme",
                            hasSwitch = true,
                            isEnabled = isDarkMode,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            primaryColor = accentPink,
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
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                )

                ProfileMoreMenuCard(
                    icon = Icons.Default.AccountCircle,
                    label = "Account Settings",
                    subtitle = "Manage your account",
                    cardColor = cardColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    accentPink = accentPink,
                    onClick = { navController.navigate("account_settings") }
                )
                ProfileMoreMenuCard(
                    icon = Icons.Default.HelpOutline,
                    label = "Help & Support",
                    subtitle = "Get help and contact us",
                    cardColor = cardColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    accentPink = accentPink,
                    onClick = { navController.navigate("help_support") }
                )
                ProfileMoreMenuCard(
                    icon = Icons.Default.Info,
                    label = "About",
                    subtitle = "App version and info",
                    cardColor = cardColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    accentPink = accentPink,
                    onClick = { navController.navigate("about_app") }
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 28.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = {
                        authViewModel.signOut(context) {
                            navController.navigate("login") {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE84D7A)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFFE84D7A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMoreMenuCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    accentPink: Color,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentPink.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentPink,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = secondaryTextColor.copy(alpha = 0.75f)
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = secondaryTextColor.copy(alpha = 0.45f),
                modifier = Modifier.size(20.dp)
            )
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
