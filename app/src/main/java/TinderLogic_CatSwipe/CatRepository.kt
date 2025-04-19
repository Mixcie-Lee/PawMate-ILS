package TinderLogic_CatSwipe

import com.example.pawmate_ils.R

object CatRepository {
        fun getCats(): List<Cat> {
            return listOf(
                Cat("Alexa", "1 year", "Playful and agile", R.drawable.cat1),
                Cat("Yuri", "2 years", "Smart and loyal", R.drawable.cat2),
                Cat("Oggy", "6 months", "Independent and cuddly", R.drawable.cat3)
            )
        }
    }
