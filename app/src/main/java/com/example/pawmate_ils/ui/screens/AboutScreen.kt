package com.example.pawmate_ils.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.R
import com.example.pawmate_ils.ThemeManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB8B8B8) else Color(0xFF5A5A5A)
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentGradient = if (isDarkMode)
        Brush.verticalGradient(listOf(Color(0xFFFF6B8A), Color(0xFFFF9999)))
    else
        Brush.verticalGradient(listOf(Color(0xFFFFB6C1), Color(0xFFFF8FA3)))

    val context = LocalContext.current
    val supportEmail = "pawmate.support@gmail.com"
    val facebookUrl = "https://www.facebook.com/share/1AQdGpKRtS/?mibextid=wwXIfr"
    val instagramUrl = "https://www.instagram.com/_pawbridge?igsh=MTR0djk1MTJoNzZldw%3D%3D&utm_source=qr"
    val tiktokUrl = "https://www.tiktok.com/@pawmate4"

    var heroVisible by remember { mutableStateOf(false) }
    var versionVisible by remember { mutableStateOf(false) }
    var bodyVisible by remember { mutableStateOf(false) }
    var linksVisible by remember { mutableStateOf(false) }
    var footerVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        heroVisible = true
        delay(200L)
        versionVisible = true
        delay(150L)
        bodyVisible = true
        delay(200L)
        linksVisible = true
        delay(200L)
        footerVisible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "About PawMate",
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Hero banner ---
                AnimatedVisibility(
                    visible = heroVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(
                        initialOffsetY = { -60 },
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(accentGradient)
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val logoPulse by rememberInfiniteTransition(label = "logo_pulse")
                                .animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.05f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2200, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "logo_scale"
                                )

                            Image(
                                painter = painterResource(id = R.drawable.blackpawmateicon3),
                                contentDescription = "PawMate Logo",
                                contentScale = ContentScale.Fit,
                                colorFilter = ColorFilter.tint(Color.White),
                                modifier = Modifier
                                    .size(90.dp)
                                    .graphicsLayer {
                                        scaleX = logoPulse
                                        scaleY = logoPulse
                                    }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "PawMate",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Find your perfect companion",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- App Version card ---
                AnimatedVisibility(
                    visible = versionVisible,
                    enter = fadeIn(tween(450)) + slideInVertically(
                        initialOffsetY = { 35 },
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 380f)
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "App Version",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            VersionInfoRow("Version", "1.0 (Stable)", textColor, secondaryTextColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            VersionInfoRow("Platform", "Android Native (Jetpack Compose)", textColor, secondaryTextColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            VersionInfoRow("API Level", "Optimized for Android 11 (API 30) and above", textColor, secondaryTextColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            VersionInfoRow("Developed by", "PawMate team", textColor, primaryColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- What is PawMate card ---
                AnimatedVisibility(
                    visible = bodyVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f)
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "ABOUT PAWMATE",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textColor,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "WHAT IS PAWMATE?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "PawMate is a mobile application developed as a specialized platform " +
                                        "to facilitate and streamline the pet adoption process. Originally conceived " +
                                        "as part of a Senior High School research study, the app focuses on the " +
                                        "critical relationship between User Interface (UI) design and User Engagement " +
                                        "to ensure that finding a forever home for animals is as intuitive and " +
                                        "effective as possible.\n\n" +
                                        "The application serves as a digital bridge, connecting animal shelters " +
                                        "directly with potential adopters through real-time messaging, verified pet " +
                                        "profiles, and an interactive discovery system. By prioritizing a " +
                                        "high-engagement user experience, PawMate aims to modernize animal welfare " +
                                        "efforts and increase successful adoption rates within the community.",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- Social / contact links ---
                AnimatedVisibility(
                    visible = linksVisible,
                    enter = fadeIn(tween(400))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "Connect with us",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SocialButton(
                                icon = Icons.Filled.Email,
                                label = "Email",
                                color = primaryColor,
                                delayMs = 0,
                                parentVisible = linksVisible,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:$supportEmail")
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Send email")
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            SocialButton(
                                icon = Icons.Filled.Facebook,
                                label = "Facebook",
                                color = Color(0xFF1877F2),
                                delayMs = 120,
                                parentVisible = linksVisible,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
                                    context.startActivity(intent)
                                }
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            SocialButton(
                                icon = Icons.Filled.CameraAlt,
                                label = "Instagram",
                                color = Color(0xFFE1306C),
                                delayMs = 240,
                                parentVisible = linksVisible,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl))
                                    context.startActivity(intent)
                                }
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            SocialButton(
                                icon = Icons.Filled.MusicNote,
                                label = "TikTok",
                                color = if (isDarkMode) Color(0xFFFF004F) else Color.Black,
                                delayMs = 360,
                                parentVisible = linksVisible,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tiktokUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // --- Footer ---
                AnimatedVisibility(
                    visible = footerVisible,
                    enter = fadeIn(tween(600))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .padding(bottom = 16.dp),
                            color = secondaryTextColor.copy(alpha = 0.2f)
                        )

                        Icon(
                            imageVector = Icons.Filled.Pets,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Made with love for pets everywhere",
                            fontSize = 12.sp,
                            color = secondaryTextColor.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialButton(
    icon: ImageVector,
    label: String,
    color: Color,
    delayMs: Int,
    parentVisible: Boolean,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(parentVisible) {
        if (parentVisible) {
            delay(delayMs.toLong())
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInHorizontally(
            initialOffsetX = { 30 },
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun VersionInfoRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor
        )
    }
}
