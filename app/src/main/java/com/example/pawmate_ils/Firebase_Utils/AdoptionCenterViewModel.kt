package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
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

                    val pets = snapshot?.documents?.mapNotNull {
                        it.toObject(PetData::class.java)
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
        val petId = db.collection("pets").document().id

        val petWithOwner = newPet.copy(
            petId = petId,
            shelterId = shelterId,
            shelterName = shelterName
        )

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.collection("pets")
                        .document(petId)
                        .set(petWithOwner)
                        .await()
                }
                _addPetStatus.value = Result.success("Pet added successfully")
            } catch (e: Exception) {
                _addPetStatus.value = Result.failure(e)
                Log.e("SHELTER_VM", "Add Pet Failed", e)
            }
        }
    }

    // 🟢 FIXED: This function is now properly closed and independent
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
                withContext(Dispatchers.IO) {
                    db.collection("pets").document(petId).delete().await()
                }
                _addPetStatus.value = Result.success("Pet deleted successfully")
            } catch (e: Exception) {
                _addPetStatus.value = Result.failure(e)
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
}