package TinderLogic_CatSwipe


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import com.example.pawmate_ils.R
import kotlinx.coroutines.Job
import kotlin.math.roundToInt


@Composable
fun CatCard(
    cat: Cat,
    isTopCard: Boolean,
    onSwiped: (String) -> Unit,
    onImageTap: (Cat) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatableX = remember { Animatable(0f) }

    val cardModifier = Modifier
        .offset { IntOffset(animatableX.value.roundToInt(), 0) }
        .fillMaxWidth(0.9f)
        .aspectRatio(0.60f)
        .rotate(animatableX.value / 30)
        .clip(RoundedCornerShape(40.dp))
        .background(Color.Black)

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
            CatCardContent(cat)
        }
    } else {
        Box(
            modifier = cardModifier,
            contentAlignment = Alignment.BottomCenter
        ) {
            CatCardContent(cat)
        }
    }
}

@Composable
fun CatCardContent(cat: Cat) {
    var currentImageIndex by remember { mutableStateOf(0) }
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = if (currentImageIndex == 0) cat.imageRes else cat.subImages[currentImageIndex - 1]),
            contentDescription = cat.name,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            // Cycle through subImages
                            if (currentImageIndex < cat.subImages.size) {
                                currentImageIndex++
                            } else {
                                currentImageIndex = 0 // Reset to main image
                            }
                        }
                    )
                },
            contentScale = ContentScale.FillBounds
        )

        // Dim overlay behind content, positioned at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 0.dp, y = 10.dp)
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(5.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = cat.name,
                    letterSpacing = 6.sp,
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.custom_font))
                )

                Text(text = cat.age, fontSize = 19.sp, color = Color.White, fontFamily = FontFamily(Font(R.font.custom_font)))

                Text(text = cat.description, fontSize = 17.sp, color = Color.White, fontFamily = FontFamily(Font(R.font.custom_font)))

                Text(text = cat.breed, fontSize = 15.sp, color = Color.White, fontFamily = FontFamily(Font(R.font.custom_font)))

                    Text(
                        "ADOPT",
                        fontSize = 20.sp,
                        letterSpacing = 5.sp
                    )
                }
            }
        }
    }
