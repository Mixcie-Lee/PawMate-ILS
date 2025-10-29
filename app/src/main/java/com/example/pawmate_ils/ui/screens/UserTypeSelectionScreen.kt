    package com.example.pawmate_ils.ui.screens

    import android.net.Uri
    import android.util.Log
    import android.widget.Toast
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.runtime.livedata.observeAsState
    import androidx.compose.runtime.setValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavController
    import com.example.pawmate_ils.Firebase_Utils.AuthState
    import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
    import com.example.pawmate_ils.Firebase_Utils.FirestoreRepository
    import com.example.pawmate_ils.SettingsManager
    import com.example.pawmate_ils.onboard.OnboardingUtil
    import com.example.pawmate_ils.ui.theme.DarkBrown
    import com.example.pawmate_ils.ui.theme.LightGray
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.tasks.await
    import kotlinx.coroutines.withContext

    @Composable
    fun UserTypeSelectionScreen(
        navController: NavController,
        authViewModel: AuthViewModel = viewModel(),
        firestoreRepo: FirestoreRepository = FirestoreRepository(),
    ) {
        //Firebase initialization and tools
        val authState = authViewModel.authState.observeAsState()
        val context = LocalContext.current
        val onboardingUtil = OnboardingUtil(context)
        val settings = remember { SettingsManager(context) }
        var editableName by remember { mutableStateOf(settings.getUsername()) }
        var profilePhotoUri by remember { mutableStateOf(settings.getProfilePhotoUri()?.let { Uri.parse(it) }) }


        LaunchedEffect(authState) {
            // Check if onboarding is already done
            if (onboardingUtil.isOnboardingCompleted()) {
                val currentUser = authViewModel.currentUser
                if (currentUser != null) {
                    try {
                            authViewModel.syncUserDataLocal(context)
                        editableName = settings.getUsername()
                        profilePhotoUri = settings.getProfilePhotoUri()?.let { Uri.parse(it) }
                        val snapshot = withContext(Dispatchers.IO) {
                            firestoreRepo.getUserRole(currentUser.uid)
                        }

                        // Read the user's role
                        val role = snapshot?.getString("role")
                        // Decide where to navigate based on the role
                        val destination = when (role) {
                            "adopter" -> "pet_swipe"
                            "shelter" -> "adoption_center_dashboard"
                            else -> "user_type"
                        }

                        // Navigate if role found
                        if (destination != "user_type") {
                            navController.navigate(destination) {
                                popUpTo("user_type") { inclusive = true }
                            }
                        }

                    } catch (e: Exception) {
                        // Handle errors (network, Firestore, etc.)
                        Toast.makeText(
                            context,
                            "Error loading user info: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }





        var selectedOption by remember { mutableStateOf<String?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Are you a...",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Normal,
                        color = Color.Black,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Adopter Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOption == "adopter") LightGray else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    onClick = { selectedOption = "adopter" }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Adopter?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                fontSize = 20.sp
                            )
                        )
                    }
                }

                // Animal Shelter Owner Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedOption == "shelter") LightGray else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    onClick = { selectedOption = "shelter" }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Animal Shelter Owner?",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Normal,
                                color = Color.Black,
                                fontSize = 20.sp
                            )
                        )
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        when (selectedOption) {
                            "adopter" -> navController.navigate("signup")
                            "shelter" -> navController.navigate("seller_signup")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkBrown,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = selectedOption != null
                ) {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }