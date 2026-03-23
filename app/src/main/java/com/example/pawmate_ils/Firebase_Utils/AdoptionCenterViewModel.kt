package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdoptionCenterViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private var petsListener: ListenerRegistration? = null

    private val _shelterPets = MutableStateFlow<List<PetData>>(emptyList())
    val shelterPets: StateFlow<List<PetData>> = _shelterPets

    // Track add pet status
    private val _addPetStatus = MutableStateFlow<Result<String>?>(null)
    val addPetStatus: StateFlow<Result<String>?> = _addPetStatus

    init {
        listenToShelterPets()
    }

    fun refreshShelterPets() {
        listenToShelterPets()
    }

    private fun listenToShelterPets() {
        val currentUser = authViewModel.currentUser
        if (currentUser == null) {
            _shelterPets.value = emptyList()
            return
        }

        petsListener?.remove()
        petsListener = db.collection("pets")
            .whereEqualTo("shelterId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _shelterPets.value = emptyList()
                    return@addSnapshotListener
                }

                _shelterPets.value = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PetData::class.java)?.copy(
                        petId = doc.getString("petId") ?: doc.id
                    )
                } ?: emptyList()
            }
    }

    /* adds a new pet to Firestore (root-level "pets" collection)
      and attaches the current shelter info from authViewModel.
     */
    fun addPet(newPet: PetData) {
        val currentUser = authViewModel.currentUser
        if (currentUser == null) {
            _addPetStatus.value = Result.failure(Exception("User not logged in"))
            return
        }

        val petId = db.collection("pets").document().id
        val petToSave = newPet.copy(
            petId = petId,
            shelterId = currentUser.uid,
            shelterName = currentUser.displayName ?: "Unknown Shelter"
        )

        viewModelScope.launch {
            db.collection("pets")
                .document(petId)
                .set(petToSave)
                .addOnSuccessListener {
                    _addPetStatus.value = Result.success("Pet added successfully")
                }
                .addOnFailureListener { e ->
                    _addPetStatus.value = Result.failure(e)
                }
        }
    }

    fun deletePet(petId: String) {
        db.collection("pets")
            .document(petId)
            .delete()
    }

    fun updatePet(petId: String, updatedData: Map<String, Any>) {
        db.collection("pets")
            .document(petId)
            .update(updatedData)
    }

    fun getPets(
        shelterId: String? = null,
        type: String? = null,
        onComplete: (List<PetData>) -> Unit
    ) {
        db.collection("pets")
            .get()
            .addOnSuccessListener { snapshot ->
                val pets = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PetData::class.java)?.copy(
                        petId = doc.getString("petId") ?: doc.id
                    )
                }.filter { pet ->
                    (shelterId == null || pet.shelterId == shelterId) &&
                        (type == null || pet.type == type)
                }
                onComplete(pets)
            }
            .addOnFailureListener {
                onComplete(emptyList())
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
                val pets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PetData::class.java)?.copy(
                        petId = doc.getString("petId") ?: doc.id
                    )
                }
                    ?.filter { pet ->
                        (shelterId == null || pet.shelterId == shelterId) &&
                            (type == null || pet.type == type)
                    } ?: emptyList()
                onUpdate(pets)
            }
    }

    override fun onCleared() {
        petsListener?.remove()
        super.onCleared()
    }
}
