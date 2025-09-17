package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import com.example.pawmate_ils.SharedViewModel
import com.example.pawmate_ils.ui.theme.DarkBrown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    onSignUpClick: (String, String, String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    onSellerAuthClick: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            Text(
                text = "PawMate",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "Create an account",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    fontSize = 18.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Enter your details to sign up for this app",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text("Full Name", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("name@example.com", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text("Address", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                placeholder = { Text("Mobile Number", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = {
                    when {
                        fullName.isBlank() -> {
                            errorMessage = "Please enter your full name"
                            return@Button
                        }
                        email.isBlank() -> {
                            errorMessage = "Please enter your email"
                            return@Button
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorMessage = "Please enter a valid email address"
                            return@Button
                        }
                        password.isBlank() -> {
                            errorMessage = "Please enter your password"
                            return@Button
                        }
                        password.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                            return@Button
                        }
                        address.isBlank() -> {
                            errorMessage = "Please enter your address"
                            return@Button
                        }
                        mobileNumber.isBlank() -> {
                            errorMessage = "Please enter your mobile number"
                            return@Button
                        }
                        else -> {
                            errorMessage = null
                            isLoading = true
                            scope.launch {
                                try {
                                    onSignUpClick(fullName, email, password, address, mobileNumber)
                                    sharedViewModel.username.value = fullName
                                    delay(50)
                                    navController.navigate("pet_selection") {
                                        popUpTo("user_type") { inclusive = true }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Sign up failed: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkBrown,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "or",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            OutlinedButton(
                onClick = { /* Handle Google Sign In */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "G",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Continue with Google",
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { /* Handle Apple Sign In */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🍎",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Continue with Apple",
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Log in",
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "By clicking continue, you agree to our Terms of Service and Privacy Policy.",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}