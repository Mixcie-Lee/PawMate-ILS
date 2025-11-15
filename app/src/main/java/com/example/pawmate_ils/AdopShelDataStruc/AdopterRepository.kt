package com.example.pawmate_ils.AdopShelDataStruc

import com.example.pawmate_ils.firebase_models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AdopterRepository{
    private val db = FirebaseFirestore.getInstance()
    private  val adopterColl =  db.collection("adopters")

    suspend fun  addAdopter(adopter: User){
        adopterColl.document(adopter.id).set(adopter).await()
    }
    suspend fun getAllAdopter(): List<User>{
        val snapshot = adopterColl.get().await()
        return snapshot.toObjects(User::class.java)
    }
    suspend fun updateAdopter(adopter: User){
        adopterColl.document(adopter.id).set(adopter).await()
    }

    suspend fun deleteAdopter(id: String){
        adopterColl.document(id).delete().await()
    }

}
