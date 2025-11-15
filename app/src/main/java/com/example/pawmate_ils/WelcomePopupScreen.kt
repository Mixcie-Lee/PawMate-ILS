    package com.example.pawmate_ils

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import androidx.navigation.compose.rememberNavController
    import kotlinx.coroutines.delay
    @Composable
    fun WelcomePopupScreen(
        navController: NavController,
        userType: String // "adopter" or "shelter"
    ) {
        // Navigate after 3 seconds based on user type
        LaunchedEffect(Unit) {
            delay(3000L)

            val destination = when (userType.lowercase()) {
                "adopter" -> "pet_swipe"
                "shelter" -> "adoption_center_dashboard" // make it dashboard instead of pets
                else -> "user_type"
            }

            navController.navigate(destination) {
                // Remove the welcome popup from backstack
                popUpTo("welcome_popup") { inclusive = true }
            }
        }

        // Full black background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Popup card (also black)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .width(300.dp)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Main logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Welcome Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .padding(bottom = 20.dp)
                            .offset(y = -50.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Alternate logo
                    Image(
                        painter = painterResource(id = R.drawable.pawmate_logo_alt),
                        contentDescription = "Welcome Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(85.dp)
                            .padding(bottom = 16.dp),
                        contentScale = ContentScale.Crop
                    )

                    // Short text
                    Text(
                        text = "Welcome back! Get ready to continue your journey.",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun WelcomePopupPreview() {
        val navController = rememberNavController()
        WelcomePopupScreen(navController = navController, userType = "adopter")
    }
