package com.example.pawmate_ils.Firebase_Utils

import TinderLogic_PetSwipe.PetData
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawmate_ils.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PetsRepository : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val localPets = mutableStateListOf(
        PetData(
            petId = "MaxID",
            name = "Max",
            breed = "Golden Retriever",
            age = "2 years",
            description = "Friendly and energetic",
            type = "dog",
            imageRes = R.drawable.dog1,
            additionalImages = emptyList(),
            shelterId = "shelter1"
        ),
        PetData(
            petId = "CHARLIEID",
            name = "Charlie",
            breed = "Labrador",
            age = "1 year",
            description = "Playful and loyal",
            type = "dog",
            imageRes = R.drawable.shitzu,
            additionalImages = emptyList(),
            shelterId = "shelter2"
        ),
        PetData(
            petId = "ROCKYID",
            name = "Rocky",
            breed = "German Shepherd",
            age = "3 years",
            description = "Protective and smart",
            type = "dog",
            imageRes = R.drawable.chow,
            additionalImages = emptyList(),
            shelterId = "shelter3"
        ),
        PetData(
            petId = "BUDDYID",
            name = "Buddy",
            breed = "Beagle",
            age = "2 years",
            description = "Curious and gentle",
            type = "dog",
            imageRes = R.drawable.dog1,
            additionalImages = emptyList(),
            shelterId = "shelter4"
        ),
        PetData(
            petId = "COOPERID",
            name = "Cooper",
            breed = "Border Collie",
            age = "1 year",
            description = "Intelligent and active",
            type = "dog",
            imageRes = R.drawable.shitzu,
            additionalImages = emptyList(),
            shelterId = "shelter5"
        ),
        PetData(
            petId = "DUKEID",
            name = "Duke",
            breed = "Bulldog",
            age = "4 years",
            description = "Calm and friendly",
            type = "dog",
            imageRes = R.drawable.chow,
            additionalImages = emptyList(),
            shelterId = "shelter6"
        ),
        PetData(
            petId = "ZEUSID",
            name = "Zeus",
            breed = "Husky",
            age = "2 years",
            description = "Adventurous and strong",
            type = "dog",
            imageRes = R.drawable.dog1,
            additionalImages = emptyList(),
            shelterId = "shelter7"
        ),
        PetData(
            petId = "BEARID",
            name = "Bear",
            breed = "Saint Bernard",
            age = "3 years",
            description = "Gentle giant",
            type = "dog",
            imageRes = R.drawable.shitzu,
            additionalImages = emptyList(),
            shelterId = "shelter8"
        ),
        PetData(
            petId = "ALEXAID",
            name = "Alexa",
            breed = "Persian",
            age = "1 year",
            description = "Playful and agile",
            type = "cat",
            imageRes = R.drawable.cat1,
            additionalImages = emptyList(),
            shelterId = "shelter9"
        ),
        PetData(
            petId = "YURIID",
            name = "Yuri",
            breed = "Garfield",
            age = "2 years",
            description = "Smart and loyal",
            type = "cat",
            imageRes = R.drawable.cat2,
            additionalImages = emptyList(),
            shelterId = "shelter10"
        ),
        PetData(
            petId = "OGGYID",
            name = "Oggy",
            breed = "Siberian",
            age = "6 months",
            description = "Independent and cuddly",
            type = "cat",
            imageRes = R.drawable.cat3,
            additionalImages = emptyList(),
            shelterId = "shelter11"
        )
    )

    private val _allPets = MutableStateFlow<List<PetData>>(localPets.toList())
    val allPets: StateFlow<List<PetData>> = _allPets

    init {
        fetchPetsFromFirestore()
    }

    private fun fetchPetsFromFirestore() {
        viewModelScope.launch {
            db.collection("pets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FIRESTORE", "Error fetching: ${error.message}")
                        return@addSnapshotListener
                    }

                    val firestorePets = snapshot?.documents?.mapNotNull { doc ->
                        // 🎯 Manually attach the Firestore Document ID to the PetData object
                        doc.toObject(PetData::class.java)?.copy(petId = doc.id)
                    } ?: emptyList()

                    // 🎯 Combine them but ensure we don't have duplicates if a local pet was also saved to Firestore
                    val combined = (localPets + firestorePets).distinctBy { it.petId ?: it.name }
                    _allPets.value = combined
                }
        }
    }

    fun addLocalPet(pet: PetData) {
        localPets.add(pet)
        _allPets.value =
            localPets.toList() + (_allPets.value.filter { it.shelterId !in localPets.map { p -> p.shelterId } })
    }

    fun appendBlankCard(pet: PetData) {
        _allPets.value = _allPets.value + pet
    }
    fun getPetsByShelter(targetShelterId: String): List<PetData> {
        // 🎯 Filter both local and Firestore pets to only show the ones owned by this shelter
        return _allPets.value.filter { it.shelterId == targetShelterId }
    }
}