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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import coil.compose.rememberAsyncImagePainter
import com.example.pawmate_ils.SettingsManager
import androidx.compose.ui.platform.LocalContext
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(navController: NavController, username: String = "User") {
    //Firebase tools for saving settings such as photo and name remotely and locally
    val authViewModel: AuthViewModel = viewModel()
    val firestoreRepository = remember { FirestoreRepository() }
    val coroutineScope = rememberCoroutineScope()
    //<---------DIVIDER BETWEN FIREBASE TOOLS AND OTHER COMPONENTS--------->
    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }
    val context = LocalContext.current
    val settings = remember { SettingsManager(context) }
    var notificationsEnabled by remember { mutableStateOf(settings.isNotificationsEnabled()) }
    var privacyEnabled by remember { mutableStateOf(settings.isPrivacyEnabled()) }
    var editableName by remember { mutableStateOf(settings.getUsername()) }
    var profilePhotoUri by remember { mutableStateOf(settings.getProfilePhotoUri()?.let { Uri.parse(it) }) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            profilePhotoUri = uri
            settings.setProfilePhotoUri(uri.toString())
            val currentUser = authViewModel.currentUser
            currentUser?.let { user ->
                coroutineScope.launch(Dispatchers.IO) {
                    try{
                        val downloadUrl =  firestoreRepository.uploadProfilePhoto(user.uid, uri)
                        settings.setProfilePhotoUri(downloadUrl) // save remote url locally
                        profilePhotoUri = Uri.parse(downloadUrl)
                    }catch( e: Exception){
                        e.printStackTrace()
                    }
                }
            }

        }
    }


    // Update local state when theme changes
    LaunchedEffect(Unit) {
        authViewModel.syncUserDataLocal(context)
        delay(800)
        editableName = settings.getUsername()
        profilePhotoUri = settings.getProfilePhotoUri()?.let { Uri.parse(it) }

        println("🔥 Saved photo URI = $profilePhotoUri") // check logcat output
        isDarkMode = ThemeManager.isDarkMode
    }
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F5F5)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color.Gray else Color.Gray
    
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
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .clickable {
                                    imagePicker.launch("image/*")},
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePhotoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(profilePhotoUri?.toString()),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                    Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = editableName,
                                onValueChange = { editableName = it },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(color = textColor, fontSize = 16.sp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkBrown,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                    cursorColor = DarkBrown
                                )
                            )
                            Text(
                                text = "description",
                                fontSize = 12.sp,
                                color = secondaryTextColor
                            )
                        }
                        
                        IconButton(onClick = {
                            settings.setUsername(editableName)
                            val currentUser = authViewModel.currentUser
                            currentUser?.let { user ->
                                coroutineScope?.launch(Dispatchers.IO) {
                                      try{
                                          firestoreRepository.updateDisplayName(user.uid, editableName)
                                      }catch (e : Exception){
                                          e.printStackTrace()}
                                  }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SettingsItem(
                            label = "Notifications",
                            hasSwitch = true,
                            isEnabled = notificationsEnabled,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            onToggle = {
                                notificationsEnabled = !notificationsEnabled
                                settings.setNotificationsEnabled(notificationsEnabled)
                            }
                        )
                        
                        Divider(color = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f))
                        
                        SettingsItem(
                            label = "Privacy",
                            hasSwitch = true,
                            isEnabled = privacyEnabled,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            onToggle = {
                                privacyEnabled = !privacyEnabled
                                settings.setPrivacyEnabled(privacyEnabled)
                            }
                        )
                        
                        Divider(color = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f))
                        
                        SettingsItem(
                            label = "Dark Mode",
                            hasSwitch = true,
                            isEnabled = isDarkMode,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            onToggle = { 
                                ThemeManager.toggleDarkMode()
                                isDarkMode = ThemeManager.isDarkMode
                            }
                        )
                        
                        Divider(color = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f))
                        
                        SettingsItem(
                            label = "Account Settings",
                            hasSwitch = false,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            onClick = { navController.navigate("account_settings") }
                        )
                        
                        Divider(color = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f))
                        
                        SettingsItem(
                            label = "Help & Support",
                            hasSwitch = false,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            onClick = { navController.navigate("help_support") }
                        )
                        
                        Divider(color = if (isDarkMode) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f))
                        
                        SettingsItem(
                            label = "About",
                            hasSwitch = false,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
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
                    checkedTrackColor = DarkBrown,
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
fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        ) { onClick() }
    )
}
