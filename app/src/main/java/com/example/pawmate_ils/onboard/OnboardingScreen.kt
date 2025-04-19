package com.example.pawmate_ils.onboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.pawmate_ils.ui.theme.PetPink

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = listOf(OnboardingModel.FirstPage, OnboardingModel.SecondPage, OnboardingModel.ThirdPage)
    val pagerState = rememberPagerState(initialPage = 0) { pages.size }

    val buttonState = remember {
        derivedStateOf {
            when (pagerState.currentPage) {
                0 -> listOf("", "Next")
                1 -> listOf("Back", "Next")
                2 -> listOf("Back", "Get Started")
                else -> listOf("", "")
            }
        }
    }

    val scope = rememberCoroutineScope()

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
                            textColor = PetPink
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
                        backgroundColor = PetPink,
                        textColor = Color.White
                    ) {
                        scope.launch {
                            if (pagerState.currentPage < pages.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                onComplete()
                            }
                        }
                    }
                }
            }
        },
        content = { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                HorizontalPager(state = pagerState) { index ->
                    OnboardingGraphUI(onboardingModel = pages[index])
                }
            }
        }
    )
}

@Composable
private fun PreviewOnboardingScreen() {
    OnboardingScreen {}
}