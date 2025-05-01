package com.example.pawmate_ils.onboard


import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChooseUI(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = Color.Black,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    fontSize : Int = 14,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
){


    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ), shape = RoundedCornerShape(10.dp))
    {

        Text( text = text, fontSize =  fontSize.sp, style = textStyle)
    }
}



@Composable
fun Adopter(text: String, function: () -> Unit) {

    ChooseUI (text = "Adopter", modifier = Modifier.width(100.dp).height(100.dp),
        backgroundColor = Color(0xFFF2CAF3)){

    }
}

@Composable
fun AnimShel(text: String, function: () -> Unit) {

    ChooseUI (
        text = "Animal Shelter", modifier = Modifier.width(100.dp).height(100.dp),
        backgroundColor = Color(0xFFF2CAF3)

    ){  }


}


