package com.example.pawmate_ils.Firebase_Utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AdoptionCenterViewMdelFactory(
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory{
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AdoptionCenterViewModel::class.java)) {
            AdoptionCenterViewModel(authViewModel) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}