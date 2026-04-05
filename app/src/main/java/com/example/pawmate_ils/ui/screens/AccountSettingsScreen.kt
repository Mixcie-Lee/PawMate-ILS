package com.example.pawmate_ils.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.AdopShelDataStruc.AdopterRepository
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.ThemeManager
import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var currentPasswordProfile by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
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

    val currentUserProfile by authViewModel.userData.collectAsState(initial = null)

    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }
    LaunchedEffect(Unit) {
        isDarkMode = ThemeManager.isDarkMode
    }

    LaunchedEffect(currentUserProfile) {
        currentUserProfile?.let { user ->
            // We only populate if the local fields are blank to avoid overwriting user input
            if (email.isBlank()) email = user.email ?: ""
            // Matches the 'MobileNumber' field name in your User model
            if (phone.isBlank()) phone = user.MobileNumber ?: ""
        }
    }

    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB8B8B8) else Color(0xFF5A5A5A)
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentPink = Color(0xFFE84D7A)

    val fieldShape = RoundedCornerShape(14.dp)
    val pillShape = RoundedCornerShape(28.dp)

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = primaryColor,
        unfocusedBorderColor = Color.Gray.copy(alpha = 0.25f),
        cursorColor = primaryColor,
        focusedLabelColor = secondaryTextColor,
        unfocusedLabelColor = secondaryTextColor.copy(alpha = 0.85f),
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
        focusedContainerColor = cardColor,
        unfocusedContainerColor = cardColor
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Account settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            placeholder = { Text(currentUserProfile?.email ?: "example@mail.com") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = fieldShape,
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone number") },
                            placeholder = { Text(currentUserProfile?.MobileNumber ?: "09XXXXXXXXX") }, // Placeholder
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = fieldShape,
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = currentPasswordProfile,
                            onValueChange = { currentPasswordProfile = it },
                            label = { Text("Current password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = fieldShape,
                            colors = fieldColors
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
                                        user.reload().await()
                                        val credential = EmailAuthProvider.getCredential(
                                            user.email!!,
                                            currentPasswordProfile.trim()
                                        )
                                        user.reauthenticate(credential).await()
                                        Log.d("AccountSettings", "User reauthenticated successfully")

                                        if (email.isNotBlank() && email != user.email) {
                                            authViewModel.updateEmailInFirestore(
                                                email,
                                                currentPasswordProfile.trim()
                                            ) { success, message ->
                                                Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT).show()
                                                Log.d("AccountSettings", "updateEmailInFirestore success=$success")
                                            }
                                        }

                                        if (phone.isNotBlank()) {
                                            authViewModel.updatePhoneNumberInFirestore(phone) { success, message ->
                                                Toast.makeText(context, message ?: "", Toast.LENGTH_SHORT).show()
                                                Log.d("AccountSettings", "updatePhoneNumberInFirestore success=$success")
                                            }
                                        }

                                        val updatedAdopter = User(
                                            id = user.uid,
                                            email = if (email.isNotBlank()) email else user.email!!,
                                            MobileNumber = phone
                                        )
                                        adopterRepository.updateAdopter(updatedAdopter)
                                        Log.d("AccountSettings", "Adopter collection updated: $updatedAdopter")

                                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("AccountSettings", "Failed to update profile", e)
                                        Toast.makeText(context, "Incorrect password or update failed", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isUpdating = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = !isUpdating,
                            shape = pillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentPink,
                                contentColor = Color.White
                            )
                        ) {
                            if (isUpdating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text("Save profile", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Text(
                    text = "Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Current password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            shape = fieldShape,
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            shape = fieldShape,
                            colors = fieldColors
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm new password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            shape = fieldShape,
                            colors = fieldColors
                        )

                        val canUpdatePassword =
                            !isLoading && newPassword.isNotBlank() && newPassword == confirmPassword

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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = canUpdatePassword,
                            shape = pillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canUpdatePassword) accentPink else Color.Gray.copy(alpha = 0.35f),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.28f),
                                disabledContentColor = Color.White.copy(alpha = 0.65f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text("Update password", fontWeight = FontWeight.SemiBold)
                        }

                        passwordFeedback?.let { feedback ->
                            Text(
                                feedback,
                                color = if (feedback.contains("success", true)) Color(0xFF4ADE80) else accentPink,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Danger zone",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentPink,
                    modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    onClick = {
                        isLoggingOut = true
                        com.example.pawmate_ils.GemManager.clearData()
                        authViewModel.signOut(context) {
                            isLoggingOut = false
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            navController.navigate("login") {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isLoggingOut) "Logging out…" else "Log out",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentPink
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = accentPink,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp)
                        .height(52.dp),
                    shape = pillShape,
                    border = BorderStroke(1.dp, accentPink.copy(alpha = 0.85f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentPink
                    )
                ) {
                    Text("Delete account", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Confirm account deletion") },
            text = {
                Column {
                    Text("This will permanently delete your account and all associated data.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Current password") },
                        visualTransformation = PasswordVisualTransformation(),
                        enabled = !isDeleting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    passwordFeedback?.let { feedback ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(feedback, color = accentPink, fontSize = 13.sp)
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
                                navController.navigate("login") {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        }
                    },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = accentPink, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Delete", color = accentPink, fontWeight = FontWeight.SemiBold)
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
