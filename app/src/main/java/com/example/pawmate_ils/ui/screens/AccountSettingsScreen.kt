package com.example.pawmate_ils.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.AdopShelDataStruc.AdopterRepository
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.firebase_models.User
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var currentPasswordProfile by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var deletePassword by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var passwordFeedback by remember { mutableStateOf<String?>(null) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current
    val adopterRepository = AdopterRepository()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
            ,

            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /** PROFILE UPDATE **/
            Text("Profile", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currentPasswordProfile,
                onValueChange = { currentPasswordProfile = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )


            Button(
                onClick = {
                    val user = authViewModel.currentUser
                    if (user == null) {
                        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                        Log.e("AccountSettings", "User not logged in")
                        return@Button
                    }
                    if (currentPasswordProfile.isBlank()) {
                        Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
                        Log.e("AccountSettings", "Current password for profile update is blank")
                        return@Button
                    }

                    isUpdating = true
                    coroutineScope.launch {
                        try {
                            Log.d("AccountSettings", "Reloading user")
                            user.reload().await()

                            // Reauthenticate using current password
                            Log.d("AccountSettings", "Reauthenticating user")
                            val credential = EmailAuthProvider.getCredential(user.email!!, currentPasswordProfile.trim())
                            user.reauthenticate(credential).await()

                            // --- UPDATE EMAIL IN AUTH ---
                            if (email.isNotBlank() && email != user.email) {
                                Log.d("AccountSettings", "Updating email in Firebase Auth to $email")
                                user.updateEmail(email).await()
                            }

                            // --- UPDATE EMAIL IN FIRESTORE ---
                            Log.d("AccountSettings", "Updating email in Firestore")
                            authViewModel.updateEmailInFirestore(email, currentPasswordProfile.trim()) { success, message ->
                                Log.d("AccountSettings", "updateEmailInFirestore success=$success message=$message")
                                Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT).show()
                            }

                            // --- UPDATE PHONE IN FIRESTORE ---
                            if (phone.isNotBlank()) {
                                Log.d("AccountSettings", "Updating phone number in Firestore to $phone")
                                authViewModel.updatePhoneNumberInFirestore(phone) { success, message ->
                                    Log.d("AccountSettings", "updatePhoneNumberInFirestore success=$success message=$message")
                                    Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT).show()
                                }
                            }

                            // --- UPDATE LOCAL ADOPTER COLLECTION ---
                            val updatedAdopter = User(
                                id = user.uid,
                                email = email,
                                MobileNumber = phone // phone number update
                            )
                            Log.d("AccountSettings", "Updating adopter collection with $updatedAdopter")
                            adopterRepository.updateAdopter(updatedAdopter)

                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            Log.d("AccountSettings", "Profile updated successfully")

                        } catch (e: Exception) {
                            Log.e("AccountSettings", "Failed to update profile", e)
                            Toast.makeText(context, "Incorrect password or update failed", Toast.LENGTH_SHORT).show()
                        } finally {
                            isUpdating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1), contentColor = Color.White)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Updating…")
                } else {
                    Text("Save Profile")
                }
            }

            Divider()

            /** CHANGE PASSWORD **/
            Text("Change Password", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Button(
                onClick = {
                    passwordFeedback = null
                    if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                        passwordFeedback = "Please fill in all fields"
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        passwordFeedback = "Passwords do not match"
                        return@Button
                    }
                    isLoading = true
                    authViewModel.updatePassword(currentPassword, newPassword) { success, message ->
                        isLoading = false
                        passwordFeedback = message
                        if (success) {
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        }
                    }
                },
                enabled = !isLoading && newPassword.isNotBlank() && newPassword == confirmPassword,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB6C1), contentColor = Color.White)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Updating…")
                } else {
                    Text("Update Password")
                }
            }
            passwordFeedback?.let { feedback ->
                Text(
                    feedback,
                    color = if (feedback.contains("success", true)) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Divider()

            /** DANGER ZONE: LOGOUT & DELETE **/
            Text("Danger Zone", style = MaterialTheme.typography.titleMedium, color = Color.Red)

            Button(
                onClick = {
                    isLoggingOut = true
                    authViewModel.signOut()
                    isLoggingOut = false
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate("signup") {
                        popUpTo(0)
                    }
                },
                enabled = !isLoggingOut,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White)
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logging out…")
                } else {
                    Text("Log out")
                }
            }

            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
            ) {
                Text("Delete Account")
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
                    title = { Text("Confirm Account Deletion") },
                    text = {
                        Column {
                            Text("This will permanently delete your account and all associated data.")
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = deletePassword,
                                onValueChange = { deletePassword = it },
                                label = { Text("Enter current password") },
                                visualTransformation = PasswordVisualTransformation(),
                                enabled = !isDeleting
                            )
                            passwordFeedback?.let { feedback ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(feedback, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (deletePassword.isBlank()) {
                                    passwordFeedback = "Please enter your password"
                                    return@TextButton
                                }
                                isDeleting = true
                                passwordFeedback = null
                                authViewModel.deleteAccountAndData(context, deletePassword) { success, message ->
                                    isDeleting = false
                                    passwordFeedback = message
                                    if (success) {
                                        navController.navigate("signup") {
                                            popUpTo(0)
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            enabled = !isDeleting
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(color = Color.Red, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Delete", color = Color.Red)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { if (!isDeleting) showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}



