    package com.example.pawmate_ils

    import androidx.compose.animation.core.Animatable
    import androidx.compose.animation.core.FastOutSlowInEasing
    import androidx.compose.animation.core.LinearEasing
    import androidx.compose.animation.core.RepeatMode
    import androidx.compose.animation.core.animateFloat
    import androidx.compose.animation.core.infiniteRepeatable
    import androidx.compose.animation.core.rememberInfiniteTransition
    import androidx.compose.animation.core.tween
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.LinearProgressIndicator
    import androidx.compose.material3.Surface
    import androidx.compose.material3.Text
    import androidx.compose.runtime.*
    import androidx.compose.ui.draw.scale
    import androidx.compose.ui.graphics.Brush
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.graphicsLayer
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
        userType: String
    ) {
        val progressAnim = remember { Animatable(0f) }
        val fadeAnim = remember { Animatable(1f) }
        val infiniteTransition = rememberInfiniteTransition(label = "welcome_logo")
        val logoScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logo_scale"
        )

        LaunchedEffect(Unit) {
            delay(80L)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
            )
            delay(350L)
            fadeAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            )

            val destination = when (userType.lowercase()) {
                "adopter" -> "pet_swipe"
                "shelter" -> "adoption_center_dashboard"
                else -> "user_type"
            }

            navController.navigate(destination) {
                popUpTo("welcome_popup") { inclusive = true }
            }
        }

        val roleLabel = if (userType.lowercase() == "shelter") "Shelter Mode" else "Adopter Mode"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = fadeAnim.value }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF0F5),
                            Color(0xFFFFE4EC),
                            Color(0xFFFFD6E3),
                            Color(0xFFFFC6D9)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.88f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .width(320.dp)
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Welcome Image",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(180.dp)
                            .height(150.dp)
                            .scale(logoScale),
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(id = R.drawable.pawmate_logo_alt),
                        contentDescription = "Welcome Image",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(70.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = "PawMate",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB35C7D),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDDE8)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = roleLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB35C7D),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = "Welcome back! Preparing your PawMate experience...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6D4C5A),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    LinearProgressIndicator(
                        progress = { progressAnim.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFFFF9DB8),
                        trackColor = Color(0xFFFFEAF1)
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
