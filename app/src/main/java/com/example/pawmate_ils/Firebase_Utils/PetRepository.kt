package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PetRepository : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // All pets state
    private val localPets = mutableStateListOf(
        PetData("Max", "Golden Retriever", "2 years", "Friendly and energetic", "dog", R.drawable.dog1, listOf(R.drawable.dogsub1, R.drawable.dogsub2), shelterId = "shelter1"),
        PetData("Charlie", "Labrador", "1 year", "Playful and loyal", "dog", R.drawable.shitzu, listOf(R.drawable.shitzusub1, R.drawable.shitzusub2), shelterId = "shelter2"),
        PetData("Rocky", "German Shepherd", "3 years", "Protective and smart", "dog", R.drawable.chow, listOf(R.drawable.chowsub1, R.drawable.chowsub2), shelterId = "shelter3"),
        PetData("Buddy", "Beagle", "2 years", "Curious and gentle", "dog", R.drawable.dog1, listOf(R.drawable.dogsub1, R.drawable.dogsub2), shelterId = "shelter4"),
        PetData("Cooper", "Border Collie", "1 year", "Intelligent and active", "dog", R.drawable.shitzu, listOf(R.drawable.shitzusub1, R.drawable.shitzusub2), shelterId = "shelter5"),
        PetData("Duke", "Bulldog", "4 years", "Calm and friendly", "dog", R.drawable.chow, listOf(R.drawable.chowsub1, R.drawable.chowsub2), shelterId = "shelter6"),
        PetData("Zeus", "Husky", "2 years", "Adventurous and strong", "dog", R.drawable.dog1, listOf(R.drawable.dogsub1, R.drawable.dogsub2), shelterId = "shelter7"),
        PetData("Bear", "Saint Bernard", "3 years", "Gentle giant", "dog", R.drawable.shitzu, listOf(R.drawable.shitzusub1, R.drawable.shitzusub2), shelterId = "shelter8"),
        PetData("Alexa", "Persian", "1 year", "Playful and agile", "cat", R.drawable.cat1, listOf(R.drawable.posaadd1, R.drawable.posaadd2), shelterId = "shelter9"),
        PetData("Yuri", "Garfield", "2 years", "Smart and loyal", "cat", R.drawable.cat2, listOf(R.drawable.posaaa1, R.drawable.posaaa2), shelterId = "shelter10"),
        PetData("Oggy", "Siberian", "6 months", "Independent and cuddly", "cat", R.drawable.cat3, listOf(R.drawable.posaaaa1, R.drawable.posaaaa2), shelterId = "shelter11")
    )
    //val allPets: List<PetData> get() = _allPets
    private val _allPets = MutableStateFlow<List<PetData>>(localPets.toList())
    val allPets: StateFlow<List<PetData>> = _allPets

    init {
        fetchPetsFromFirestore()
    }
    // 🐶 Fetch Firestore pets and merge with mock ones
    private fun fetchPetsFromFirestore() {
        // No coroutine required for addSnapshotListener, but we stick to viewModelScope for safety
        viewModelScope.launch {
            db.collection("pets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // keep only local mocks if Firestore snapshot fails
                        _allPets.value = localPets.toList()
                        return@addSnapshotListener
                    }

                    val firestorePets = snapshot?.documents
                        ?.mapNotNull { it.toObject(PetData::class.java) }
                        ?: emptyList()

                    // Combine: local (mock) first, then firestore pets.
                    // Use toList() so it's a plain immutable List<PetData>.
                    _allPets.value = localPets.toList() + firestorePets
                }
        }
    }
    fun addLocalPet(pet: PetData) {
        localPets.add(pet)
        _allPets.value = localPets.toList() + (_allPets.value.filter { it.shelterId !in localPets.map { p -> p.shelterId } })
    }

    fun appendBlankCard(pet: PetData) {
        // append to the end of the flow list (keeps local mocks at front)
        _allPets.value = _allPets.value + pet
    }






    /*
        // Add new pet (blank card)
        fun addPet(pet: PetData) {
            _allPets.add(pet)
        }

        // Update pet after validation
        fun approvePetImages(
            shelterId: String,
            petName: String,
            mainImage: Int,
            additionalImages: List<Int>
        ) {
            _allPets.replaceAll { pet ->
                if (pet.shelterId == shelterId && pet.name == petName) {
                    pet.copy(
                        imageRes = mainImage,
                        additionalImages = additionalImages,
                        validationStatus = true
                    )
                } else pet
            }
        }

     */
}
