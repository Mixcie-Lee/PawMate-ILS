package com.example.pawmate_ils.chatScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.pawmate_ils.firebase_models.Channel
import com.example.pawmate_ils.firebase_models.User
import com.example.pawmate_ils.ui.components.AdopterBottomBar

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
            AdopterBottomBar(
                navController = navController,
                selectedTab = "home"
            )
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFFB6C1))
                        }
                    }

                    channels.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
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
                                val isShelterView = currentUserRole == "shelter"
                                val currentUserId = authViewModel.currentUser?.uid ?: ""

                                // 🟢 OLD LOGIC RECOVERED: Only show badge if I am NOT the last sender
                                val shouldShowBadge = channel.unreadCount > 0 && channel.lastSenderId != currentUserId

                                val isLive = authViewModel.isUserActuallyOnline(
                                    User(isOnline = channel.isOnline, lastActive = channel.lastActive)
                                )

                                val displayName = if (isShelterView) {
                                    "${channel.adopterName} • ${channel.petName}"
                                } else {
                                    "${channel.shelterName} • ${channel.petName}"
                                }

                                val displayPhoto = if (isShelterView) channel.adopterPhotoUri else channel.shelterPhotoUri
                                val defaultSubtitle = if (isShelterView) "Interested in ${channel.petName}" else "Shelter for ${channel.petName}"
                                val displaySubtitle = if (channel.lastMessage.isNotEmpty()) channel.lastMessage else defaultSubtitle
                                if (channel.isPriority) {
                                    VIPChannelItem(
                                        displayName = displayName,
                                        displayPhoto = displayPhoto,
                                        subtitle = displaySubtitle, // ✅ Now dynamic                                        isPriority = true,
                                        isPriority = true,
                                        isLive = isLive,
                                        unreadCount = if (shouldShowBadge) channel.unreadCount else 0, // ✅ GRAFTED
                                        onClick = {
                                            homeViewModel.resetUnreadCount(channel.channelId)
                                            navController.navigate("message/${channel.channelId}")
                                        }
                                    )
                                } else {
                                    ChatListItem(
                                        shelterName = displayName,
                                        photoUri = displayPhoto,
                                        lastMessage = if (channel.lastMessage.isNotEmpty()) channel.lastMessage else "No messages yet",
                                        timeAgo = null,
                                        unreadCount = if (shouldShowBadge) channel.unreadCount else 0, // ✅ GRAFTED
                                        isLive = isLive,
                                        onAvatarClick = {
                                            // 🟢 THIS IS THE FIX: Handle the profile navigation here
                                            val targetUserId = if (currentUserRole == "shelter") channel.adopterId else channel.shelterId
                                            if (!targetUserId.isNullOrEmpty()) {
                                                navController.navigate("profile_details/$targetUserId")
                                            }
                                        },
                                        onClick = {
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
}

@Composable
fun ChatListItem(
    shelterName: String,
    photoUri: String?,
    lastMessage: String?,
    timeAgo: String?,
    unreadCount: Int,
    isLive: Boolean = false,
    onAvatarClick: () -> Unit,
    isPriority: Boolean = false,
    onClick: () -> Unit,

) {
    val isDarkMode = ThemeManager.isDarkMode
    val cardBg = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val mainTextColor = if (isDarkMode) Color.White else Color(0xFF333333)
    val subTextColor = if (isDarkMode) Color.LightGray else Color.DarkGray
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick()}
            .background(cardBg)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(55.dp).clickable{onAvatarClick()}) {
                    AsyncImage(
                        model = photoUri ?: "https://via.placeholder.com/150",
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize() // Fill the 55.dp parent
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop
                    )

                    if (isLive) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                                .border(2.dp, cardBg, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }


                Text(
                    text = shelterName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDarkMode) Color.White else Color(0xFF333333),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                if (!timeAgo.isNullOrEmpty()) {
                    Text(text = timeAgo, fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastMessage ?: "No messages yet",
                    color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 67.dp),
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
@Composable
fun VIPChannelItem(
    displayName: String,
    displayPhoto: String?,
    subtitle: String,
    isPriority: Boolean,
    isLive: Boolean,
    unreadCount: Int,
    onClick: () -> Unit
) {
    val isDarkMode = ThemeManager.isDarkMode
    // Subtle gold tint for the background instead of a thick border
    val vipBgColor = if (isDarkMode) Color(0xFF2D2A20) else Color(0xFFFFFDF0)
    val goldAccent = Color(0xFFFFD700)
    val pawMatePink = Color(0xFFD95C5C)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = vipBgColor),
        // A very thin, subtle border to keep it premium
        border = BorderStroke(1.dp, goldAccent.copy(alpha = 0.4f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Section
            Box(modifier = Modifier.size(56.dp)) {
                AsyncImage(
                    model = displayPhoto ?: "https://via.placeholder.com/150",
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.LightGray.copy(alpha = 0.2f)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.blackpawmateicon3),
                    error = painterResource(R.drawable.blackpawmateicon3)
                )

                // 👑 Subtle Crown Badge
                if (isPriority) {
                    Surface(
                        color = goldAccent,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopStart)
                            .offset(x = (-2).dp, y = (-2).dp),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👑", fontSize = 10.sp)
                        }
                    }
                }

                // Live Indicator
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80))
                            .border(2.dp, vipBgColor, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            // Info Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isDarkMode) Color.White else Color(0xFFB8860B)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            // 🔴 Notification Badge (Clean & Standardized)
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(pawMatePink)
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
