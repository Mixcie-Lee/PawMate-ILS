package com.example.pawmate_ils.ui.screens

import TinderLogic_PetSwipe.PetData
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
    val userData by authViewModel.userData.collectAsState()
    val adoptionViewModel: AdoptionCenterViewModel = viewModel(
        factory = AdoptionCenterViewMdelFactory(authViewModel)
    )

    val addPetStatus by adoptionViewModel.addPetStatus.collectAsState()

    // Get stable data once here
    val stableShelterName = userData?.shelterName ?: userData?.name ?: "PawMate Shelter"
    val stableOwnerName = userData?.ownerName ?: "Authorized Staff"
    val rawAddress = userData?.Address
    val stableAddress = if (rawAddress.isNullOrBlank()) "Binangonan, Rizal" else rawAddress

    LaunchedEffect(addPetStatus) {
        addPetStatus?.let { result ->
            result.onSuccess { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                navController.navigate("adoption_center_dashboard") {
                    popUpTo("add_pet") { inclusive = true }
                }
            }.onFailure { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        StatelessAddPet(
            navController = navController,
            onBackClick = { navController.popBackStack() },
            onSavePet = { name, type, breed, age, sex, desc, health, mainUri, subUris ->
                adoptionViewModel.addPetWithImages(
                    context = context,
                    name = name,
                    type = type,
                    breed = breed,
                    age = age,
                    gender = sex,
                    description = desc,
                    healthStatus = health,
                    mainImageUri = mainUri,
                    subImageUris = subUris,
                    shelterId = authViewModel.currentUser?.uid ?: "",
                    shelterName = stableShelterName,
                    shelterAddress = stableAddress
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatelessAddPet(
    navController: NavController,
    onBackClick: () -> Unit,
    onSavePet: (String, String, String, String, String, String, String, Uri?, List<Uri>) -> Unit
) {
    val context = LocalContext.current
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("dog") }
    var petBreed by remember { mutableStateOf("Aspin") }
    var petAge by remember { mutableStateOf("1") }
    var petDescription by remember { mutableStateOf("") }
    var petSex by remember { mutableStateOf("Male") }
    var healthStatus by remember { mutableStateOf("") }

    // Image Picker States
    var mainImageUri by remember { mutableStateOf<Uri?>(null) }
    var subImage1Uri by remember { mutableStateOf<Uri?>(null) }
    var subImage2Uri by remember { mutableStateOf<Uri?>(null) }

    val mainPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { mainImageUri = it }
    val sub1Picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { subImage1Uri = it }
    val sub2Picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { subImage2Uri = it }

    val dogBreeds = listOf("Aspin", "Golden Retriever", "Shih Tzu", "Pomeranian", "Chow Chow", "Beagle", "Labrador")
    val catBreeds = listOf("Puspin", "Siamese", "Persian", "Maine Coon", "Bengal", "British Shorthair")
    val currentBreeds = if (petType == "dog") dogBreeds else catBreeds

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Pet", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFD67A7A), fontSize = 32.sp)) },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBackIosNew, "Back", modifier = Modifier.size(20.dp)) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White).verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Pet Photos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFD67A7A))

            // Main Photo Card
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp).clickable { mainPicker.launch("image/*") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (mainImageUri != null) AsyncImage(model = mainImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
            }

            // Sub Photos Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(subImage1Uri to sub1Picker, subImage2Uri to sub2Picker).forEach { (uri, picker) ->
                    Card(
                        modifier = Modifier.weight(1f).height(90.dp).clickable { picker.launch("image/*") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (uri != null) AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            else Icon(Icons.Default.Add, null, tint = Color.Gray)
                        }
                    }
                }
            }

            LabelText("Type")
            RoundedDropdown(selected = petType, options = listOf("dog", "cat")) { selectedType ->
                petType = selectedType
                petBreed = if (selectedType == "dog") "Aspin" else "Puspin"
            }

            LabelText("Name")
            RoundedTextField(value = petName, onValueChange = { petName = it }, placeholder = "Max")

            LabelText("Age")
            RoundedTextField(value = petAge, onValueChange = { petAge = it }, placeholder = "1 year old")

            LabelText("Sex")
            RoundedDropdown(selected = petSex, options = listOf("Male", "Female")) { petSex = it }

            LabelText("Breed")
            RoundedEditableDropdown(value = petBreed, options = currentBreeds, onValueChange = { petBreed = it })

            LabelText("Description")
            RoundedTextField(value = petDescription, onValueChange = { petDescription = it }, placeholder = "Friendly...", singleLine = false, modifier = Modifier.height(120.dp))

            LabelText("Health Status")
            RoundedTextField(value = healthStatus, onValueChange = { healthStatus = it }, placeholder = "Vaccinated...", singleLine = false, modifier = Modifier.height(120.dp))

            // Keep Google Drive Organizer (Slightly Smaller)
            Text(text = "External Organizer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1SY1pczmOL7b-lAL6CoOzIW4p1r9UWqWH"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(45.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A).copy(alpha = 0.8f))
            ) {
                Text("Open Google Drive", color = Color.White)
            }

            //TOP LINE SEPARATOR

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Text(
                text = "Please upload images for review. If they don't meet our standards, we'll email you to ensure the best experience for adopters.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                lineHeight = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // 🆕 BOTTOM LINE
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))


            Button(
                onClick = {
                    if (petName.isBlank() || mainImageUri == null) {
                        Toast.makeText(context, "Fill Name and Main Photo", Toast.LENGTH_SHORT).show()
                    } else {
                        onSavePet(petName, petType, petBreed, petAge, petSex, petDescription, healthStatus, mainImageUri, listOfNotNull(subImage1Uri, subImage2Uri))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A))
            ) {
                Text("Save Pet", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// Ensure your Helper UI components (RoundedTextField, etc.) are below here...
@Composable
fun LabelText(text: String) { Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp)) }

@Composable
fun RoundedTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier, singleLine: Boolean = true) {
    TextField(value = value, onValueChange = onValueChange, modifier = modifier.fillMaxWidth(), placeholder = { Text(placeholder) }, singleLine = singleLine, shape = RoundedCornerShape(15.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFEFEFEF), unfocusedContainerColor = Color(0xFFEFEFEF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedDropdown(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(value = selected, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }, shape = RoundedCornerShape(15.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFEFEFEF), unfocusedContainerColor = Color(0xFFEFEFEF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedEditableDropdown(value: String, options: List<String>, onValueChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(value = value, onValueChange = { onValueChange(it); expanded = true }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(15.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFEFEFEF), unfocusedContainerColor = Color(0xFFEFEFEF), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onValueChange(option); expanded = false }) }
        }
    }
}