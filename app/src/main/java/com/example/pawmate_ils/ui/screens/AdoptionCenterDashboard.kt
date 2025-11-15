package com.example.pawmate_ils.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.Firebase_Utils.HomeViewModel
import com.example.pawmate_ils.Firebase_Utils.PetRepository
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.firebase_models.Channel
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.ui.models.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionCenterDashboard(
    navController: NavController,
    centerName: String
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Pets", "Applications",)
    //Firebase initialization
    val authViewModel : AuthViewModel = viewModel()
    val sharedViewModel : SharedViewModel = viewModel ()
    val authState  = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    //Channel initialization
    val homeViewModel: HomeViewModel = viewModel()
    val channels by homeViewModel.channels.collectAsState()
    LaunchedEffect(Unit) {
        homeViewModel.listenToChannels()
    }

    //observe all pets, i planned to change the available pets number into count of how many pets were
    //added hehe
    val petRepository: PetRepository = viewModel()
    val pets by petRepository.allPets.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = centerName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBrown
                            )
                        )
                        Text(
                            text = "Welcome back!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Pets
                                    2 -> Icons.AutoMirrored.Filled.List
                                    else ->Icons.Default.Home
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (index) {
                                0 -> navController.navigate("adoption_center_dashboard") {
                                    popUpTo("adoption_center_dashboard") { inclusive = true }
                                    launchSingleTop = true
                                }

                                1 -> navController.navigate("adoption_center_pets") {
                                    popUpTo("adoption_center_dashboard")
                                    launchSingleTop = true
                                }

                                2 -> navController.navigate("adoption_center_applications") {
                                    popUpTo("adoption_center_dashboard")
                                    launchSingleTop = true
                                }

                                3 -> navController.navigate("chat_home") {
                                    popUpTo("adoption_center_dashboard")
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
           0 ->  DashboardContent(
                paddingValues = paddingValues,
                navController = navController,
                onAddPet = { navController.navigate("add_pet") },
                onViewPets = { navController.navigate("adoption_center_pets") },
                onViewApplications = { navController.navigate("adoption_center_applications") },
                channels = channels,
                homeViewModel = homeViewModel // pass it here
            )
            1, 2, 3 -> {
                // Navigation handled by bottom bar
            }
        }
    }
}

@Composable
private fun DashboardContent(
    paddingValues: PaddingValues,
    navController: NavController,
    onAddPet: () -> Unit,
    onViewPets: () -> Unit,
    onViewApplications: () -> Unit,
    channels: List<Channel>,
    homeViewModel : HomeViewModel
) {
    //observe all pets, i planned to change the available pets number into count of how many pets were
    //added hehe
    val petRepository: PetRepository = viewModel()
    val pets by petRepository.allPets.collectAsState()


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Statistics
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Uploaded Pets",
                    value = pets.size.toString(),
                    icon = Icons.Default.Pets,
                    color = DarkBrown,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Applications",
                    value = channels.size.toString(),
                    icon = Icons.AutoMirrored.Filled.List,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

            }
        }

        // Quick Actions
        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    text = "Add Pet",
                    icon = Icons.Default.Add,
                    onClick = onAddPet,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    text = "View Pets",
                    icon = Icons.Default.Pets,
                    onClick = onViewPets,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Applications
        item {
            Text(
                text = "Recent Applications",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(channels, key = { it.channelId }) { channel ->
            ChannelCard(
                channel = channel,
                onClick = {
                    navController.navigate("message/${channel.channelId}")
                },
                onDelete = { ch ->
                    homeViewModel.deleteChannel(ch)
                }
            )
        }

        // Adoption Tips
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBrown.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Tip",
                        tint = DarkBrown,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Pro Tip",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBrown
                            )
                        )
                        Text(
                            text = "Regular updates to pet profiles can increase adoption rates by 40%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ChannelCard(
    channel: Channel,
    onClick: () -> Unit,
    onDelete: (Channel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable{ onClick() }
            ) {
                Text(
                    text = channel.adopterName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = channel.petName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = channel.lastMessage.takeIf { it.isNotEmpty() } ?: "No messages yet",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            }

            if (channel.unreadCount > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = channel.unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            IconButton(
                onClick = { onDelete(channel) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete channel",
                    tint = Color.Red
                )
            }
        }
    }
}







@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkBrown
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationCard(
    application: Application,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = application.applicantName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${application.petName} • ${application.petType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = application.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = when (application.status) {
                    "Pending" -> Color(0xFFFFA000)
                    "Approved" -> Color(0xFF4CAF50)
                    else -> Color(0xFFF44336)
                }
            ) {
                Text(
                    text = application.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}