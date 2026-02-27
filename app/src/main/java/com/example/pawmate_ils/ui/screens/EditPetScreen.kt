package com.example.pawmate_ils.ui.screens

import TinderLogic_PetSwipe.PetData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(
    navController: NavController,
    viewModel: AdoptionCenterViewModel,
    petId: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Inherited State Variables
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("dog") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("1") }
    var petSex by remember { mutableStateOf("Male") }
    var petDescription by remember { mutableStateOf("") }

    val dogBreeds = listOf("Aspin", "Golden Retriever", "Shih Tzu", "Pomeranian", "Chow Chow", "Beagle", "Labrador")
    val catBreeds = listOf("Puspin", "Siamese", "Persian", "Maine Coon", "Bengal", "British Shorthair")
    val currentBreeds = if (petType == "dog") dogBreeds else catBreeds

    // 2. AUTO-FILL LOGIC: Fetches cloud data to fill the inputs
    LaunchedEffect(petId) {
        val existingPet = viewModel.shelterPets.value.find { it.petId == petId }
        existingPet?.let {
            petName = it.name ?: ""
            petType = it.type ?: "dog"
            petBreed = it.breed ?: ""
            petAge = it.age ?: "1"
            petSex = it.gender ?: "Male"
            petDescription = it.description ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Pet",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD67A7A),
                            fontSize = 32.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, "Back", modifier = Modifier.size(20.dp), tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdowns now include indicators
            LabelText("Type")
            RoundedDropdown(selected = petType, options = listOf("dog", "cat")) { selectedType ->
                petType = selectedType
                petBreed = if (selectedType == "dog") "Aspin" else "Puspin"
            }

            LabelText("Name")
            RoundedTextField(value = petName, onValueChange = { petName = it }, placeholder = "Max")

            LabelText("Age")
            RoundedDropdown(selected = petAge, options = (1..20).map { it.toString() }) { petAge = it }

            LabelText("Sex")
            RoundedDropdown(selected = petSex, options = listOf("Male", "Female")) { petSex = it }

            LabelText("Breed")
            RoundedEditableDropdown(value = petBreed, options = currentBreeds, onValueChange = { petBreed = it })

            LabelText("Description")
            RoundedTextField(
                value = petDescription,
                onValueChange = { petDescription = it },
                placeholder = "Friendly and energetic",
                singleLine = false,
                modifier = Modifier.height(120.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Text(text = "Update Image via Google Drive", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1SY1pczmOL7b-lAL6CoOzIW4p1r9UWqWH"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A))
            ) {
                Text("Open Google Drive Folder", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Please upload clear images of your pets in the shared google drive folder. Our team will review and add them to the app.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (petName.isBlank() || petDescription.isBlank() || petBreed.isBlank()) {
                        Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedData = mapOf(
                            "name" to petName,
                            "type" to petType,
                            "breed" to petBreed,
                            "age" to petAge,
                            "gender" to petSex,
                            "description" to petDescription
                        )
                        viewModel.updatePet(petId, updatedData)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A))
            ) {
                Text("Save Changes", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}
@Preview(showBackground = true, name = "Edit Pet Full Design Preview", heightDp = 1200)
@Composable
fun EditPetScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Back Arrow
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ArrowBackIosNew, null, Modifier.size(20.dp), tint = Color.Gray)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "Edit Pet",
                        color = Color(0xFFD67A7A),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.size(20.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Form Fields
                    LabelText("Type")
                    DropdownSimulation("dog")

                    LabelText("Name")
                    RoundedTextField(value = "Max", onValueChange = {}, placeholder = "Max")

                    LabelText("Age")
                    DropdownSimulation("2")

                    LabelText("Sex")
                    DropdownSimulation("Male")

                    LabelText("Breed")
                    DropdownSimulation("Golden Retriever")

                    LabelText("Description")
                    RoundedTextField(
                        value = "Friendly and energetic",
                        onValueChange = {},
                        placeholder = "",
                        modifier = Modifier.height(120.dp),
                        singleLine = false
                    )

                    // Google Drive Section
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

                    Text(text = "Update Image via Google Drive", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A))
                    ) {
                        Text("Open Google Drive Folder", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "Please upload clear images of your pets in the shared google drive folder.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

                    // Final Save Changes Button
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A))
                    ) {
                        Text("Save Changes", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}
// Helper for Preview Simulation
@Composable
private fun DropdownSimulation(text: String) {
    TextField(
        value = text, onValueChange = {}, readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
        shape = RoundedCornerShape(15.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEFEFEF),
            unfocusedContainerColor = Color(0xFFEFEFEF),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}



