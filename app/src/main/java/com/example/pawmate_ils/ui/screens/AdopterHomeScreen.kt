package com.example.pawmate_ils.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.R

data class AdoptedPet(
    val name: String,
    val breed: String,
    val description: String,
    val imageResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdopterHomeScreen(navController: NavController) {
    var currentPetIndex by remember { mutableStateOf(0) }
    
    val adoptedPets = listOf(
        AdoptedPet("Max", "Golden Retriever", "bla bla bla bla bla bla", R.drawable.dog1),
        AdoptedPet("Luna", "Persian Cat", "Gentle and loving companion", R.drawable.cat_selection),
        AdoptedPet("Charlie", "Labrador", "Playful and energetic friend", R.drawable.dog1)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = { navController.navigate("profile_settings") },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (adoptedPets.isNotEmpty() && currentPetIndex < adoptedPets.size) {
                        val currentPet = adoptedPets[currentPetIndex]
                        
                        Card(
                            modifier = Modifier
                                .width(320.dp)
                                .height(480.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = currentPet.imageResId),
                                    contentDescription = currentPet.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(
                                            Color.Black.copy(alpha = 0.6f),
                                            RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = currentPet.name,
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 28.sp
                                            )
                                        )
                                        Text(
                                            text = currentPet.breed,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = currentPet.description,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No pets adopted yet",
                                fontSize = 18.sp,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { navController.navigate("pet_selection") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6B4423),
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Find Pets")
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp, vertical = 30.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { 
                            if (adoptedPets.isNotEmpty() && currentPetIndex < adoptedPets.size - 1) {
                                currentPetIndex++
                            } else if (adoptedPets.isNotEmpty()) {
                                currentPetIndex = 0
                            }
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.White,
                        contentColor = Color.Gray,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Next",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    FloatingActionButton(
                        onClick = { },
                        modifier = Modifier.size(64.dp),
                        containerColor = Color(0xFF007AFF),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    FloatingActionButton(
                        onClick = { },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color.White,
                        contentColor = Color.Gray,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}