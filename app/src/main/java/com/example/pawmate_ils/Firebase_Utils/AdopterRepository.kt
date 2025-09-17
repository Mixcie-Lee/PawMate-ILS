package com.example.pawmate_ils.Firebase_Utils
import com.example.pawmate_ils.firebase_models.AdopterProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdopterRepository {
    private val adoptersRef = FirebaseFirestore.getInstance().collection("adopters")

    suspend fun getAdopter(adopterId: String): AdopterProfile? {
        return try {
            val snapshot = adoptersRef.document(adopterId).get().await()
            snapshot.toObject(AdopterProfile::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun createUser(profile: AdopterProfile) {
        adoptersRef.document(profile.id).set(profile).await()
    }
}
