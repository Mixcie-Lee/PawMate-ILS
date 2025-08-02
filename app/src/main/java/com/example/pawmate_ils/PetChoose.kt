package com.example.pawmate_ils

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.ui.theme.PawMateILSTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Data class for our swipeable category cards
data class SwipeableCategory(
    val id: String,
    val title: String,
    val imageResId: Int,
    val backgroundColor: Color,
    val navigationRoute: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetSelectionScreen(navController: NavController) {
    val categories = remember {
        listOf(
            SwipeableCategory("dog", "Dogs", R.drawable.dog_selection, Color(0xFFC8E6C9), "pet_swipe"),
            SwipeableCategory("cat", "Cats", R.drawable.cat_selection, Color(0xFFFFCDD2), "cat_swipe")
        )
    }

    // For this screen, we'll just show the two options clearly.
    // A full swipe stack for just two categories might be overkill if tapping is the main goal.
    // We can make them look like cards.

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.DarkGray // Or your desired background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .offset(y = -80.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center the category choices
        ) {
            Text(
                text = "Choose Your Preference",
                fontSize = 35.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 50.dp)
                ,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    CategoryCard(
                        category = category,
                        onClick = { navController.navigate(category.navigationRoute) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.75f) // Make cards taller than wide
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            // You can add your "not done" text or other UI elements here if needed
            // Footer "not done" text - kept from original if still needed
            // Spacer(modifier = Modifier.weight(1f))
            // ... (your existing animation for "not done" text can go here)
        }
    }
}

@Composable
fun CategoryCard(
    category: SwipeableCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleAnimation"
    )

    Card(
        modifier = modifier
            .scale(animatedScale)
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                onClick = onClick,
                indication = ripple(),
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            androidx.compose.ui.input.pointer.PointerEventType.Press -> scale = 0.95f
                            androidx.compose.ui.input.pointer.PointerEventType.Release -> scale = 1f
                            androidx.compose.ui.input.pointer.PointerEventType.Exit -> scale = 1f
                        }
                    }
                }
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = category.imageResId),
                contentDescription = category.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .weight(10f) // Image takes most space
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Text(
                text = category.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f), // Adjust text color for contrast
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview(showBackground = true, apiLevel = 34)
@Composable
fun PetSelectionScreenPreview() {
    val mockNavController = rememberNavController()
    PawMateILSTheme {
        PetSelectionScreen(navController = mockNavController)
    }
}
