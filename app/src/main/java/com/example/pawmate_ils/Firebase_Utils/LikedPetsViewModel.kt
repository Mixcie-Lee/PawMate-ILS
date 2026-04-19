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
import com.example.pawmate_ils.GemManager
import kotlinx.coroutines.tasks.await

// Data model for each liked pet
data class LikedPet(
    val name: String = "",
    val breed: String = "",
    val age: String = "",
    val description: String = "",
    val type: String = "",
    val imageRes: Int = 0,
    val shelterId: String = "", // 🎯 Added this
    val petId: String = "",
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
            shelterId = readFieldAsString("shelterId"),
            petId = readFieldAsString("petId"),
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

    private val firestoreRepo = FirestoreRepository()

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
                    if (e != null) {
                        _isLoading.value = false
                        Log.e("LikedPetsVM", "SnapshotListener error: ${e.message}")
                        return@addSnapshotListener
                    }

                    // Eto yung original parsing mo
                    val pets = snapshot?.documents?.mapNotNull { doc ->
                        runCatching { doc.toLikedPetOrNull() }
                            .onFailure { Log.e("LikedPetsVM", "Doc parse: ${doc.id}", it) }
                            .getOrNull()
                    } ?: emptyList()

                    // 🚀 ADDED LOGIC: Verification Loop para sa Cascade Deletion
                    viewModelScope.launch {
                        val validatedPetsList = mutableListOf<LikedPet>()

                        for (pet in pets) {
                            // Gagamit tayo ng fallback ID logic base sa code mo
                            val petIdToCheck = pet.petId.ifBlank { pet.documentId }

                            try {
                                // Tinitignan sa main gallery kung existing pa yung record
                                val mainRegistryDoc = firestore.collection("pets").document(petIdToCheck).get().await()

                                if (mainRegistryDoc.exists()) {
                                    validatedPetsList.add(pet)
                                } else {
                                    // Kapag binura na ng Shelter, lilinisin natin ang "ghost record" sa user
                                    Log.d("LikedPetsVM", "Detected deleted pet: ${pet.name}. Cleaning up favorites.")
                                    firestore.collection("users").document(userId)
                                        .collection("likedPets").document(pet.documentId).delete()
                                }
                            } catch (err: Exception) {
                                Log.e("LikedPetsVM", "Error checking existence of ${pet.name}", err)
                                validatedPetsList.add(pet) // Safe default: i-keep pag may connection error
                            }
                        }

                        // Update the final state flow
                        _likedPets.value = validatedPetsList
                        _isLoading.value = false
                        Log.d("LikedPetsVM", "Final validated list: ${validatedPetsList.size} pets")
                    }

                } catch (t: Throwable) {
                    _isLoading.value = false
                    Log.e("LikedPetsVM", "Snapshot listener crashed", t)
                    _likedPets.value = emptyList()
                }
            }
    }

    /** ❤️ Add a pet to Firestore when user swipes right. */
    fun addLikedPet(pet: PetData) {
        val userId = auth.currentUser?.uid ?: return

        val adopterName = auth.currentUser?.displayName ?: "An Adopter"
        // 🔹 Convert PetData → LikedPet
        val likedPet = LikedPet(
            name = pet.name ?: "unknown",
            breed = pet.breed ?: "no breed",
            age = pet.age ?: "undefined age",
            description = pet.description ?: "no description",
            type = pet.type ?: "undefined type",
            imageRes = pet.imageRes,
            shelterId = pet.shelterId ?: "",
            petId = pet.petId ?: ""
        )
        Log.d("LikedPetsVM", "Adding liked pet: ${likedPet.name}")


        viewModelScope.launch {
            firestore.collection("users")
                .document(userId)
                .collection("likedPets")
                .document(likedPet.petId.ifBlank { likedPet.name }) // using name as ID; can change to UUID if needed
                .set(likedPet)
                .addOnSuccessListener {
                    Log.d("LikedPetsVM", "Pet added successfully")
                    if (likedPet.shelterId.isNotEmpty()) {
                        viewModelScope.launch {
                            firestoreRepo.triggerMatchNotification(
                                adopterName = adopterName,
                                shelterId = likedPet.shelterId,
                                petName = likedPet.name
                            )
                        }
                    }

                }






                .addOnFailureListener {
                    Log.e("LikedPetsVM", "Failed to add pet: ${it.message}")
                }

        }
        }


    /** 💔 Remove a liked pet (e.g., from adopter like screen). */
    fun removeLikedPet(pet: LikedPet) {
        val userId = auth.currentUser?.uid ?: return

        unfavoriteAndRestore(pet)
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

    fun unfavoriteAndRestore(pet: LikedPet) {
        viewModelScope.launch {
            try {
                // 1. Setup IDs and Auth
                val adopterId = auth.currentUser?.uid ?: return@launch
                val petId = pet.petId.ifBlank { pet.documentId }
                val shelterId = pet.shelterId
                val adopterName = auth.currentUser?.displayName ?: "An adopter"


                //GemManager.addGems(5) TENTATIVE ATM PLEASE BRING BACK

                // 2. Validation check
                if (petId.isBlank()) {
                    Log.e("RestoreError", "Cannot restore: Pet ID is missing.")
                    return@launch
                }

                // 3. 🔔 Notify the Shelter (Do this before deleting the channel)
                if (shelterId.isNotEmpty()) {
                    firestoreRepo.sendNotification(
                        receiverId = shelterId,
                        title = "Match Withdrawn 💔",
                        message = "$adopterName is no longer interested in ${pet.name}."
                    )
                    firestoreRepo.deleteChannelCompletely(adopterId, shelterId)
                    Log.d("Restore", "Notification sent to shelter: $shelterId")
                }

                // 4. 🔥 Database Cleanup
                // Remove from Adopter's Favorites collection
                firestoreRepo.removePetFromFavorites(adopterId, petId)

                // Remove from Swiped history so it reappears in the Swipe Screen
                firestoreRepo.removePetFromSwipedHistory(adopterId, petId)



                Log.d("Restore", "Success: ${pet.name} (ID: $petId) is now back in the Discovery deck.")

            } catch (e: Exception) {
                Log.e("RestoreError", "Failed to restore pet: ${e.message}")
            }
        }
    }

    fun removeSinglePetKeepChannel(pet: LikedPet) {
        viewModelScope.launch {
            try {
                val adopterId = auth.currentUser?.uid ?: return@launch
                val petId = pet.petId.ifBlank { pet.documentId }
                val shelterId = pet.shelterId

                if (petId.isBlank()) return@launch

                // 1. Remove from Adopter's Favorites
                firestoreRepo.removePetFromFavorites(adopterId, petId)

                // 2. Remove from Swiped history (returns to Discovery deck)
                firestoreRepo.removePetFromSwipedHistory(adopterId, petId)

                // 3. Update the Channel's pet names list (Remove JUST this pet)
                if (shelterId.isNotEmpty()) {
                    val channelId = "$adopterId-$shelterId"
                    firestoreRepo.removePetNameFromChannel(channelId, pet.name)
                    Log.d("Restore", "Updated channel $channelId: Removed ${pet.name}")
                }

            } catch (e: Exception) {
                Log.e("RestoreError", "Failed to partially remove pet: ${e.message}")
            }
        }
    }


}
