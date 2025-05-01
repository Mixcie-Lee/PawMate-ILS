package TinderLogic_PetSwipe

import TinderLogic_CatSwipe.AnimatedWelcomeText
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay



@Composable
fun AnimatedWelcomeText(userName: String) {
    var isPink by remember { mutableStateOf(true) }

    val animatedColor by animateColorAsState(
        targetValue = if (isPink) Color(0xFFFFB6C1) else Color.White, // LightPink ↔ White
        animationSpec = tween(durationMillis = 1000),
        label = "WelcomeColor"
    )

    // Toggle the color every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            isPink = !isPink
        }
    }

    Text(
        text = "Welcome Back\n\n$userName!",
        fontSize = 30.sp,
        letterSpacing = 4.sp,
        fontWeight = FontWeight.Bold,
        color = animatedColor

    )
}
@Composable
fun PetSwipeScreen(userName: String) {
    val context = LocalContext.current
    val petList = remember { PetRepository.getPets() }
    val cardStack = remember { mutableStateListOf(*petList.toTypedArray()) }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }

    fun resetCards() {
        cardStack.clear()
        cardStack.addAll(petList)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))


        AnimatedWelcomeText(userName = userName)


        Spacer(modifier = Modifier.height(90.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cardStack.isEmpty()) {
                // Reset automatically when all cards are swiped
                LaunchedEffect(Unit) {
                    Toast.makeText(context, "Process will reset, until you find your fur baby \uD83D\uDC36", Toast.LENGTH_SHORT).show()
                    delay(800)
                    resetCards()

                }
            } else {
                cardStack.asReversed().forEachIndexed { index, pet ->
                    PetCard(
                        pet = pet,
                        isTopCard = index == cardStack.lastIndex,
                        onSwiped = {
                            cardStack.remove(pet)
                        },
                        onImageTap = { tappedPet ->
                            selectedPet = tappedPet // Set the selected pet
                        }
                    )
                }
            }
        }
    }
}
