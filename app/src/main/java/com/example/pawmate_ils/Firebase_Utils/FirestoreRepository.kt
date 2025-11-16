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

    // --- NEW ---
    fun updateEmail(
        newEmail: String,
        currentPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (user != null && email != null) {
            // Re-authenticate first
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // Update email in Firebase Auth
                    user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            // Update email in Firestore
                            FirebaseFirestore.getInstance().collection("users")
                                .document(user.uid)
                                .update("email", newEmail)
                                .addOnSuccessListener {
                                    onResult(true, "Email updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    onResult(
                                        false,
                                        "Email updated in Auth but Firestore failed: ${e.message}"
                                    )
                                }
                        } else {
                            onResult(false, updateTask.exception?.message)
                        }
                    }
                } else {
                    onResult(false, "Reauthentication failed: ${authTask.exception?.message}")
                }
            }
        } else {
            onResult(false, "No user logged in.")
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

}

