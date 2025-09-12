package com.example.pawmate_ils.Firebase_Utils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await


object FirebaseUtils {
  private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    //Handle register sign in, login, logout

    suspend fun registerUser(email: String, password: String): String? {
        return try{
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.uid
        }catch(e: Exception){
            null
        }
    }
    suspend fun loginUser(email: String, password: String): String?{
        return try{
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.uid
        }catch(e: Exception){
            null
        }
    }

    fun getCurrentUser(): String?  = auth.currentUser?.uid

    fun logoutUser() = auth.signOut()

}


