package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.pawmate_ils.ThemeManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.ProfilePhotoDefaults
import com.example.pawmate_ils.firebase_models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    targetUserId: String,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // 🎣 CATCH: Integrating your new getUserByIdFlow from AuthViewModel
    val userState by authViewModel.getUserByIdFlow(targetUserId).collectAsState(initial = null)

    if (userState == null) {
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFFB6C1))
        }
    } else {
        val user = userState!!
        val scrollState = rememberScrollState()
        val isDarkMode = ThemeManager.isDarkMode
        val pageBgColors = if (isDarkMode) {
            listOf(Color(0xFF121212), Color(0xFF1A1A1A), Color(0xFF151515))
        } else {
            listOf(Color.White, Color(0xFFFFE4E9), Color(0xFFFFD1DC))
        }
        val primaryText = if (isDarkMode) Color(0xFFF5F5F5) else Color.Black
        val labelColor = if (isDarkMode) Color(0xFFB0B0B0) else Color.Gray
        val bodyText = if (isDarkMode) Color(0xFFE8E8E8) else Color(0xFF333333)
        val ownerAccent = if (isDarkMode) Color(0xFFFF8FAB) else Color(0xFFE84D7A)
        val avatarBorder = if (isDarkMode) Color.White.copy(alpha = 0.22f) else Color.White
        val onlineRing = if (isDarkMode) Color(0xFF1A1A1A) else Color.White

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryText)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent // Allow gradient to show through
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = pageBgColors))
                    .verticalScroll(scrollState)
                    .padding(padding)
            ) {
                // --- 1. HEADER: IMAGE & FLOATING ONLINE DOT ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box {
                        AsyncImage(
                            model = user.photoUri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .border(4.dp, avatarBorder, CircleShape),
                            contentScale = ContentScale.Crop,
                            // 🛡️ FALLBACK: Using the gender-based logic we built
                            error = painterResource(
                                id = ProfilePhotoDefaults.placeholderResForGender(user.gender)
                            )
                        )

                        // 🟢 Online Status Indicator
                        if (user.isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4ADE80))
                                    .border(3.dp, onlineRing, CircleShape)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-10).dp, y = (-10).dp)
                            )
                        }
                    }
                }

                // --- 2. IDENTITY SECTION ---
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        ),
                        textAlign = TextAlign.Center
                    )

                    if (user.role == "shelter" && !user.ownerName.isNullOrBlank()) {
                        Text(
                            text = "Owned by ${user.ownerName}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = ownerAccent
                            ),
                            modifier = Modifier.padding(top = 2.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🏥 SHELTER HOURS PILL (Conditional Design)
                    if (user.role == "shelter") {
                        // 🛠️ The Logic: Ensures it looks clean even if the user typed "9AM 5PM"
                        val formattedHours = remember(user.shelterHours) {
                            val raw = user.shelterHours.trim()
                            when {
                                raw.isBlank() -> "Not specified"
                                // If they typed "9AM 5PM", this adds the dash for them visually
                                !raw.contains("-") && raw.contains(" ") -> raw.replace(" ", " - ")
                                else -> raw
                            }
                        }

                        Surface(
                            color = if (isDarkMode) Color.Black.copy(alpha = 0.35f) else Color(0xFFFFD6D6).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp),
                            border = if (isDarkMode) {
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            } else {
                                BorderStroke(1.dp, Color(0xFFFF9999))
                            },
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            // 🏁 We use a Row to put the Icon and Text side-by-side
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isDarkMode) Color.White else Color(0xFFB35C7D)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Shelter hours: $formattedHours",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDarkMode) Color.White else Color.DarkGray
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // --- 3. DYNAMIC DETAILS SECTION ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    DetailItem(
                        label = "Location",
                        value = user.Address.ifBlank { "No address provided" },
                        labelColor = labelColor,
                        valueColor = bodyText
                    )

                    DetailItem(
                        label = "Contacts",
                        value = "${user.MobileNumber.ifBlank { "No mobile number" }}\n${user.email}",
                        labelColor = labelColor,
                        valueColor = bodyText
                    )

                    DetailItem(
                        label = if (user.role == "shelter") "About Us" else "About Me",
                        value = user.aboutMe.ifBlank { "This user hasn't added a bio yet." },
                        labelColor = labelColor,
                        valueColor = bodyText
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // --- 4. PRIMARY ACTION BUTTON ---


                }
            }
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    labelColor: Color = Color.Gray,
    valueColor: Color = Color(0xFF333333)
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                color = labelColor,
                fontWeight = FontWeight.SemiBold
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                color = valueColor
            )
        )
    }
}