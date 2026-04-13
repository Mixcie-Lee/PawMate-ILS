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
        ),
        OnboardingModel(
            title = "Terms and Conditions",
            description = "Please review Adoption Terms & User Responsibility below. By continuing, you also acknowledge our Terms of Service and Privacy Policy."
        )
    )

    /** Full legal copy shown on the last onboarding step (scrollable). */
    object AdoptionTerms {
        const val sectionTitle = "Adoption Terms & User Responsibility"

        const val section1Title = "1. User Responsibilities & Ethical Conduct"
        val section1Body = """
By utilizing the PawMate platform to initiate an adoption, the User agrees to provide a safe, permanent, and nurturing environment for the animal. In accordance with the Animal Welfare Act (RA 10631), the User is strictly prohibited from:

• Neglecting or abandoning the adopted pet.
• Reselling the pet for profit or commercial purposes.
• Using the pet for illegal activities or unauthorized breeding.
        """.trimIndent()

        const val section2Title = "2. Liability and Assumption of Risk"
        val section2Body = """
The User acknowledges that PawMate acts solely as a facilitator between shelters and adopters. PawMate ILS holds no liability for:

• The health, temperament, or future behavior of the adopted animal once the physical transfer is complete.
• Any damages, injuries, or legal disputes arising after the adoption agreement has been signed.
• The accuracy of the medical history provided by third-party shelters (though we strive for data integrity).
        """.trimIndent()

        const val section3Title = "3. Compliance with Local Ordinances & National Law"
        val section3Body = """
The User agrees to comply with all applicable Philippine laws, specifically the Anti-Rabies Act (RA 9482). This includes:

• Mandatory registration of the animal with local government units (LGU).
• Ensuring the pet receives annual anti-rabies vaccinations.
• Adherence to local barangay or city ordinances regarding leash laws and public pet safety.
        """.trimIndent()
    }
}
