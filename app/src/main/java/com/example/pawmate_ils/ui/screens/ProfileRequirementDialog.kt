package com.example.pawmate_ils.ui.screens
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

@Composable
fun ProfileRequirementDialog(
    authViewModel: AuthViewModel,
    navController: NavController,
    canShow : Boolean
) {
    // 1. Observe the Firestore data stream
    val userData by authViewModel.userData.collectAsState()

    // 2. Local UI state
    var showWelcomeDialog by remember { mutableStateOf(false) }
    var hasPromptedThisSession by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    // 3. Logic: Wait for Firestore data to load before checking photoUri
    LaunchedEffect(userData, canShow) {
        val user = userData
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (canShow && firebaseUser != null && user != null && !hasPromptedThisSession) {
            // Check for blank string (default in your User model)
            if (user.photoUri.isBlank()) {
                showWelcomeDialog = true
                hasPromptedThisSession = true
            }
        }
    }

    if (showWelcomeDialog) {
        AlertDialog(
            onDismissRequest = { showWelcomeDialog = false },
            title = { Text("Complete Your Profile 🐾", fontWeight = FontWeight.Bold) },
            text = { Text("PawMate works best when others can see who they are matching with! Would you like to upload a profile picture now?") },
            confirmButton = {
                Button(
                    onClick = {
                        showWelcomeDialog = false
                        // Role-based navigation logic
                        val destination = if (userData?.role == "shelter") "ShelterProfSettings" else "profile_settings"
                        navController.navigate(destination)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1))
                ) {
                    Text("Proceed", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWelcomeDialog = false }) {
                    Text("Later", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}