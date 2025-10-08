package com.example.pawmate_ils

import androidx.compose.runtime.mutableStateListOf
import TinderLogic_PetSwipe.PetData

object LikedPetsManager {
    private val _likedPets = mutableStateListOf<PetData>()
    val likedPets: List<PetData> get() = _likedPets.toList()
    
    fun addLikedPet(pet: PetData) {
        if (!_likedPets.any { it.name == pet.name && it.type == pet.type }) {
            _likedPets.add(pet)
        }
    }
    
    fun removeLikedPet(pet: PetData) {
        _likedPets.removeAll { it.name == pet.name && it.type == pet.type }
    }
    
    fun isLiked(pet: PetData): Boolean {
        return _likedPets.any { it.name == pet.name && it.type == pet.type }
    }
    
    fun clearAll() {
        _likedPets.clear()
    }
    
    fun getLikedPetsCount(): Int {
        return _likedPets.size
    }
}

