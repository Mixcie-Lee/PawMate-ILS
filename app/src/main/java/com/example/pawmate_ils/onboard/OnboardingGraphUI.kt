package com.example.pawmate_ils.onboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingGraphUI(onboardingModel: OnboardingModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(50.dp)) // Top spacer

        Text(
            text = onboardingModel.title,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.size(16.dp))

        Text(
            text = onboardingModel.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun OnboardChoose(
    onAdopterSelected: () -> Unit,
    onShelterOwnerSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(70.dp))

        Text(
            text = "Are you a...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 0.dp),
            fontSize = 30.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.size(80.dp))

        // Row for Adopter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Adopter",
                modifier = Modifier
                    .padding(30.dp, 0.dp)
                    .weight(1f),
                fontSize = 33.sp,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium
            )

            // TODO: Define Adopter composable or replace with a Button
            // Adopter(text = "Adopter") {
            //     onAdopterSelected()
            // }
        }

        Spacer(modifier = Modifier.size(100.dp))

        // Row for Animal Shelter Owner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: Define AnimShel composable or replace with a Button
            // AnimShel(text = "Animal Shelter Owner") {
            //     onShelterOwnerSelected()
            // }
            Text(
                text = "Animal Shelter Owner",
                modifier = Modifier
                    .padding(10.dp, 10.dp)
                    .weight(1f),
                fontSize = 28.sp,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview1(){
    OnboardingGraphUI(OnboardingData.onboardingItems[0])
}
@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview2(){
    OnboardingGraphUI(OnboardingData.onboardingItems[1])
}
@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview3(){
    OnboardChoose(
        onAdopterSelected = { },
        onShelterOwnerSelected = {}
    )
}