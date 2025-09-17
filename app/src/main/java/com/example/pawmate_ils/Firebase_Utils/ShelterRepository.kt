package com.example.pawmate_ils.Firebase_Utils

import com.example.pawmate_ils.firebase_models.ShelterProfile
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ShelterRepository {
    private val ShelRef = FirebaseFirestore.getInstance().collection("shelter")

    suspend fun getShelter(ShelterId: String): ShelterProfile? {
        return try {
            val snapshot = ShelRef.document(ShelterId).get().await()
            snapshot.toObject(ShelterProfile::class.java)
        } catch (e: Exception) { null }
    }

    suspend fun createUser(profile: ShelterProfile) {
        ShelRef.document(profile.id).set(profile).await()
    }
}