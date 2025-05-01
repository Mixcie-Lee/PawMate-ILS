package TinderLogic_PetSwipe

import com.example.pawmate_ils.R

object PetRepository {
    fun getPets(): List<Pet> {
        return listOf(
            Pet("Max",
                "1 year",
                "Playful and energetic",
                "Golden Retriever",
                R.drawable.dog1,
                listOf(R.drawable.dogsub1,R.drawable.dogsub2)
            ),

            Pet("Alex",
                "2 years",
                "Calm and loyal",
                "Maltese",
                R.drawable.shitzu,
                listOf(R.drawable.shitzusub1,R.drawable.shitzusub2)
            ),
            Pet("Bella",
                "6 months",
                "Curious and cuddly",
                "Aspin",
                R.drawable.chow,
                listOf(R.drawable.chowsub1, R.drawable.chowsub2)
            )
        )
    }
}