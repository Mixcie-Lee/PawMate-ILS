package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AdoptionCenterViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    //Track add pet status
    private val _addPetStatus = MutableStateFlow<Result<String>?>(null)
    val addPetStatus: StateFlow<Result<String>?> = _addPetStatus


    private val _shelterPets = MutableStateFlow<List<PetData>>(emptyList())
    val shelterPets: StateFlow<List<PetData>> = _shelterPets

    private val _uploadedPetsCount = MutableStateFlow(0) // THE DYNAMIC COUNTER
    val uploadedPetsCount: StateFlow<Int> = _uploadedPetsCount

// ------------------------------------

    init {
        val currentUser = authViewModel.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            // LOGIC 1: THE DYNAMIC COUNTER (Firestore Only)
            // This ensures the count starts at 0 and ignores local samples
            db.collection("pets")
                .whereEqualTo("shelterId", uid)
                .addSnapshotListener { snapshot, _ ->
                    _uploadedPetsCount.value = snapshot?.size() ?: 0
                }

            // THE SPEED FIX: START LISTENING FOR LIST CHANGES
            // This ensures new pets appear in the Manage List instantly without restart
            observePets(shelterId = uid) { updatedList ->
                _shelterPets.value = updatedList
            }
        }







    }














    fun addPet(
        NewPet : PetData
    ) {
        val currentUser = authViewModel.currentUser
        if (currentUser == null) {
            _addPetStatus.value = Result.failure(Exception("user not logged in"))
            return
        }
        val shelterId = currentUser.uid
        val shelterName = currentUser.displayName ?: "Unknown Shelter"
        val petId = db.collection("pets").document().id

        val petWithOwner = NewPet.copy(
            petId = petId,
            shelterId = shelterId,
            shelterName = shelterName
        )
        viewModelScope.launch {
            db.collection("pets")
                .document(petId)
                .set(petWithOwner)
                .addOnSuccessListener {
                    _addPetStatus.value = Result.success("Pet added successfully")
                }
                .addOnFailureListener { e ->
                    _addPetStatus.value = Result.failure(e)
                }
        }
        /* Fetch all pets from firestore with optional filters heheh

         */

   /* fun uploadPetImages(uris: List<Uri>, onComplete: (List<String>) -> Unit) {
        val storage = Firebase.storage.reference
        val uploadedUrls = mutableListOf<String>()

        uris.forEach { uri ->
            val ref = storage.child("pets/${UUID.randomUUID()}")
            ref.putFile(uri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    uploadedUrls.add(url.toString())
                    if (uploadedUrls.size == uris.size) {
                        onComplete(uploadedUrls)
                    }
                }
            }
        }
    }
*/

        fun getPets(
            shelterId: String? = null,
            type: String? = null,
            onComplete: (List<PetData>) -> Unit
        ) {
            db.collection("pets")
                .get()
                .addOnSuccessListener { snapshot ->
                    val pets = snapshot.documents.mapNotNull { it.toObject(PetData::class.java) }
                        .filter { pet ->
                            (shelterId == null || pet.shelterId == shelterId) &&
                                    (type == null || pet.type == type)
                        }
                    onComplete(pets)
                }
                .addOnFailureListener {
                    onComplete(emptyList())
                }
        }
    }
    fun observePets(
        shelterId: String? = null,
        type: String? = null,
        onUpdate: (List<PetData>) -> Unit
    ) {
        db.collection("pets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val pets = snapshot?.documents?.mapNotNull { it.toObject(PetData::class.java) }
                    ?.filter { pet ->
                        (shelterId == null || pet.shelterId == shelterId) &&
                                (type == null || pet.type == type)
                    } ?: emptyList()
                onUpdate(pets)
            }
    }

    // --- LOGIC 2: THE DELETE FUNCTION ---
    fun deletePet(petId: String) {
        viewModelScope.launch {
            db.collection("pets")
                .document(petId) // Finds the specific target
                .delete()
                .addOnSuccessListener {
                    // Success! Logic #1 will automatically update the UI count
                    _addPetStatus.value = Result.success("Pet deleted successfully")
                }
                .addOnFailureListener { e ->
                    _addPetStatus.value = Result.failure(e)
                }
        }
    }
//UPDATE PETS IMPLEMENTATION ALLOW THE USERS TO MODIFY THE PET
// Logic #3: THE UPDATE FUNCTION
fun updatePet(petId: String, updatedData: Map<String, Any>) {
    viewModelScope.launch {
        db.collection("pets")
            .document(petId)
            .update(updatedData) // Overwrites only the specified fields
            .addOnSuccessListener {
                _addPetStatus.value = Result.success("Pet updated successfully")
            }
            .addOnFailureListener { e ->
                _addPetStatus.value = Result.failure(e)
            }
    }
}








}

