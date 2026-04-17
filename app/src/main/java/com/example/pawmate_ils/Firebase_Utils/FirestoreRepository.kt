package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import android.net.Uri
import android.util.Log
import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay

import java.util.UUID

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    private val adoptersCollection = db.collection("adopters")


    suspend fun getUserRole(uid: String): com.google.firebase.firestore.DocumentSnapshot {
        return db.collection("users")
            .document(uid)
            .get()
            .await()
    }

    // Add a new user
    suspend fun addUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    // Get all users
    suspend fun getAllUsers(): List<User> {
        val snapshot = usersCollection.get().await()
        return snapshot.toObjects(User::class.java)

    }

    // Get single user by ID
    suspend fun getUserById(userId: String): User? {
        if (userId.isNullOrBlank()) {
            throw IllegalArgumentException("Invalid userId passed to getUserById: '$userId'")
        }
        val snapshot = usersCollection.document(userId).get().await()
        return snapshot.toObject(User::class.java)
    }

    // Update user
    suspend fun updateUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    // Delete user
    suspend fun deleteUser(userId: String) {
        usersCollection.document(userId).delete().await()
    }

    //HANDLES PROFILE PIC CHANGES. SO THE PROFILE PIC WOULD NOT DISAPPEAR WHEN THE APP IS CLOSED.
    /* suspend fun uploadProfilePhoto(uid: String, imageUri: Uri): String? {
        return try {
            val storageRef = Firebase.storage.reference
                .child("profile_photos/$uid.jpg")

            // Upload the image
            storageRef.putFile(imageUri).await()

            // Get the download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Save URL to Firestore user document
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("profileImage", downloadUrl)
                .await()

            Log.d("UPLOAD_PROFILE_PHOTO", "Profile photo updated successfully")
            downloadUrl
        } catch (e: Exception) {
            Log.e("UPLOAD_PROFILE_PHOTO", "Upload failed", e)
            null
        }
    }
    */


    /*fun uploadPetImages(uris: List<Uri>, onComplete: (List<String>) -> Unit) {
        if (uris.isEmpty()) {
            Log.e("UPLOAD", "No URIs to upload!")
            onComplete(emptyList())
            return
        }

        val storage = Firebase.storage.reference
        val uploadedUrls = mutableListOf<String>()

        uris.forEach { uri ->
            Log.d("UPLOAD", "Uploading URI: $uri") // debug log

            val fileName = "pets/${UUID.randomUUID()}.jpg"
            val ref = storage.child(fileName)

            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        uploadedUrls.add(url.toString())
                        if (uploadedUrls.size == uris.size) {
                            onComplete(uploadedUrls)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("UPLOAD", "Failed to upload $uri", e)
                }
        }
    }
*/

    //HANDLES USERNAME, SO THE NAME WOULD NOT DISAPPEAR WHEN THE APP IS CLOSED. SAME LOGIC AS PROFILE PHOTO HANDLER :)
    suspend fun updateDisplayName(uid: String, displayName: String) {
        usersCollection.document(uid).update("name", displayName).await()
    }

    object FirestoreManager {
        val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    }

    suspend fun addLikedPet(userId: String, petId: String, petName: String, petImage: String) {
        val likedPet = hashMapOf(
            "petId" to petId,
            "petName" to petName,
            "petImage" to petImage,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users")
            .document(userId)
            .collection("likedPets")
            .document(petId)
            .set(likedPet)
            .await()
    }

    suspend fun getLikedPetsCount(userId: String): Int {
        val snapshot = db.collection("users")
            .document(userId)
            .collection("likedPets")
            .get()
            .await()
        return snapshot.size()
    }

    suspend fun removeLikedPet(userId: String, petId: String) {
        db.collection("users")
            .document(userId)
            .collection("likedPets")
            .document(petId)
            .delete()
            .await()
    }

    suspend fun updateGems(uid: String, newGems: Int) {
        try {
            db.collection("users").document(uid)
                .update("gems", newGems)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to update gems: ${e.message}")
        }
    }

    suspend fun updateLikedPetsCount(uid: String, newCount: Int) {
        try {
            db.collection("users").document(uid)
                .update("likedPetsCount", newCount)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Failed to update liked pets count: ${e.message}")
        }

    }


    // --- NEW: SHELTER-RELATED FUNCTIONS ---
    private val shelterCollection = db.collection("shelters")
    suspend fun getShelterNameById(shelterId: String): String? { // --- NEW ---
        return try {
            val snapshot = shelterCollection.document(shelterId).get().await()
            snapshot.getString("shelterName") // Assuming "shelterName" field exists
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching shelter: ${e.message}")
            null
        }
    }

    suspend fun getShelterByUserId(userId: String): Map<String, Any>? { // --- NEW ---
        return try {
            val snapshot = shelterCollection
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
            if (!snapshot.isEmpty) snapshot.documents[0].data else null
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error fetching shelter by userId: ${e.message}")
            null
        }
    }


    // ------------------- UPDATE PHONE NUMBER -------------------
// Requires a PhoneAuthCredential from verification
    suspend fun updateEmail(uid: String, newEmail: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("email", newEmail)
                .await()
            Log.d("FirestoreRepository", "✅ Email updated successfully in Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "❌ Failed to update email: ${e.message}")
            throw e
        }
    }

    suspend fun updatePhoneNumber(uid: String, newPhone: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("MobileNumber", newPhone)
                .await()
            Log.d("FirestoreRepository", "✅ Phone number updated successfully in Firestore.")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "❌ Failed to update phone: ${e.message}")
            throw e
        }
    }

    suspend fun isNewUser(uid: String): Boolean {
        return try {
            val doc = usersCollection.document(uid).get().await()
            !doc.exists()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "isNewUser() failed: ${e.message}")
            false
        }
    }

    suspend fun updateShelterPresenceInPets(shelterId: String, isOnline: Boolean) {
        try {
            val pets = db.collection("pets")
                .whereEqualTo("shelterId", shelterId)
                .get()
                .await()

            val batch = db.batch()
            for (doc in pets.documents) {
                batch.update(
                    doc.reference,
                    "shelterIsOnline", isOnline,
                    "shelterLastActive", System.currentTimeMillis()
                )
            }
            batch.commit().await()
            Log.d("FirestoreRepo", "✅ Syncing presence to all pets for $shelterId")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to sync pet presence: ${e.message}")
        }
    }

    suspend fun updateAdopterPresenceInChannels(
        adopterId: String,
        isOnline: Boolean,
        timestamp: Long
    ) {
        try {
            // Find every chat channel where this user is the adopter
            val channels = db.collection("channels")
                .whereEqualTo("adopterId", adopterId)
                .get()
                .await()

            if (channels.isEmpty) return

            val batch = db.batch()
            for (doc in channels.documents) {
                batch.update(
                    doc.reference,
                    "online", isOnline,
                    "lastActive", timestamp
                )
            }
            batch.commit().await()
            Log.d("FirestoreRepo", "✅ Adopter presence synced to ${channels.size()} channels")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Error syncing adopter presence: ${e.message}")
        }
    }

    suspend fun updateShelterPresenceInChannels(
        shelterId: String,
        isOnline: Boolean,
        timestamp: Long
    ) {
        try {
            // Find every chat channel where this user is the SHELTER
            val channels = db.collection("channels")
                .whereEqualTo("shelterId", shelterId)
                .get()
                .await()

            if (channels.isEmpty) return

            val batch = db.batch()
            for (doc in channels.documents) {
                batch.update(
                    doc.reference,
                    "online", isOnline,
                    "lastActive", timestamp
                )
            }
            batch.commit().await()
            Log.d("FirestoreRepo", "✅ Shelter presence synced to ${channels.size()} channels")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Error syncing shelter channel presence: ${e.message}")
        }
    }

    suspend fun updateChannelPetNames(channelId: String, newPetNames: List<String>) {
        try {
            db.collection("channels")
                .document(channelId)
                .update("petNames", newPetNames) // 🎯 Overwrites the list with the new pet included
                .await()
            Log.d("Firestore", "Successfully updated petNames for channel: $channelId")
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating petNames", e)
            throw e
        }
    }

    suspend fun recordSwipe(userId: String, petId: String) {
        val swipeData = hashMapOf(
            "userId" to userId,
            "petId" to petId,
            "timestamp" to System.currentTimeMillis()
        )
        // Use a composite ID "userId_petId" to prevent duplicate entries
        db.collection("swipes")
            .document("${userId}_${petId}")
            .set(swipeData)
            .await()
    }

    suspend fun markAsSwiped(userId: String, petId: String) {
        val data = hashMapOf("timestamp" to System.currentTimeMillis())
        // Save to users/USER_ID/swipedPets/PET_ID
        db.collection("users").document(userId)
            .collection("swipedPets").document(petId)
            .set(data).await()
    }

    suspend fun getSwipedPetIds(userId: String): List<String> {
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("swipedPets").get().await()
            snapshot.documents.map { it.id } // Returns list of Pet IDs
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun removePetFromFavorites(userId: String, petId: String) {
        try {
            // 🎯 FIX: Targeting the subcollection used by the ViewModel
            db.collection("users")
                .document(userId)
                .collection("likedPets")
                .document(petId)
                .delete()
                .await()
            Log.d("FirestoreRepo", "✅ Pet $petId removed from likedPets subcollection")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to remove favorite: ${e.message}")
            throw e
        }
    }

    suspend fun removePetFromSwipedHistory(userId: String, petId: String) {
        try {
            db.collection("users")
                .document(userId)
                .collection("swipedPets") // Must match markAsSwiped exactly
                .document(petId)
                .delete()
                .await()
            Log.d("FirestoreRepo", "✅ Pet $petId removed from swipe history (Restored to deck)")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to restore pet: ${e.message}")
            throw e
        }
    }

    /** 🧹 AGENDA PART 3: Delete the chat channel associated with this match */
    suspend fun deleteChannel(adopterId: String, shelterId: String) {
        try {
            val channelId = "$adopterId-$shelterId"
            db.collection("channels")
                .document(channelId)
                .delete()
                .await()
            Log.d("FirestoreRepo", "✅ Channel $channelId deleted successfully")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Error deleting channel: ${e.message}")
            throw e
        }
    }

    suspend fun sendNotification(receiverId: String, title: String, message: String) {
        try {
            val notificationData = hashMapOf(
                "title" to title,
                "message" to message,
                "timestamp" to System.currentTimeMillis(),
                "isRead" to false,
                "type" to "match_update" // Helps the UI decide which icon to show
            )

            db.collection("users")
                .document(receiverId)
                .collection("notifications")
                .add(notificationData)
                .await()
            Log.d("Notification", "✅ Notification sent to $receiverId")
        } catch (e: Exception) {
            Log.e("NotificationError", "❌ Failed to send: ${e.message}")
        }
    }

    suspend fun removePetFromAdopterFavorites(adopterId: String, petName: String) {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            // 1. Remove from LikedPets
            val likedSnapshot = db.collection("users").document(adopterId)
                .collection("likedPets").whereEqualTo("name", petName).get().await()
            for (doc in likedSnapshot.documents) doc.reference.delete().await()

            // 2. Remove from SwipedPets (so it reappears in the Swipe Deck)
            val swipedSnapshot = db.collection("users").document(adopterId)
                .collection("swipedPets").whereEqualTo("petId", petName).get().await()
            for (doc in swipedSnapshot.documents) doc.reference.delete().await()

        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Cleanup failed: ${e.message}")
        }
    }

    suspend fun updateChannelShelterName(channelId: String, newName: String) {
        try {
            db.collection("channels")
                .document(channelId)
                .update("shelterName", newName) // This maps to your @PropertyName("shelterName")
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error updating shelter name: ${e.message}")
        }
    }

    suspend fun removePetNameFromChannel(channelId: String, petName: String) {
        try {
            db.collection("channels")
                .document(channelId)
                .update("petNames", com.google.firebase.firestore.FieldValue.arrayRemove(petName))
                .await()
            Log.d("FirestoreRepo", "✅ Removed $petName from channel $channelId")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to remove pet name: ${e.message}")
            throw e
        }
    }

    suspend fun updateUserTier(uid: String, newTier: String) {
        try {
            db.collection("users").document(uid)
                .update("tier", newTier) // 🎯 This MUST be "tier" to match your AuthViewModel
                .await()
            Log.d("FirestoreRepo", "✅ Tier updated to $newTier in cloud")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Tier update failed: ${e.message}")
        }
    }

    suspend fun addAdopterProfile(user: User) {
        try {
            // This saves the data to the specific "adopters" folder
            adoptersCollection.document(user.id).set(user).await()
            Log.d("FirestoreRepo", "✅ Adopter profile saved to adopters collection")
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "❌ Failed to save to adopters: ${e.message}")
            throw e
        }
    }

    //NOTIFICATION UPON SWIPING A PET
    suspend fun triggerMatchNotification(adopterName: String, shelterId: String, petName: String) {
        // 🔍 DIAGNOSTIC 1: Check if the Swipe Screen actually reached this function
        Log.d("NOTIF_TRACE", "➡️ Entering triggerMatchNotification")
        Log.d(
            "NOTIF_TRACE",
            "📦 Params - Adopter: $adopterName, ShelterID: $shelterId, Pet: $petName"
        )

        if (shelterId.isEmpty()) {
            Log.e("NOTIF_TRACE", "❌ ERROR: shelterId is empty! Cannot route notification.")
            return
        }

        try {
            sendNotification(
                receiverId = shelterId,
                title = "New Pet Match! 🐾",
                message = "$adopterName is interested in $petName!"
            )
            // 🔍 DIAGNOSTIC 2: Confirm the sendNotification call finished
            Log.d("NOTIF_TRACE", "✅ sendNotification dispatch completed")
        } catch (e: Exception) {
            Log.e("NOTIF_TRACE", "❌ EXCEPTION: ${e.message}")
        }
    }
}

