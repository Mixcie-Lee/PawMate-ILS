package com.example.pawmate_ils.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.ThemeManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(navController: NavController) {
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFFF0F5)
    val cardColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = if (isDarkMode) Color(0xFFB8B8B8) else Color(0xFF5A5A5A)
    val primaryColor = if (isDarkMode) Color(0xFFFF9999) else Color(0xFFFFB6C1)
    val accentGradient = if (isDarkMode)
        Brush.linearGradient(listOf(Color(0xFFFF6B8A), Color(0xFFFF9999)))
    else
        Brush.linearGradient(listOf(Color(0xFFFFB6C1), Color(0xFFFF8FA3)))

    val context = LocalContext.current
    val supportEmail = "pawmate.support@gmail.com"
    val facebookUrl = "https://www.facebook.com/share/1AQdGpKRtS/?mibextid=wwXIfr"
    val instagramUrl = "https://www.instagram.com/_pawbridge?igsh=MTR0djk1MTJoNzZldw%3D%3D&utm_source=qr"
    val tiktokUrl = "https://www.tiktok.com/@pawmate4"

    var headerVisible by remember { mutableStateOf(false) }
    var contactVisible by remember { mutableStateOf(false) }
    var socialsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        headerVisible = true
        delay(250L)
        contactVisible = true
        delay(200L)
        socialsVisible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Contact Us",
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // --- Header icon + title ---
                AnimatedVisibility(
                    visible = headerVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val pulse by rememberInfiniteTransition(label = "header_pulse")
                            .animateFloat(
                                initialValue = 1f,
                                targetValue = 1.06f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "paw_pulse"
                            )

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                                .clip(CircleShape)
                                .background(accentGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Pets,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Get in Touch",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Have questions or need help?\nReach out through any of the channels below.",
                            fontSize = 14.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                // --- Email contact card ---
                AnimatedVisibility(
                    visible = contactVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f)
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$supportEmail")
                                }
                                context.startActivity(
                                    Intent.createChooser(intent, "Send email")
                                )
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = "Email",
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "Email",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = supportEmail,
                                    fontSize = 13.sp,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- Social links ---
                AnimatedVisibility(
                    visible = socialsVisible,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = 350f)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Or find us on",
                            fontSize = 14.sp,
                            color = secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ContactIcon(
                                icon = Icons.Filled.Facebook,
                                label = "Facebook",
                                color = Color(0xFF1877F2),
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.width(28.dp))

                            ContactIcon(
                                icon = Icons.Filled.CameraAlt,
                                label = "Instagram",
                                color = Color(0xFFE1306C),
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl))
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.width(28.dp))

                            ContactIcon(
                                icon = Icons.Filled.MusicNote,
                                label = "TikTok",
                                color = if (isDarkMode) Color(0xFFFF004F) else Color.Black,
                                onClick = {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(tiktokUrl))
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
private fun ContactIcon(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
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
