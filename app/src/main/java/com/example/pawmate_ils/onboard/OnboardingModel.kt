package com.example.pawmate_ils.onboard


import androidx.annotation.DrawableRes

import com.example.pawmate_ils.R

open class OnboardingModel

    (@DrawableRes val image: Int, val title: String, val description: String,)

{
    data object FirstPage: OnboardingModel(
        image = R.drawable.kimmy,
        title = "Ok ok oko ok ",
        description = "YESYEYSYEYSYSYYEYSYSYSYS"

    )
    data object SecondPage: OnboardingModel(
        image = R.drawable.kimmy,
        title = "Ok ok oko ok ",
        description = "YESYEYSYEYSYSYYEYSYSYSYS"
    )
    data object ThirdPage: OnboardingModel(
        image = R.drawable.kimmy,
        title = "Ok ok oko ok ",
        description = "YESYEYSYEYSYSYYEYSYSYSYS"
    )
}
