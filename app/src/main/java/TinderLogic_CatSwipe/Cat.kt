package TinderLogic_CatSwipe

import TinderLogic_PetSwipe.Pet

class Cat(
        name: String,
        age: String,
        description: String,
        breed : String,
        imageRes: Int,
        subImages: List<Int>
    ) : Pet(name, age, description, breed, imageRes, subImages)
