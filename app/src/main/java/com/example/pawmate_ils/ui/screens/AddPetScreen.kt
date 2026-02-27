package com.example.pawmate_ils.ui.screens

import TinderLogic_PetSwipe.PetData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewMdelFactory
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.R

@Composable
fun AddPetScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val adoptionViewModel: AdoptionCenterViewModel = viewModel(
        factory = AdoptionCenterViewMdelFactory(authViewModel)
    )

    val addPetStatus by adoptionViewModel.addPetStatus.collectAsState()

    LaunchedEffect(addPetStatus) {
        addPetStatus?.let { result ->
            result.onSuccess {
                Toast.makeText(context, "Pet added successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }.onFailure { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        StatelessAddPet(
            navController = navController,
            onBackClick = { navController.popBackStack() },
            onSavePet = { newPet -> adoptionViewModel.addPet(newPet) },
            currentUserUid = authViewModel.currentUser?.uid,
            currentDisplayName = authViewModel.currentUser?.displayName
        )

        FloatingNavBar(
            navController = navController,
            selectedTab = 1,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatelessAddPet(
    navController: NavController,
    onBackClick: () -> Unit,
    onSavePet: (PetData) -> Unit,
    currentUserUid: String?,
    currentDisplayName: String?
) {
    val context = LocalContext.current
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("dog") }
    var petBreed by remember { mutableStateOf("Aspin") }
    var petAge by remember { mutableStateOf("1") }
    var petDescription by remember { mutableStateOf("") }
    var petSex by remember { mutableStateOf("Male") }

    val dogBreeds = listOf("Aspin", "Golden Retriever", "Shih Tzu", "Pomeranian", "Chow Chow", "Beagle", "Labrador")
    val catBreeds = listOf("Puspin", "Siamese", "Persian", "Maine Coon", "Bengal", "British Shorthair")
    val currentBreeds = if (petType == "dog") dogBreeds else catBreeds

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add Pet",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LabelText("Type")
            RoundedDropdown(selected = petType, options = listOf("dog", "cat")) { selectedType ->
                petType = selectedType
                petBreed = if (selectedType == "dog") "Aspin" else "Puspin"
            }

            LabelText("Name")
            RoundedTextField(value = petName, onValueChange = { petName = it }, placeholder = "Max")

            LabelText("Age")
            RoundedTextField(
                value = petAge,
                onValueChange = { petAge = it },
                placeholder = "1 year old" // Updated placeholder
            )

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

            Text(text = "Upload Image via Google Drive", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

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

            Button(
                onClick = {
                    if (petName.isBlank() || petDescription.isBlank() || petBreed.isBlank()) {
                        Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        onSavePet(PetData(
                            name = petName, breed = petBreed, age = petAge,
                            description = petDescription, type = petType,
                            imageRes = R.drawable.placeholder,
                            shelterId = currentUserUid ?: "shelter_mock",
                            shelterName = currentDisplayName ?: "Shelter Name",
                            validationStatus = false
                        ))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A))
            ) {
                Text("Save", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedEditableDropdown(value: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val filteredOptions = options.filter { it.contains(value, ignoreCase = true) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFEFEFEF), unfocusedContainerColor = Color(0xFFEFEFEF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
        )
        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                filteredOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onValueChange(option); expanded = false },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedDropdown(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected.replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true,
            trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFEFEFEF), unfocusedContainerColor = Color(0xFFEFEFEF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option.replaceFirstChar { it.uppercase() }) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

@Composable
fun LabelText(text: String) {
    Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
}

@Composable
fun RoundedTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, singleLine: Boolean = true, keyboardType: KeyboardType = KeyboardType.Text) {
    TextField(
        value = value, onValueChange = onValueChange, modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray) },
        singleLine = singleLine, keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(15.dp),
        colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFEFEFEF), unfocusedContainerColor = Color(0xFFEFEFEF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
    )
}

@Preview(showBackground = true, name = "Add Pet Full Design Preview", heightDp = 1200)
@Composable
fun AddPetScreenPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
            StatelessAddPet(
                navController = rememberNavController(),
                onBackClick = {},
                onSavePet = {},
                currentUserUid = "preview_user",
                currentDisplayName = "Preview Shelter"
            )
            FloatingNavBar(
                navController = rememberNavController(),
                selectedTab = 1,
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = 20.dp)
            )
        }
    }
}