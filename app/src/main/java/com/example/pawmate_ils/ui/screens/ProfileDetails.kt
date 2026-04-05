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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFFB6C1))
        }
    } else {
        val user = userState!!
        val scrollState = rememberScrollState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
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
                    // 🎨 DESIGN: Soft Pink Vertical Gradient from Colleague's layout
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFFFE4E9), Color(0xFFFFD1DC))
                        )
                    )
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
                                .border(4.dp, Color.White, CircleShape),
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
                                    .border(3.dp, Color.White, CircleShape)
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
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = if (user.role == "shelter") "Verified Shelter" else "${user.Age} years old",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

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
                            color = Color(0xFFFFD6D6).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFFF9999)),
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
                                    tint = Color(0xFFB35C7D) // Matches your PawMate theme pink
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Shelter hours: $formattedHours",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray
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
                        value = user.Address.ifBlank { "No address provided" }
                    )

                    DetailItem(
                        label = "Contacts",
                        value = "${user.MobileNumber.ifBlank { "No mobile number" }}\n${user.email}"
                    )

                    DetailItem(
                        label = if (user.role == "shelter") "About Us" else "About Me",
                        value = user.aboutMe.ifBlank { "This user hasn't added a bio yet." }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // --- 4. PRIMARY ACTION BUTTON ---


                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 17.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium
        )
    }
}