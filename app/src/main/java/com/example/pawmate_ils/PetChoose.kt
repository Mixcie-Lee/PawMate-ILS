package com.example.pawmate_ils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.navigation.compose.rememberNavController

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition.Center
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.ui.theme.PawMateILSTheme

@Composable
    fun PetSelectionScreen(navController: NavController) {
    var selectedPet by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.DarkGray
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "What pets would you like to adopt?",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 32.dp)

            )

            // Row with Dog and Cat options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PetCategoryButton(
                    title = "Dog",
                    baseColor = Color.Transparent, // Light green
                    imageResId = R.drawable.dog_selection, // Your specific dog image
                    isSelected = selectedPet == "Dog",
                    onClick = {
                        selectedPet = "Dog"
                        navController.navigate(" PetSwipeScreen")
                    },
                    modifier = Modifier
                        .size(400.dp)
                        .weight(1f)
                        .padding(8.dp)
                )

                PetCategoryButton(
                    title = "Cat",
                    baseColor = Color.Transparent, // Light pink/purple
                    imageResId = R.drawable.cat_selection, // Your specific cat image
                    isSelected = selectedPet == "Cat",
                    onClick = {
                        selectedPet = "Cat"
                        navController.navigate(" CatSwipeScreen")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                )
            }

            // Footer "not done" text
            Spacer(modifier = Modifier.weight(1f))
            var condition by remember { mutableStateOf(true) }

            val verticalOffset = animateDpAsState(
                targetValue = if (condition) 20.dp else -20.dp,
                animationSpec = tween(durationMillis = 2500)
            ).value

            LaunchedEffect(Unit) {
                condition = !condition

            }
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .width(250.dp)// Set the height of the box

                    .padding(16.dp), // Optional padding inside the box
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = " ✨ Give a stray their main character moment 🐶🐱 ✨",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center

                    ),
                    modifier = Modifier
                        .padding(16.dp)
                        .offset(y = verticalOffset)
                )
            }
        }



        // Bottom dots indicator
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            color = if (index == 2) Color.DarkGray else Color.LightGray,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}




@Composable
fun PetCategoryButton(
    title: String,
    baseColor: Color ,
    imageResId: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate color when selected
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) baseColor.copy(alpha = 0.7f) else baseColor,
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColorAnimation"
    )

    // Animate scale when selected
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleAnimation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(250.dp)
                .width(100.dp)
                .scale(scale)
                .clip(RoundedCornerShape(24.dp))
                .background(backgroundColor)
                .clickable(onClick = onClick)

        ) {
            // Add the pet image
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "$title image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(400.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(30.dp))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            letterSpacing = 7.sp,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFFFF007F) else Color.White
        )


    }
}

@Preview(showBackground = true)
@Composable
fun PetSelectionScreenPreview() {
    val mockNavController = rememberNavController() // This creates a dummy navController

    PawMateILSTheme {
        PetSelectionScreen(navController = mockNavController)
    }
}