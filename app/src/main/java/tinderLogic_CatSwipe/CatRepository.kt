package TinderLogic_CatSwipe

import com.example.pawmate_ils.R

object CatRepository {
        fun getCats(): List<Cat> {
            return listOf(
                Cat(
                    "Alexa",
                    "1 year",
                    "Playful and agile",
                    "Persian",
                    R.drawable.cat1,
                    listOf(R.drawable.posaadd1, R.drawable.posaadd2)

                ),
                Cat("Yuri",
                    "2 years",
                    "Smart and loyal",
                    "Garfield",
                    R.drawable.cat2,
                    listOf(R.drawable.posaaa1, R.drawable.posaaa2)
                ),
                Cat(
                    "Oggy",
                    "6 months",
                    "Independent and cuddly",
                    "Siberian",
                    R.drawable.cat3,
                      listOf(R.drawable.posaaaa1,R.drawable.posaaaa2)


                )
            )
        }
    }
