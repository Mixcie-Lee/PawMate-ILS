package com.example.pawmate_ils.Firebase_Utils

import android.net.Uri
import android.util.Log
import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.example.pawmate_ils.Firebase_Utils.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.storage
import kotlinx.coroutines.delay
import java.util.UUID

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")


    suspend fun getUserRole(uid: String): com.google.firebase.firestore.DocumentSnapshot  {
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
    suspend fun uploadProfilePhoto(uid: String, imageUri: Uri): String? {
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



    fun uploadPetImages(uris: List<Uri>, onComplete: (List<String>) -> Unit) {
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


    //HANDLES USERNAME, SO THE NAME WOULD NOT DISAPPEAR WHEN THE APP IS CLOSED. SAME LOGIC AS PROFILE PHOTO HANDLER :)
    suspend fun updateDisplayName(uid: String, displayName: String) {
        usersCollection.document(uid).update("name", displayName).await()
    }

    object FirestoreManager {
        val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    }







}

