package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.Firebase_Utils.PetsRepository
import com.example.pawmate_ils.firebase_models.Channel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.example.pawmate_ils.firebase_models.User


private fun formatChatTimestamp(timestampMs: Long): Pair<String, Boolean> {
    if (timestampMs <= 0L) return "" to false
    val now = System.currentTimeMillis()
    val diff = now - timestampMs
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < minute -> "Just now" to true
        diff < hour -> "${diff / minute}m ago" to true
        diff < day -> {
            val calNow = java.util.Calendar.getInstance()
            val calMsg = java.util.Calendar.getInstance().apply { timeInMillis = timestampMs }
            if (calNow.get(java.util.Calendar.DAY_OF_YEAR) == calMsg.get(java.util.Calendar.DAY_OF_YEAR)) {
                java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(java.util.Date(timestampMs)) to false
            } else "Yesterday" to false
        }
        diff < 7 * day -> java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault()).format(java.util.Date(timestampMs)) to false
        else -> java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault()).format(java.util.Date(timestampMs)) to false
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionCenterDashboard(
    navController: NavController,
    centerName: String,
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    adoptionCenterViewModel: AdoptionCenterViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val channels by homeViewModel.channels.collectAsState(initial = emptyList())
    val pets by adoptionCenterViewModel.shelterPets.collectAsState(initial = emptyList())
    val uploadedCount by adoptionCenterViewModel.uploadedPetsCount.collectAsState()


    LaunchedEffect(Unit) {
        homeViewModel.listenToChannels()
        val currentShelterId = authViewModel.currentUser?.uid ?: ""
        if (currentShelterId.isNotEmpty()) {
            adoptionCenterViewModel.listenToUploadedPetsCount(currentShelterId)
        }
    }

    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(60_000)
            tick++
        }
    }



    StatelessFullDashboard(
        selectedTab = selectedTab,
        channels = channels,
        petCount = uploadedCount,
        centerName = centerName,
        navController = navController,
        onDeleteChannel = { ch -> homeViewModel.deleteChannel(ch) },
        authViewModel = authViewModel,
        homeViewModel = homeViewModel // Pass it here
    )

    ProfileRequirementDialog(
        authViewModel = authViewModel,
        navController = navController,
        canShow = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatelessFullDashboard(
    selectedTab: Int,
    channels: List<Channel>,
    petCount: Int,
    centerName: String,
    navController: NavController,
    onDeleteChannel: (Channel) -> Unit,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel // Added parameter
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Dashboard",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD67A7A),
                                    fontSize = 32.sp
                                )
                            )
                            Text(text = "Welcome back!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("ShelterProfSettings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { paddingValues ->
            StatelessDashboardContent(
                paddingValues = paddingValues,
                navController = navController,
                channels = channels,
                petCount = petCount,
                onDeleteChannel = onDeleteChannel,
                homeViewModel = homeViewModel, // Now available to pass
                authViewModel = authViewModel,
            )
        }

        FloatingNavBar(
            navController = navController,
            selectedTab = selectedTab,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun FloatingNavBar(
    navController: NavController,
    selectedTab: Int,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Triple("Home", Icons.Default.Dashboard, "adoption_center_dashboard"),
        Triple("Add", Icons.Default.Pets, "add_pet"),
        Triple("Manage", Icons.AutoMirrored.Filled.List, "adoption_center_pets")
    )

    Card(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .fillMaxWidth()
            .height(82.dp),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, (label, icon, route) ->
                val isSelected = selectedTab == index
                val accentColor = Color(0xFFD67A7A)

                Column(
                    modifier = Modifier
                        .clickable {
                            if (!isSelected) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) accentColor else Color.LightGray,
                        modifier = Modifier.size(26.dp)
                    )

                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) accentColor else Color.LightGray
                    )

                    if (isSelected) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(accentColor, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun StatelessDashboardContent(
    paddingValues: PaddingValues,
    navController: NavController,
    channels: List<Channel>,
    petCount: Int,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    onDeleteChannel: (Channel) -> Unit
) {
    val sortedChannels by remember(channels) {
        derivedStateOf {
            channels.sortedWith(
                compareByDescending<Channel> { it.isPriority }
                    .thenByDescending { it.timestamp }
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(title = "Uploaded Pets", value = petCount.toString(), icon = Icons.Default.Pets, color = Color(0xFFD67A7A), modifier = Modifier.weight(1f))
                StatCard(title = "Application", value = channels.size.toString(), icon = Icons.AutoMirrored.Filled.List, color = Color(0xFFB4C7BC), modifier = Modifier.weight(1f))
            }
        }
        item {
            Text(
                text = "Recent Applications",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            )
        }

        if (sortedChannels.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("No applications yet", color = Color.Gray, fontWeight = FontWeight.Medium)
                    Text("Your pet listings will appear here once matched.", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }

        items(
            items = sortedChannels,
            key = { it.channelId }
        ) { channel ->
            ChannelCardDesign(
                channel = channel,
                onClick = { navController.navigate("message/${channel.channelId}") },
                onDelete = { onDeleteChannel(it) },
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                navController = navController,
            )
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))
        }
        item { Spacer(modifier = Modifier.height(110.dp)) }
    }
}
@Composable
fun ChannelCardDesign(
    channel: Channel,
    onClick: () -> Unit,
    onDelete: (Channel) -> Unit,
    navController: NavController,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel
){
    val isLive = authViewModel.isUserActuallyOnline(
        User(isOnline = channel.isOnline, lastActive = channel.lastActive)
    )

    val (timeLabel, isRecent) = formatChatTimestamp(channel.timestamp)
    val currentUserId = authViewModel.currentUser?.uid ?: ""
    val shouldShowBadge = channel.unreadCount > 0 && channel.lastSenderId != currentUserId
    val petsDisplay = channel.petNames.joinToString(", ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                homeViewModel.resetUnreadCount(channel.channelId)
                onClick()
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(50.dp)) {
            coil.compose.AsyncImage(
                model = channel.adopterPhotoUri ?: "https://via.placeholder.com/150",
                contentDescription = "Adopter Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable {
                        if (!channel.adopterId.isNullOrEmpty()) {
                            navController.navigate("profile_details/${channel.adopterId}")
                        }
                    }
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80))
                        .border(2.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 🎯 FIXED NESTING: Everything below is now inside this Column
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(), // Added this so SpaceBetween works
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = channel.adopterName, fontWeight = FontWeight.Bold)

                    if (channel.isPriority && channel.adopterTier == 3) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "Priority",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "VIP",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                // 🕒 Your Time Label
                if (timeLabel.isNotEmpty()) {
                    Text(
                        text = timeLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isRecent) Color(0xFFD67A7A) else Color.Gray
                    )
                }
            }

            // 🎯 These were floating outside; now they are correctly stacked inside the Column
            Text(
                text = if (petsDisplay.isNotEmpty()) "Interested in: $petsDisplay" else "New Match",                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = channel.lastMessage.ifEmpty { "No messages yet" },
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (shouldShowBadge) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF2DDA53),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = channel.unreadCount.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        IconButton(onClick = { onDelete(channel) }) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}

