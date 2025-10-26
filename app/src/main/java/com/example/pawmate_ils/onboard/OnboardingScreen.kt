package com.example.pawmate_ils.onboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.pawmate_ils.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.ThemeManager
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = OnboardingData.onboardingItems
    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    var termsAccepted by remember { mutableStateOf(false) }

    val buttonState = remember {
        derivedStateOf {
            when (pagerState.currentPage) {
                0 -> listOf("", "Next")
                1 -> listOf("Back", "Next")
                2 -> listOf("Back", "Next")
                3 -> listOf("Back", "Get Started")
                else -> listOf("", "")
            }
        }
    }

    val scope = rememberCoroutineScope()
    val infiniteTransition = rememberInfiniteTransition(label = "main_animation")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF0F5),
                        Color(0xFFFFE4E9),
                        Color(0xFFFFD1DC),
                        Color(0xFFFFB6C1)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (buttonState.value[0].isNotEmpty()) {
                            Card(
                                onClick = {
                                    scope.launch {
                                        if (pagerState.currentPage > 0) {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    }
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = buttonState.value[0],
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(pages.size) { dotIndex ->
                                val dotScale by animateFloatAsState(
                                    targetValue = if (pagerState.currentPage == dotIndex) 1.3f else 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "dot_scale_$dotIndex"
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .scale(dotScale)
                                        .background(
                                            color = if (pagerState.currentPage == dotIndex)
                                                Color.White
                                            else
                                                Color.White.copy(alpha = 0.4f),
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        val buttonPulse by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "button_pulse"
                        )
                        
                        Card(
                            onClick = {
                                scope.launch {
                                    if (pagerState.currentPage < pages.size - 1) {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    } else if (termsAccepted) {
                                        onComplete()
                                    }
                                }
                            },
                            enabled = if (pagerState.currentPage == 3) termsAccepted else true,
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                                disabledContainerColor = Color.White.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier.scale(if (pagerState.currentPage == 3 && termsAccepted) buttonPulse else 1f)
                        ) {
                            Text(
                                text = buttonState.value[1],
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pagerState.currentPage == 3 && !termsAccepted) 
                                    Color.Gray 
                                else 
                                    Color(0xFFFF9999),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                val logoScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.08f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "logo_scale"
                )
                
                val logoRotation by infiniteTransition.animateFloat(
                    initialValue = -3f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "logo_rotation"
                )
                
                Image(
                    painter = painterResource(id = R.drawable.pawmate_logo),
                    contentDescription = "PawMate Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 40.dp, bottom = 16.dp)
                        .scale(logoScale)
                        .rotate(logoRotation)
                )
                
                val textShimmer by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "text_shimmer"
                )
                
                Text(
                    text = "PAWMATE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = textShimmer),
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.2f),
                            offset = androidx.compose.ui.geometry.Offset(0f, 3f),
                            blurRadius = 8f
                        )
                    )
                )
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { index ->
                    if (index == 3) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                                animationSpec = tween(600),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 28.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val shieldScale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.12f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "shield_pulse"
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Text(
                                        text = "🛡️",
                                        fontSize = 64.sp,
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .scale(shieldScale)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(28.dp))
                                
                                Text(
                                    text = pages[index].title,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 20.dp),
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.2f),
                                            offset = androidx.compose.ui.geometry.Offset(0f, 2f),
                                            blurRadius = 6f
                                        )
                                    )
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.9f)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Text(
                                        text = pages[index].description,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color.Black.copy(alpha = 0.8f),
                                        lineHeight = 24.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(28.dp))
                                
                                val checkboxGlow by infiniteTransition.animateFloat(
                                    initialValue = 0.85f,
                                    targetValue = 0.95f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "checkbox_glow"
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = if (!termsAccepted) checkboxGlow else 0.9f)
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(20.dp)
                                    ) {
                                        Checkbox(
                                            checked = termsAccepted,
                                            onCheckedChange = { termsAccepted = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFFFB6C1),
                                                uncheckedColor = Color.Gray.copy(alpha = 0.5f),
                                                checkmarkColor = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "I agree to the Terms of Service and Privacy Policy",
                                            fontSize = 14.sp,
                                            color = Color.Black.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        val pageIcon = when (index) {
                            0 -> "🐾"
                            1 -> "🏠"
                            2 -> "❤️"
                            else -> "📋"
                        }
                        
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                                animationSpec = tween(600),
                                initialOffsetY = { it / 3 }
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 28.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val iconPulse by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "icon_pulse"
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.25f)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Text(
                                        text = pageIcon,
                                        fontSize = 60.sp,
                                        modifier = Modifier
                                            .padding(20.dp)
                                            .scale(iconPulse)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Text(
                                    text = pages[index].title,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 20.dp),
                                    style = androidx.compose.ui.text.TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.25f),
                                            offset = androidx.compose.ui.geometry.Offset(0f, 3f),
                                            blurRadius = 8f
                                        )
                                    )
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White.copy(alpha = 0.9f)
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Text(
                                        text = pages[index].description,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color.Black.copy(alpha = 0.8f),
                                        lineHeight = 24.sp,
                                        fontWeight = FontWeight.Normal,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewOnboardingScreen() {
    OnboardingScreen {}
}