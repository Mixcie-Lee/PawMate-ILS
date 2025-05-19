package com.example.pawmate_ils.onboard

data class OnboardingModel(
    val title: String,
    val description: String
)

object OnboardingData {
    val onboardingItems = listOf(
        OnboardingModel(
            title = "Welcome to PawMate",
            description = "PawMate is your trusted companion in the journey of pet adoption and animal care. Whether you are looking to welcome a new furry friend into your home or support your local animal shelter, our platform is designed to make the process seamless, transparent, and joyful."
        ),
        OnboardingModel(
            title = "For Animal Shelters & Rescues",
            description = "Easily showcase your adoptable pets, manage inquiries, and connect with a community of passionate adopters. PawMate empowers shelters and rescues to reach more people, streamline operations, and ensure every animal finds a loving home."
        ),
        OnboardingModel(
            title = "For Pet Adopters & Families",
            description = "Discover a wide variety of pets waiting for a forever home. Browse detailed profiles, learn about each animal's story, and connect directly with shelters. PawMate is here to help you find the perfect companion and make adoption a rewarding experience."
        )
    )
}
