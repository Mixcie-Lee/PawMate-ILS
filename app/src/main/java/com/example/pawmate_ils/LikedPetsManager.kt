package com.example.pawmate_ils

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.flow.MutableStateFlow   // ✅ NEW: For reactive count
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import TinderLogic_PetSwipe.PetData

object LikedPetsManager {

    // ✅ NEW: StateFlow to observe the count in Compose easily
    private val _likedPetsCount = MutableStateFlow(0)
    val likedPetsCount: StateFlow<Int> get() = _likedPetsCount.asStateFlow()

    // Original list of liked pets (mutable for Compose reactivity)
    private val _likedPets = mutableStateListOf<PetData>()
    val likedPets: List<PetData> get() = _likedPets.toList() // ✅ Expose as immutable list

    // Add a pet if not already liked
    fun addLikedPet(pet: PetData) {
        if (!_likedPets.any { it.name == pet.name && it.type == pet.type }) {
            _likedPets.add(pet)
            _likedPetsCount.value = _likedPets.size   // ✅ Update reactive count
        }
    }

    // Remove a pet
    fun removeLikedPet(pet: PetData) {
        _likedPets.removeAll { it.name == pet.name && it.type == pet.type }
        _likedPetsCount.value = _likedPets.size       // ✅ Update reactive count
    }

    // Check if pet is liked
    fun isLiked(pet: PetData): Boolean {
        return _likedPets.any { it.name == pet.name && it.type == pet.type }
    }

    // Clear all liked pets
    fun clearAll() {
        _likedPets.clear()
        _likedPetsCount.value = 0                     // ✅ Reset count
    }

    // Get current count (non-reactive, for legacy usage)
    fun getLikedPetsCount(): Int {
        return _likedPets.size
    }
}
