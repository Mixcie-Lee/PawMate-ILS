package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AdoptionCenterViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    // Sa ilalim ng private val db = FirebaseFirestore.getInstance()
    private val firestoreRepo = FirestoreRepository()

    private val _addPetStatus = MutableStateFlow<Result<String>?>(null)
    val addPetStatus: StateFlow<Result<String>?> = _addPetStatus

    private val _shelterPets = MutableStateFlow<List<PetData>>(emptyList())
    val shelterPets: StateFlow<List<PetData>> = _shelterPets

    private val _uploadedPetsCount = MutableStateFlow(0)
    val uploadedPetsCount: StateFlow<Int> = _uploadedPetsCount.asStateFlow()


    init {
        // 🔹 START DYNAMIC OBSERVER IMMEDIATELY
        startObservingShelterData()
    }

    private fun startObservingShelterData() {
        val currentUser = authViewModel.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            // 🟢 FIXED: Combined count and list into ONE server-side filtered listener
            // This is "Push-based" - Firestore tells the app when things change.
            db.collection("pets")
                .whereEqualTo("shelterId", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("SHELTER_VM", "Firestore Listen Failed", error)
                        return@addSnapshotListener
                    }

                    val pets = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(PetData::class.java)?.copy(petId = doc.id)
                    } ?: emptyList()

                    // Update UI StateFlows instantly
                    _shelterPets.value = pets
                    _uploadedPetsCount.value = pets.size

                    Log.d("SHELTER_VM", "Dynamic Sync: ${pets.size} pets found.")
                }
        }
    }

    fun addPet(newPet: PetData) {
        val currentUser = authViewModel.currentUser ?: return
        val shelterId = currentUser.uid
        val shelterName = currentUser.displayName ?: "Unknown Shelter"
        val petRef = db.collection("pets").document()
        val petId = db.collection("pets").document().id

        val petWithOwner = newPet.copy(
            petId = petId,
            shelterId = shelterId,
            shelterName = shelterName
        )

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 🎯 Use the reference we created above
                    petRef.set(petWithOwner).await()
                }
                _addPetStatus.value = Result.success("Pet added successfully")
            } catch (e: Exception) {
                _addPetStatus.value = Result.failure(e)
                Log.e("SHELTER_VM", "Add Pet Failed", e)
            }
        }
    }

    // 🟢 FIXED: This function is    now properly closed and independent
    fun listenToUploadedPetsCount(shelterId: String) {
        db.collection("pets")
            .whereEqualTo("shelterId", shelterId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AdoptionVM", "Count listener failed", error)
                    return@addSnapshotListener
                }
                val count = snapshot?.size() ?: 0
                _uploadedPetsCount.value = count
            }
    }

    // 🟢 FIXED: Moved outside of the listener function
    fun deletePet(petId: String) {
        viewModelScope.launch {
            try {

                db.collection("pets").document(petId).delete().await()

                // 2. Optional: Manual local update (Firestore listener usually handles this)
                val currentList = _shelterPets.value.toMutableList()
                currentList.removeAll { it.petId == petId }
                _shelterPets.value = currentList

                Log.d("AdoptionVM", "✅ Pet $petId deleted from Firestore and Local State")
            } catch (e: Exception) {
                Log.e("AdoptionVM", "❌ Delete failed: ${e.message}")
            }
        }
    }

    // 🟢 FIXED: Moved outside of the listener function
    fun updatePet(petId: String, updatedData: Map<String, Any>) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.collection("pets")
                        .document(petId)
                        .update(updatedData)
                        .await()
                }
                _addPetStatus.value = Result.success("Pet updated successfully")
                Log.d("SHELTER_VM", "Update Success: $petId")
            } catch (e: Exception) {
                _addPetStatus.value = Result.failure(e)
                Log.e("SHELTER_VM", "Update Failed", e)
            }
        }
    }

    fun startShelterPetsListener(shelterId: String) {
        db.collection("pets")
            .whereEqualTo("shelterId", shelterId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("VM", "Listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val petsList = snapshot.toObjects(PetData::class.java)
                    _shelterPets.value = petsList // 🆕 This immediately triggers the UI update
                }
            }
    }

    fun addPetWithImages(
        context: Context,
        name: String,
        type: String,
        breed: String,
        age: String,
        gender: String,
        description: String,
        healthStatus: String,
        mainImageUri: Uri?,
        subImageUris: List<Uri>,
        shelterId: String,
        shelterName: String?,
        shelterAddress: String?
    ) {
        viewModelScope.launch {
            try {
                // 🔹 Set a "Loading" state so the UI knows we're working
                _addPetStatus.value = null

                // 1. Upload Main Image to Cloudinary
                var uploadedMainUrl: String? = null
                if (mainImageUri != null) {
                    uploadedMainUrl = authViewModel.uploadToCloudinarySync(context, mainImageUri)
                }

                // 2. Upload Sub Images (Sequential for stability)
                val uploadedSubUrls = mutableListOf<String>()
                for (uri in subImageUris) {
                    val url = authViewModel.uploadToCloudinarySync(context, uri)
                    if (url != null) {
                        uploadedSubUrls.add(url)
                    }
                }

                // 3. Prepare the PetData Object
                val petRef = db.collection("pets").document() // Create the ref first
                val actualId = petRef.id
                val petData = PetData(
                    petId = actualId,
                    name = name,
                    type = type,
                    breed = breed,
                    age = age,
                    gender = gender,
                    description = description,
                    healthStatus = healthStatus,
                    imageUrl = uploadedMainUrl,
                    additionalImages = uploadedSubUrls, // Now a List<String>
                    shelterId = shelterId,
                    shelterName = shelterName,
                    shelterAddress = shelterAddress,
                    shelterIsOnline = true,
                    shelterLastActive = System.currentTimeMillis()
                )

                // 4. Save to Firestore
                withContext(Dispatchers.IO) {
                    petRef.set(petData).await()
                }

                _addPetStatus.value =
                    Result.success("Pet profile created with ${uploadedSubUrls.size + 1} photos!")
            } catch (e: Exception) {
                _addPetStatus.value = Result.failure(e)
                Log.e("SHELTER_VM", "Multi-image upload failed", e)
            }
        }
    }

    fun deletePetWithReason(pet: PetData, reason: String) {
        viewModelScope.launch {
            try {
                val petId = pet.petId ?: ""
                val petName = pet.name ?: "A pet"


                val affectedChannels = db.collection("channels")
                    .whereArrayContains("petNames", petName)
                    .get().await()

                // 2. Padalhan sila ng notification tungkol sa deletion reason
                affectedChannels.documents.forEach { doc ->
                    val adopterId = doc.getString("adopterId") ?: ""
                    if (adopterId.isNotEmpty()) {
                        firestoreRepo.sendNotification(
                            receiverId = adopterId,
                            title = "Pet Availability Update 🐾",
                            message = "$petName was removed. Reason: $reason"
                        )
                    }
                }

                // 3. I-delete ang main document
                db.collection("pets").document(petId).delete().await()
            } catch (e: Exception) {
                Log.e("DELETE_LOGIC", "Error: ${e.message}")
            }
        }
    }
}