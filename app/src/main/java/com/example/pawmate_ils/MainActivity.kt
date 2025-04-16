package com.example.pawmate_ils

import android.os.Bundle
import android.view.Surface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.pawmate_ils.onboard.OnboardingScreen
import com.example.pawmate_ils.onboard.UtilOnboarding
import com.example.pawmate_ils.ui.theme.PawMateILSTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val utilOnboarding by lazy { UtilOnboarding(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        installSplashScreen()

        val dogImages = listOf(
            R.drawable.dog1,
            R.drawable.dog2,
            R.drawable.dog3
        )

        setContent {
            val userRole = remember { mutableStateOf<String?>(null) }

            PawMateILSTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    when (userRole.value) {
                        "Adopter" -> {
                            // Navigate to Adopter login screen

                        }

                        "Shelter Owner" -> {
                            // Navigate to Shelter Owner login screen

                        }

                        else -> {
                            ShowOnboardingScreen(
                                utilOnboarding = utilOnboarding,
                                onComplete = { role ->
                                    utilOnboarding.setOnboardingCompleted()
                                    userRole.value = role
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ShowOnboardingScreen(
    utilOnboarding: UtilOnboarding,
    onComplete: (String) -> Unit
) {
    OnboardingScreen { selectedRole ->
        onComplete(selectedRole)
    }
}

