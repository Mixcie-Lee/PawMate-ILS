package com.example.pawmate_ils.chatScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.ChatViewModel
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    chatViewModel: ChatViewModel
) {
    val homeViewModel: HomeViewModel = viewModel()
    val channels by homeViewModel.channels.collectAsState()
    val context = LocalContext.current
    val currentUserRole by authViewModel.currentUserRole.collectAsState()

    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val navBarColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentColor = if (isDarkMode) Color(0xFFB39DDB) else Color(0xFFDDA0DD)
    // ✅ Fetch the role once when screen loads
    LaunchedEffect(Unit) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            authViewModel.fetchUserRole() // ✅ Pass UID, not context
        }
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
                        icon = {
                            Icon(
                                Icons.Default.Pets,
                                contentDescription = "Swipe",
                                tint = Color(0xFFFF9999)
                            )
                        },
                        label = {
                            Text(
                                "Swipe",
                                color = Color(0xFFFF9999),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        selected = true,
                        onClick = {
                            navController.navigate("pet_swipe")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9999),
                            selectedTextColor = Color(0xFFFF9999),
                            indicatorColor = Color(0xFFFFD6E0)
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = com.example.pawmate_ils.R.drawable.heart),
                                contentDescription = "Liked",
                                modifier = Modifier.size(24.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Liked", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
                            navController.navigate("adopter_home")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9999),
                            selectedTextColor = Color(0xFFFF9999),
                            indicatorColor = Color(0xFFFFD6E0)
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = com.example.pawmate_ils.R.drawable.book_open),
                                contentDescription = "Learn",
                                modifier = Modifier.size(24.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Learn", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
                            navController.navigate("educational")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9999),
                            selectedTextColor = Color(0xFFFF9999),
                            indicatorColor = Color(0xFFFFD6E0)
                        )
                    )
                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = com.example.pawmate_ils.R.drawable.profile_d),
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Profile", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
                            navController.navigate("profile_settings")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9999),
                            selectedTextColor = Color(0xFFFF9999),
                            indicatorColor = Color(0xFFFFD6E0)
                        )
                    )

                    NavigationBarItem(
                        icon = {
                            Image(
                                painter = painterResource(id = R.drawable.message_square),
                                contentDescription = "Message",
                                modifier = Modifier.size(24.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                    Color.Gray.copy(alpha = 0.6f)
                                )
                            )
                        },
                        label = { Text("Message", color = Color.Gray.copy(alpha = 0.6f)) },
                        selected = false,
                        onClick = {
                            navController.navigate("chat_home")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFFF9999),
                            selectedTextColor = Color(0xFFFF9999),
                            indicatorColor = Color(0xFFFFD6E0)
                        )
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
                            text = "Message",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color.White
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = if (isDarkMode) primaryColor else Color(0xFFFFB6C1) // 🌸 Pink for light mode
                    )
                )


                when {
                    currentUserRole.isNullOrEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    channels.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No messages yet",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(channels) { channel ->
                                val displayName = when (currentUserRole) {
                                    "shelter" -> "${channel.adopterName} - ${channel.petName.ifEmpty { "Pet" }}"
                                    "adopter" -> "${channel.shelterName} - ${channel.petName.ifEmpty { "Pet" }}"
                                    else -> "Unknown - ${channel.petName.ifEmpty { "Pet" }}"
                                }

                                ChatListItem(
                                    shelterName = displayName,
                                    lastMessage = channel.lastMessage,
                                    timeAgo = null,
                                    unreadCount = channel.unreadCount,
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

@Composable
fun BottomBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFE4E1))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Bottom navigation (Placeholder)", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun ChatListItem(
    shelterName: String,
    lastMessage: String?,
    timeAgo: String?,
    unreadCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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

@Preview(showBackground = true)
@Composable
fun ChatListItemPreview() {
    ChatListItem(
        shelterName = "Happy Paws Shelter",
        lastMessage = "Thank you for adopting Max!",
        timeAgo = "2h ago",
        unreadCount = 3,
        onClick = {}
    )
}
