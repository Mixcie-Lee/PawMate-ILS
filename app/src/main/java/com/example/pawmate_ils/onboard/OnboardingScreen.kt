package com.example.pawmate_ils.onboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.pawmate_ils.ui.theme.DarkBrown
import com.example.pawmate_ils.ThemeManager

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
    
    // Dark mode support
    val isDarkMode = ThemeManager.isDarkMode
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp, 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (buttonState.value[0].isNotEmpty()) {
                        ButtonUI(
                            text = buttonState.value[0],
                            backgroundColor = Color.Transparent,
                            textColor = DarkBrown
                        ) {
                            scope.launch {
                                if (pagerState.currentPage > 0) {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    IndicatorUI(pageSize = pages.size, currentPage = pagerState.currentPage)
                }

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    ButtonUI(
                        text = buttonState.value[1],
                        backgroundColor = DarkBrown,
                        textColor = Color.White,
                        enabled = if (pagerState.currentPage == 3) termsAccepted else true
                    ) {
                        scope.launch {
                            if (pagerState.currentPage < pages.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else if (termsAccepted) {
                                onComplete()
                            }
                        }
                    }
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { index ->
                    if (index == 3) {
                        // Terms & Privacy page with checkbox
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "📋 ${pages[index].title}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBrown,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            
                            Text(
                                text = pages[index].description,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = { termsAccepted = it },
                                    colors = CheckboxDefaults.colors(checkedColor = DarkBrown)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "I agree to the Terms of Service and Privacy Policy",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        OnboardingGraphUI(onboardingModel = pages[index])
                    }
                }
            }
        }
    )
}

@Composable
private fun PreviewOnboardingScreen() {
    OnboardingScreen {}
}