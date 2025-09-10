package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun PetChooseScreen(
    onContinueClick: (List<String>) -> Unit,
    onBackClick: () -> Unit = {}
) {
    var currentPetIndex by remember { mutableStateOf(0) }
    var likedPets by remember { mutableStateOf(mutableListOf<String>()) }
    
    val pets = listOf(
        PetData("Max", "Golden Retriever", "https://via.placeholder.com/400x600/90EE90/000000?text=Max"),
        PetData("Luna", "Persian Cat", "https://via.placeholder.com/400x600/FFB6C1/000000?text=Luna"),
        PetData("Charlie", "Labrador", "https://via.placeholder.com/400x600/87CEEB/000000?text=Charlie"),
        PetData("Bella", "Siamese Cat", "https://via.placeholder.com/400x600/DDA0DD/000000?text=Bella")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            if (currentPetIndex < pets.size) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF90EE90)) // Light green background
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                                    .background(Color(0xFF90EE90)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🐕", // Placeholder pet emoji
                                    fontSize = 80.sp
                                )
                            }
                        }
                        
                        // Pet Info at bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(
                                    Color.Black.copy(alpha = 0.3f),
                                    RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = pets[currentPetIndex].name,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 28.sp
                                )
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (currentPetIndex < pets.size - 1) {
                                currentPetIndex++
                            } else {
                                onContinueClick(likedPets)
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        containerColor = Color.White,
                        contentColor = Color.Red,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Pass",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    FloatingActionButton(
                        onClick = {
                            likedPets.add(pets[currentPetIndex].name)
                            if (currentPetIndex < pets.size - 1) {
                                currentPetIndex++
                            } else {
                                onContinueClick(likedPets)
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        containerColor = Color(0xFFFF69B4), // Pink color
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        likedPets.add(pets[currentPetIndex].name)
                        onContinueClick(likedPets)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF69B4),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        text = "ADOPT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "You've seen all pets!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    
                    Button(
                        onClick = { onContinueClick(likedPets) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF69B4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Continue with ${likedPets.size} pets",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class PetData(
    val name: String,
    val breed: String,
    val imageUrl: String
)
