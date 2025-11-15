package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Data model for each liked pet
data class LikedPet(
    val name: String = "",
    val breed: String = "",
    val age: String = "",
    val description: String = "",
    val type: String = "",
    val imageRes: Int = 0
)

class LikedPetsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _likedPets = MutableStateFlow<List<LikedPet>>(emptyList())
    val likedPets: StateFlow<List<LikedPet>> = _likedPets

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        Log.d("LikedPetsVM", "ViewModel initialized")
        fetchLikedPets()
    }

    /** 🔹 Fetch liked pets for the currently logged-in user (real-time updates). */
    fun fetchLikedPets() {
        val userId = auth.currentUser?.uid ?: return
        if (userId == null) {
            Log.d("LikedPetsVM", "fetchLikedPets: userId is null!")
            return
        }
        Log.d("LikedPetsVM", "fetchLikedPets: userId=$userId")


        _isLoading.value = true

        firestore.collection("users")
            .document(userId)
            .collection("likedPets")
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                        Log.e("LikedPetsVM", "SnapshotListener error: ${e.message}")
                        return@addSnapshotListener

                }

                val pets = snapshot?.documents?.mapNotNull { it.toObject(LikedPet::class.java) }
                    ?: emptyList()
                Log.d("LikedPetsVM", "Fetched ${pets.size} liked pets")
                _likedPets.value = pets
            }
    }

    /** ❤️ Add a pet to Firestore when user swipes right. */
    fun addLikedPet(pet: PetData) {
        val userId = auth.currentUser?.uid ?: return
        if (userId == null) {
            Log.d("LikedPetsVM", "addLikedPet: userId is null!")
            return
        }

        // 🔹 Convert PetData → LikedPet
        val likedPet = LikedPet(
            name = pet.name ?: "unknown",
            breed = pet.breed ?: "no breed",
            age = pet.age ?: "undefined age",
            description = pet.description ?: "no description",
            type = pet.type ?: "undefined type",
            imageRes = pet.imageRes
        )
        Log.d("LikedPetsVM", "Adding liked pet: ${likedPet.name}")


        viewModelScope.launch {
            firestore.collection("users")
                .document(userId)
                .collection("likedPets")
                .document(likedPet.name) // using name as ID; can change to UUID if needed
                .set(likedPet)
                .addOnSuccessListener {
                    Log.d("LikedPetsVM", "Pet added successfully")
                }
                .addOnFailureListener {
                    Log.e("LikedPetsVM", "Failed to add pet: ${it.message}")
                }

        }
        }


    /** 💔 Remove a liked pet (e.g., from adopter like screen). */
    fun removeLikedPet(petName: String) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            firestore.collection("users")
                .document(userId)
                .collection("likedPets")
                .document(petName)
                .delete()
                .addOnFailureListener {
                    _errorMessage.value = it.message
                }
        }
    }

    /** 🔢 Get the total number of liked pets (in-memory). */
    fun getLikedPetsCount(): Int = _likedPets.value.size

    /** 🗑️ Delete all liked pets (used during account deletion). */
    fun deleteAllLikedPets(onComplete: (Boolean) -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("likedPets")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                snapshot.documents.forEach { doc -> batch.delete(doc.reference) }

                batch.commit()
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }
}
