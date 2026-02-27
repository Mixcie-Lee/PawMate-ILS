package com.example.pawmate_ils.chatScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ThemeManager
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel
) {
    val homeViewModel: HomeViewModel = viewModel()
    val channels by homeViewModel.channels.collectAsState()
    val currentUserRole by authViewModel.currentUserRole.collectAsState()

    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val navBarColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White

    LaunchedEffect(Unit) {
        authViewModel.fetchUserRole()
        homeViewModel.listenToChannels()
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = navBarColor,
                contentColor = textColor,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Pets, "Swipe", tint = Color.Gray.copy(alpha = 0.6f)) },
                    label = { Text("Swipe", color = Color.Gray.copy(alpha = 0.6f)) },
                    selected = false,
                    onClick = { navController.navigate("pet_swipe") }
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.heart),
                            contentDescription = "Liked",
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray.copy(alpha = 0.6f))
                        )
                    },
                    label = { Text("Liked", color = Color.Gray.copy(alpha = 0.6f)) },
                    selected = false,
                    onClick = { navController.navigate("adopter_home") }
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.book_open),
                            contentDescription = "Learn",
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray.copy(alpha = 0.6f))
                        )
                    },
                    label = { Text("Learn", color = Color.Gray.copy(alpha = 0.6f)) },
                    selected = false,
                    onClick = { navController.navigate("educational") }
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.profile_d),
                            contentDescription = "Profile",
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray.copy(alpha = 0.6f))
                        )
                    },
                    label = { Text("Profile", color = Color.Gray.copy(alpha = 0.6f)) },
                    selected = false,
                    onClick = { navController.navigate("profile_settings") }
                )
                NavigationBarItem(
                    icon = {
                        Image(
                            painter = painterResource(id = R.drawable.message_square),
                            contentDescription = "Message",
                            modifier = Modifier.size(24.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFFFF9999))
                        )
                    },
                    label = { Text("Message", color = Color(0xFFFF9999), fontWeight = FontWeight.Bold) },
                    selected = true,
                    onClick = { /* Already on this screen */ }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Messages",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White

                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color(0xFFFFB6C1)
                    )
                )

                when {
                    currentUserRole.isNullOrEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFFFFB6C1))
                        }
                    }

                    channels.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No messages yet", color = Color.Gray, fontSize = 16.sp)
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(channels, key = { it.channelId }) { channel ->
                                val displayPhoto = if (currentUserRole == "shelter") {
                                    channel.adopterPhotoUri
                                } else {
                                    channel.shelterPhotoUri
                                }


                                val displayName = when (currentUserRole) {
                                    "shelter" -> "${channel.adopterName} • ${channel.petName}"
                                    "adopter" -> "${channel.shelterName} • ${channel.petName}"
                                    else -> "Chat • ${channel.petName}"
                                }


                                //listener function for unread badge
                                val currentUserId = authViewModel.currentUser?.uid ?: ""
                                val shouldShowBadge = channel.unreadCount > 0 && channel.lastSenderId != currentUserId

                                // ✅ This will now be recognized correctly!
                                ChatListItem(
                                    shelterName = displayName,
                                    photoUri = displayPhoto,
                                    lastMessage = if (channel.lastMessage.isNotEmpty()) channel.lastMessage else "No messages yet",
                                    timeAgo = null,
                                    unreadCount = if (shouldShowBadge) channel.unreadCount else 0,                                    onClick = {
                                        homeViewModel.resetUnreadCount(channel.channelId)
                                        navController.navigate("message/${channel.channelId}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ✅ MOVED OUTSIDE: This ensures the Composable is globally accessible within the package
@Composable
fun ChatListItem(
    shelterName: String,
    photoUri: String?,
    lastMessage: String?,
    timeAgo: String?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    val isDarkMode = ThemeManager.isDarkMode
    val cardBg = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val mainTextColor = if (isDarkMode) Color.White else Color(0xFF333333)
    val subTextColor = if (isDarkMode) Color.LightGray else Color.DarkGray
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(cardBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🆕 THE AVATAR: Manifests the cloud photo lively
                AsyncImage(
                    model = photoUri ?: "https://via.placeholder.com/150", // Fallback placeholder
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.2f)),
                    contentScale = ContentScale.Crop
                )



                Text(
                    text = shelterName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )
                if (!timeAgo.isNullOrEmpty()) {
                    Text(
                        text = timeAgo,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastMessage ?: "No messages yet",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFD95C5C))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}