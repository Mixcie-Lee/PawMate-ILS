package com.example.pawmate_ils.AdopShelDataStruc

import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ShelterRepository{
    private val db = FirebaseFirestore.getInstance()
    private  val shelterColl =  db.collection("shelters")

    suspend fun  addShelter(shelter: User){
        shelterColl.document(shelter.id).set(shelter).await()
    }
    suspend fun getAllShelter(): List<User>{
        val snapshot = shelterColl.get().await()
        return snapshot.toObjects(User::class.java)
    }
    suspend fun updateShelter(shelter: User){
        shelterColl.document(shelter.id).set(shelter).await()
    }

    suspend fun deleteShelter(id: String){
        shelterColl.document(id).delete().await()
    }
    suspend fun getShelterNameById(shelterId: String): String? {
        return try {
            val snapshot = shelterColl.document(shelterId).get().await()
            snapshot.getString("name") // Assuming the User model has a "name" field
        } catch (e: Exception) {
            null
        }
    }
}