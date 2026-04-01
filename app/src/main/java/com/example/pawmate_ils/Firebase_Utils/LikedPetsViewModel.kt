package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
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
    val imageRes: Int = 0,
    /** Subcollection document id — used for delete; not written to Firestore. */
    @get:Exclude
    val documentId: String = ""
)

/**
 * Firestore [DocumentSnapshot.getString] throws if the field exists but is not a String.
 * Older or hand-edited documents may use other types — never crash the favorites screen.
 */
private fun DocumentSnapshot.readFieldAsString(key: String): String {
    if (!contains(key)) return ""
    return try {
        when (val v = get(key)) {
            null -> ""
            is String -> v
            is Number -> v.toString()
            is Boolean -> if (v) "true" else "false"
            else -> v.toString()
        }
    } catch (_: Exception) {
        ""
    }
}

private fun DocumentSnapshot.readFieldAsInt(key: String): Int {
    if (!contains(key)) return 0
    return try {
        when (val v = get(key)) {
            null -> 0
            is Int -> v
            is Long -> v.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
            is Double -> v.toInt()
            is String -> v.trim().toIntOrNull() ?: 0
            else -> 0
        }
    } catch (_: Exception) {
        0
    }
}

private fun DocumentSnapshot.toLikedPetOrNull(): LikedPet? {
    return try {
        val petName = readFieldAsString("name").ifBlank { id }
        if (petName.isBlank()) return null
        LikedPet(
            name = petName,
            breed = readFieldAsString("breed"),
            age = readFieldAsString("age"),
            description = readFieldAsString("description"),
            type = readFieldAsString("type"),
            imageRes = readFieldAsInt("imageRes"),
            documentId = id
        )
    } catch (e: Exception) {
        Log.e("LikedPetsVM", "Failed to parse liked pet doc ${id}", e)
        null
    }
}

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
        Log.d("LikedPetsVM", "fetchLikedPets: userId=$userId")


        _isLoading.value = true

        firestore.collection("users")
            .document(userId)
            .collection("likedPets")
            .addSnapshotListener { snapshot, e ->
                try {
                    _isLoading.value = false
                    if (e != null) {
                        Log.e("LikedPetsVM", "SnapshotListener error: ${e.message}")
                        return@addSnapshotListener
                    }

                    val pets = snapshot?.documents?.mapNotNull { doc ->
                        runCatching { doc.toLikedPetOrNull() }
                            .onFailure { Log.e("LikedPetsVM", "Doc parse: ${doc.id}", it) }
                            .getOrNull()
                    } ?: emptyList()
                    Log.d("LikedPetsVM", "Fetched ${pets.size} liked pets")
                    _likedPets.value = pets
                } catch (t: Throwable) {
                    Log.e("LikedPetsVM", "Snapshot listener crashed", t)
                    _likedPets.value = emptyList()
                }
            }
    }

    /** ❤️ Add a pet to Firestore when user swipes right. */
    fun addLikedPet(pet: PetData) {
        val userId = auth.currentUser?.uid ?: return

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
    fun removeLikedPet(pet: LikedPet) {
        val userId = auth.currentUser?.uid ?: return
        val docId = pet.documentId.ifBlank { pet.name }
        if (docId.isBlank()) {
            Log.w("LikedPetsVM", "removeLikedPet: empty document id")
            return
        }

        viewModelScope.launch {
            firestore.collection("users")
                .document(userId)
                .collection("likedPets")
                .document(docId)
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
