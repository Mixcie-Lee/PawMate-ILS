package TinderLogic_PetSwipe

import com.example.pawmate_ils.R

object PetRepository {
    fun getPets(): List<Pet> {
        return listOf(
            Pet("Max", "1 year", "Playful and energetic", R.drawable.dog1),
            Pet("Alex", "2 years", "Calm and loyal", R.drawable.dog2),
            Pet("Bella", "6 months", "Curious and cuddly", R.drawable.dog3)
        )
    }
}