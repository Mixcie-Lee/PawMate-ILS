package com.example.pawmate_ils.onboard

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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

        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = null,
            modifier = Modifier
                .size(500.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = onboardingModel.title,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier
                .size(10.dp)
        )
        Text(
            text = onboardingModel.description,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 0.dp),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(
            modifier = Modifier
                .size(20.dp))
    }
}

@Composable
fun OnboardChoose(
    onboardingModel: OnboardingModel,
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

            Adopter(text = "Adopter") {
                onAdopterSelected()
            }
        }

        Spacer(modifier = Modifier.size(100.dp))

        // Row for Animal Shelter Owner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimShel(text = "Animal Shelter Owner") {
                onShelterOwnerSelected()
            }
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
    OnboardingGraphUI(OnboardingModel.FirstPage)
}
@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview2(){
    OnboardingGraphUI(OnboardingModel.SecondPage)
}
@Preview(showBackground = true)
@Composable
fun OnboardingGraphUIPreview3(){
    OnboardChoose(
        onboardingModel = OnboardingModel.ThirdPage,
        onAdopterSelected = { },
        onShelterOwnerSelected = {}
    )
}