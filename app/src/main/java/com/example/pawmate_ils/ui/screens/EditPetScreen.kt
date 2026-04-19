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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.example.pawmate_ils.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPetScreen(
    navController: NavController,
    viewModel: AdoptionCenterViewModel,
    authViewModel: AuthViewModel, // 🆕 Added to handle Cloudinary uploads
    petId: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()


    // 1. Inherited State Variables
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("dog") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("1") }
    var petSex by remember { mutableStateOf("Male") }
    var petDescription by remember { mutableStateOf("") }
    var healthStatus by remember { mutableStateOf("") }

    // 📸 2. IMAGE STATES (Handles String URLs from DB or local Uris from Gallery)
    var mainImage by remember { mutableStateOf<Any?>(null) }
    var subImage1 by remember { mutableStateOf<Any?>(null) }
    var subImage2 by remember { mutableStateOf<Any?>(null) }

    // Image Pickers
    val mainPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) mainImage = it }
    val sub1Picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) subImage1 = it }
    val sub2Picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { if (it != null) subImage2 = it }

    val dogBreeds = listOf("Aspin", "Golden Retriever", "Shih Tzu", "Pomeranian", "Chow Chow", "Beagle", "Labrador")
    val catBreeds = listOf("Puspin", "Siamese", "Persian", "Maine Coon", "Bengal", "British Shorthair")
    val currentBreeds = if (petType == "dog") dogBreeds else catBreeds

    var showDeleteDialog by remember { mutableStateOf(false) }


    // 3. AUTO-FILL LOGIC: Fetches cloud data to fill the inputs
    LaunchedEffect(petId) {
        val existingPet = viewModel.shelterPets.value.find { it.petId == petId }
        existingPet?.let {
            petName = it.name ?: ""
            petType = it.type ?: "dog"
            petBreed = it.breed ?: ""
            petAge = it.age ?: "1"
            petSex = it.gender ?: "Male"
            petDescription = it.description ?: ""
            healthStatus = it.healthStatus ?: ""

            // Fill images with existing URLs from Firestore
            mainImage = it.imageUrl
            subImage1 = it.additionalImages.getOrNull(0)
            subImage2 = it.additionalImages.getOrNull(1)
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
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 📸 4. IMAGE SECTION
            LabelText("Update Pet Photos")
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp).clickable { mainPicker.launch("image/*") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (mainImage != null) {
                        AsyncImage(model = mainImage, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(subImage1 to sub1Picker, subImage2 to sub2Picker).forEach { (img, picker) ->
                    Card(
                        modifier = Modifier.weight(1f).height(90.dp).clickable { picker.launch("image/*") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (img != null) {
                                AsyncImage(model = img, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Default.Add, null, tint = Color.Gray)
                            }
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
            RoundedDropdown(selected = petAge, options = (1..20).map { it.toString() }) { petAge = it }

            LabelText("Sex")
            RoundedDropdown(selected = petSex, options = listOf("Male", "Female")) { petSex = it }

            LabelText("Breed")
            RoundedEditableDropdown(value = petBreed, options = currentBreeds, onValueChange = { petBreed = it })

            LabelText("Description")
            RoundedTextField(value = petDescription, onValueChange = { petDescription = it }, placeholder = "Friendly...", singleLine = false, modifier = Modifier.height(120.dp))

            LabelText("Health Status")
            RoundedTextField(value = healthStatus, onValueChange = { healthStatus = it }, placeholder = "Vaccinated...")

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f))

            // 📂 GOOGLE DRIVE SECTION (Slightly Smaller)
            Text(text = "External Organizer", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1SY1pczmOL7b-lAL6CoOzIW4p1r9UWqWH"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD67A7A).copy(alpha = 0.8f))
            ) {
                Text("Open Google Drive", color = Color.White)
            }
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


            // 🚀 5. SAVING LOGIC (With Cloudinary Check)
            Button(
                onClick = {
                    if (petName.isBlank() || petDescription.isBlank() || mainImage == null) {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            try {
                                // A. Handle Main Image Upload if it's a new Uri
                                val finalMainUrl = if (mainImage is Uri) {
                                    authViewModel.uploadToCloudinarySync(context, mainImage as Uri)
                                } else {
                                    mainImage as? String
                                }

                                // B. Handle Sub Images
                                val finalSubUrls = mutableListOf<String>()
                                listOf(subImage1, subImage2).forEach { img ->
                                    if (img is Uri) {
                                        authViewModel.uploadToCloudinarySync(context, img)?.let { finalSubUrls.add(it) }
                                    } else if (img is String) {
                                        finalSubUrls.add(img)
                                    }
                                }

                                val updatedData = mapOf(
                                    "name" to petName,
                                    "type" to petType,
                                    "breed" to petBreed,
                                    "age" to petAge,
                                    "gender" to petSex,
                                    "description" to petDescription,
                                    "healthStatus" to healthStatus,
                                    "imageUrl" to (finalMainUrl ?: ""),
                                    "additionalImages" to finalSubUrls
                                )

                                viewModel.updatePet(petId, updatedData)
                                Toast.makeText(context, "Pet updated!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
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