package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
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

    /* adds a new pet to Firestore (root-level "pets" collection)
      and attaches the current shelter info from authViewModel.
     */
    fun addPet(
        name: String,
        breed: String,
        age: String,
        description: String,
        type: String,
        imageRes: List<Int> = emptyList()
    ) {
        val currentUser = authViewModel.currentUser
        if (currentUser == null) {
            _addPetStatus.value = Result.failure(Exception("user not logged in"))
            return
        }
        val shelterId = currentUser.uid
        val shelterName = currentUser.displayName ?: "Unknown Shelter"
        val petId = db.collection("pets").document().id

        val newPet = PetData(
            name = name,
            breed = breed,
            age = age,
            description = description,
            type = type,
            shelterId = shelterId,
            shelterName = shelterName,
            imageRes = imageRes,
        )
        viewModelScope.launch {
            db.collection("pets")
                .document(petId)
                .set(newPet)
                .addOnSuccessListener {
                    _addPetStatus.value = Result.success("Pet added successfully")
                }
                .addOnFailureListener { e ->
                    _addPetStatus.value = Result.failure(e)
                }
        }
        /* Fetch all pets from firestore with optional filters heheh

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
    fun uploadPetImages(uris: List<Uri>, onComplete: (List<String>) -> Unit) {
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



}

