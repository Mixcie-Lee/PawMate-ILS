package com.example.pawmate_ils.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.pawmate_ils.R

val NunitoFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

/** Tuning line breaks + disabling automatic hyphenation (avoids awkward splits on narrow screens). */
private val DefaultHyphens = Hyphens.None
private val TitleLineBreak = LineBreak.Heading
private val BodyLineBreak = LineBreak.Paragraph

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    displayMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    displaySmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    headlineLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    headlineMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    headlineSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    titleMedium = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    titleSmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        hyphens = DefaultHyphens,
        lineBreak = TitleLineBreak
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        hyphens = DefaultHyphens,
        lineBreak = BodyLineBreak
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        hyphens = DefaultHyphens,
        lineBreak = BodyLineBreak
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        hyphens = DefaultHyphens,
        lineBreak = BodyLineBreak
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        hyphens = DefaultHyphens,
        lineBreak = BodyLineBreak
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        hyphens = DefaultHyphens,
        lineBreak = BodyLineBreak
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        hyphens = DefaultHyphens,
        lineBreak = BodyLineBreak
    )
)
