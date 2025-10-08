package com.example.pawmate_ils.Firebase_Utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class Auth2ViewModel : ViewModel() {

    private val auth2: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<Auth2State>()
    val authState: LiveData<Auth2State> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (auth2.currentUser != null) {
            _authState.value = Auth2State.Authenticated
        } else {
            _authState.value = Auth2State.Unauthenticated
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = Auth2State.Loading
        auth2.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = Auth2State.Authenticated
                } else {
                    _authState.value = Auth2State.Error(task.exception?.message ?: "Sign-up failed")
                }
            }
    }
    //Gooogle sign in that works also the sign up if the user is new :)
    fun signUpWithGoogle(idToken: String, onResult: (Boolean) -> Unit){ //HANDLES GOOGLE SIGN-UP AND SIGN IN/LOGIN
        _authState.value = Auth2State.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth2.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value = Auth2State.Authenticated
                    onResult(true)
                }else{
                    _authState.value = Auth2State.Error(task.exception?.message ?: "Sign-up failed")
                    onResult(false)
                }
            }
    }




    fun signIn(email: String, password: String) {
        _authState.value = Auth2State.Loading
        auth2.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = Auth2State.Authenticated
                } else {
                    _authState.value = Auth2State.Error(task.exception?.message ?: "Sign-in failed")
                }
            }
    }

    fun signOut() {
        auth2.signOut()
        _authState.value = Auth2State.Unauthenticated
    }
}

sealed class Auth2State {
    object Authenticated : Auth2State()
    object Unauthenticated : Auth2State()
    object Loading : Auth2State()
    data class Error(val message: String) : Auth2State()
}
