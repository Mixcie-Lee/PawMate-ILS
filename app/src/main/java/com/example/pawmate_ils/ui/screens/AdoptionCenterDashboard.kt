package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pawmate_ils.ui.theme.PetPink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionCenterDashboard(
    centerName: String,
    onAddPet: () -> Unit,
    onViewPets: () -> Unit,
    onViewApplications: () -> Unit,
    onViewStatistics: () -> Unit,
    onSettings: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Pets", "Applications", "Statistics")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = centerName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PetPink
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
                                    else -> Icons.Default.Analytics
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index
                            when (index) {
                                2 -> onViewApplications()
                                3 -> onViewStatistics()
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardContent(
                paddingValues = paddingValues,
                onAddPet = onAddPet,
                onViewPets = onViewPets
            )
            1 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Pets tab content coming soon")
                }
            }
            2 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Applications tab content coming soon")
                }
            }
            3 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    Text("Statistics tab content coming soon")
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    paddingValues: PaddingValues,
    onAddPet: () -> Unit,
    onViewPets: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Total Pets",
                    value = "24",
                    icon = Icons.Default.Pets,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Applications",
                    value = "12",
                    icon = Icons.AutoMirrored.Filled.List,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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

        item {
            Text(
                text = "Recent Applications",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        items(listOf("John Doe", "Jane Smith", "Mike Johnson")) { applicant ->
            ApplicationCard(
                applicantName = applicant,
                petName = "Buddy",
                date = "Today",
                status = "Pending"
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PetPink.copy(alpha = 0.1f)
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
                tint = PetPink,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PetPink
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
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
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = PetPink
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text)
        }
    }
}

@Composable
fun ApplicationCard(
    applicantName: String,
    petName: String,
    date: String,
    status: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = applicantName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Pet: $petName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when (status) {
                                "Pending" -> Color(0xFFFFA500)
                                "Approved" -> Color(0xFF4CAF50)
                                "Rejected" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    )
                }
            }
        }
    }
} 