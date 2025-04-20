package TinderLogic_PetSwipe

import  androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pawmate_ils.PetSelectionScreen
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale

@Composable
fun PetCard(
    pet: Pet,
    isTopCard: Boolean,
    onSwiped: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var offsetX by remember { mutableStateOf(0f) }
    val animatableX = remember { Animatable(0f) }

    val cardModifier = Modifier
        .offset { IntOffset(animatableX.value.roundToInt(), 0) }
        .fillMaxWidth(0.9f)
        .aspectRatio(0.60f)
        .rotate(animatableX.value / 30)
        .clip(RoundedCornerShape(40.dp))
        .background(Color.LightGray)

    if (isTopCard) {
        Box(
            modifier = cardModifier.pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        offsetX += dragAmount.x
                        coroutineScope.launch {
                            animatableX.snapTo(offsetX)
                        }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            when {
                                offsetX > 300f -> { // Swiped right (like)
                                    onSwiped("right")
                                    offsetX = 0f
                                    animatableX.animateTo(0f, tween(300)) // Reset position
                                }
                                offsetX < -300f -> { // Swiped left (dislike)
                                    onSwiped("left")
                                    offsetX = 0f
                                    animatableX.animateTo(0f, tween(300)) // Reset position
                                }
                                else -> { // If the swipe wasn't strong enough, reset
                                    offsetX = 0f
                                    animatableX.animateTo(0f, tween(300))
                                }
                            }
                        }
                    }
                )
            },
            contentAlignment = Alignment.BottomCenter
        ) {
            PetCardContent(pet)
        }
    } else {
        Box(
            modifier = cardModifier,
            contentAlignment = Alignment.BottomCenter
        ) {
            PetCardContent(pet)
        }
    }
}

@Composable
fun PetCardContent(pet: Pet) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = pet.imageRes),
            contentDescription = pet.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // Dim overlay behind content, positioned at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 0.dp, y = (10).dp )
                .align(Alignment.BottomCenter) // Place it at the very bottom
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(5.dp) // Inner padding for the content
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = pet.name,
                    letterSpacing = 6.sp,
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = pet.age, fontSize = 16.sp, color = Color.White)
                Text(text = pet.description, fontSize = 14.sp, color = Color.White)

                Button(
                    onClick = { },
                    modifier = Modifier
                        .width(185.dp)
                        .height(60.dp)
                        .align(Alignment.CenterHorizontally),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDB7F8E),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "ADOPT",
                        fontSize = 20.sp,
                        letterSpacing = 5.sp
                    )
                }
            }
        }
    }
}
