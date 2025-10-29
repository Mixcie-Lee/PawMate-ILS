package com.example.pawmate_ils.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewMdelFactory
import com.example.pawmate_ils.Firebase_Utils.AdoptionCenterViewModel
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addPetScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val adoptionViewModel: AdoptionCenterViewModel = viewModel(
        factory = AdoptionCenterViewMdelFactory(authViewModel)
    )
    val context = LocalContext.current

    // Pet details state
    var petName by remember { mutableStateOf("") }
    var petBreed by remember { mutableStateOf("") }
    var petAge by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("dog") }
    val petTypes = listOf("dog", "cat")

    // Observe addPetStatus
    val addPetStatus by adoptionViewModel.addPetStatus.collectAsState()

    LaunchedEffect(addPetStatus) {
        addPetStatus?.let { result ->
            result.onSuccess { message ->
                Toast.makeText(context, "SUCCESS: $message", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }.onFailure { e ->
                Toast.makeText(context, "FAILED: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Pet") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pet Name
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                label = { Text("Pet Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Pet Breed
            OutlinedTextField(
                value = petBreed,
                onValueChange = { petBreed = it },
                label = { Text("Pet Breed") },
                modifier = Modifier.fillMaxWidth()
            )

            // Pet Age
            OutlinedTextField(
                value = petAge,
                onValueChange = { petAge = it },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Pet Description
            OutlinedTextField(
                value = petDescription,
                onValueChange = { petDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // Pet Type Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = petType.capitalize(),
                    onValueChange = {},
                    label = { Text("Type") },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    petTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.capitalize()) },
                            onClick = {
                                petType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 🔹 Google Drive Upload Section
            Text(
                text = "Upload Pet Images via Google Drive",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {
                    val gDriveIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://drive.google.com/drive/folders/1SY1pczmOL7b-lAL6CoOzIW4p1r9UWqWH?usp=drive_link")
                    )
                    context.startActivity(gDriveIntent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Open Google Drive Folder")
            }

            Text(
                text = "Please upload clear images of your pets in the shared Google Drive folder. " +
                        "Our team will review and add them to the app.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // ✅ Save Pet Info Button
            Button(
                onClick = {
                    if (petName.isBlank() || petBreed.isBlank() || petAge.isBlank() || petDescription.isBlank()) {
                        Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    adoptionViewModel.addPet(
                        name = petName,
                        breed = petBreed,
                        age = petAge,
                        description = petDescription,
                        type = petType,
                        imageRes = emptyList() // Images will be manually added later
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Pet Info")
            }
        }
    }
}
