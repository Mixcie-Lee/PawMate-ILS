package com.example.pawmate_ils.ui.screens

import TinderLogic_PetSwipe.PetData
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import kotlinx.coroutines.launch

// Data class preserved to fix unresolved references


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionCenterPets(
    navController: NavController,
    onBackClick: () -> Unit,
    viewModel: AdoptionCenterViewModel,
    authViewModel: com.example.pawmate_ils.Firebase_Utils.AuthViewModel, // 🎯 Add this
    onAddPet: () -> Unit
) {

    LaunchedEffect(Unit) {
        val currentUserId = authViewModel.currentUser?.uid
        if (currentUserId != null) {
            viewModel.startShelterPetsListener(currentUserId)
        }
    }

    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val pets by viewModel.shelterPets.collectAsState()
    val scope = rememberCoroutineScope()
    var petToDelete by remember { mutableStateOf<PetData?>(null) }

    /*val pets = remember {
        mutableStateListOf(
            Pet1("Max", "Dog", "Aspin", "2 years old", "Male"),
            Pet1("Salt", "Cat", "Siamese cat", "1 year old", "Female"),
            Pet1("Bella", "Dog", "Poodle", "3 years old", "Female"),
            Pet1("Charlie", "Dog", "Golden Retriever", "1 year old", "Male")
        )
    }*/









    val filteredPets = remember(searchQuery, pets) {
        val currentPets = pets ?: emptyList() // Fallback to avoid null pointer errors
        currentPets.filter { pet: PetData ->
            val query = searchQuery.lowercase().trim()
            val nameMatch = pet.name?.lowercase()?.contains(query) == true
            val breedMatch = pet.breed?.lowercase()?.contains(query) == true
            val typeMatch = pet.type?.lowercase()?.contains(query) == true

            nameMatch || breedMatch || typeMatch
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Manage Pets",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD67A7A),
                                fontSize = 32.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBackIosNew, "Back", modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
            }
            // REMOVED: floatingActionButton parameter is completely deleted here
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = { Text("Search", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray) },
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFEFEFEF),
                            unfocusedContainerColor = Color(0xFFEFEFEF),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }


                items(filteredPets) { pet ->
                    // 2. Pass the real PetData object
                    PetManagementRow(
                        navController = navController,
                        pet = pet,
                        searchQuery = searchQuery,
                        onDelete = { petToDelete = pet },

                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f))
                }

                // Keeps enough space so the bottom pet isn't blocked by the FloatingNavBar
                item { Spacer(modifier = Modifier.height(110.dp)) }
            }
        }

        // Shared Floating Navigation Bar remains correctly positioned
        FloatingNavBar(
            navController = navController,
            selectedTab = 2,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    if (petToDelete != null) {
        AlertDialog(
            onDismissRequest = { petToDelete = null },
            title = { Text("Remove ${petToDelete?.name}?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently remove this pet from the PawMate database.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            petToDelete?.petId?.let { id ->
                                viewModel.deletePet(id)
                                Toast.makeText(context, "Pet removed", Toast.LENGTH_SHORT).show()
                            }
                            petToDelete = null // Close dialog
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { petToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }




}

@Composable
fun PetManagementRow(
    pet: PetData,
    onDelete: () -> Unit,
    searchQuery: String,
    navController: NavController
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = getHighlightedText(pet.name ?: "Unnamed Pet", searchQuery),                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            )
            Text(
                text = "${pet.breed ?: "Unknown Breed"} • ${pet.age ?: "Unknown Age"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "${pet.type ?: "Pet"} • ${pet.gender ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigate("edit_pet_screen/${pet.petId}")  }) {
                Icon(Icons.Default.Edit, "Edit", tint = Color(0xFFF1C89B), modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = onDelete ) {
                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFD67A7A), modifier = Modifier.size(24.dp))
            }
        }
    }
}

//LOGIC FOR WHEN USER SEARCH A CERTAIN PET THE COLUMN CARD WILL HIGHLIGHT
@Composable
fun getHighlightedText(text: String, query: String): AnnotatedString {
    val pinkHighlight = Color(0xFFFFB6C1) // Your Pet Pink color
    return buildAnnotatedString {
        val startIndex = text.indexOf(query, ignoreCase = true)
        if (query.isNotEmpty() && startIndex != -1) {
            this.append(text.substring(0, startIndex))
            withStyle(style = SpanStyle(background = pinkHighlight, fontWeight = FontWeight.Bold)) {
                append(text.substring(startIndex, startIndex + query.length))
            }
            append(text.substring(startIndex + query.length))
        } else {
            append(text)
        }
    }
}



/*@Preview(showBackground = true, name = "Manage Pets Design Preview")
@Composable
fun AdoptionCenterPetsPreview() {
    MaterialTheme {
        AdoptionCenterPets(
            navController = rememberNavController(),
            onBackClick = {},
            onAddPet = {}
        )
    }
}
*/
