package com.example.pawmate_ils.Firebase_Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

class AuthRepository(
    private val  auth : FirebaseAuth = FirebaseAuth.getInstance()
){

        fun  getCurrentUserId(): String? = auth.currentUser?.uid


    suspend fun signInWithEmail(email: String, password: String): String? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.uid
        } catch (e: Exception) { null }
    }


    suspend fun signInWithGoogle(idToken: String): String? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.uid
        } catch (e: Exception) { null }
    }

    suspend fun signInWithApple(idToken: String, rawNonce: String): String? {
        val credential = OAuthProvider.newCredentialBuilder("apple.com")
            .setIdToken(idToken)
            .build()
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.uid
        } catch (e: Exception) { null }
    }



}

