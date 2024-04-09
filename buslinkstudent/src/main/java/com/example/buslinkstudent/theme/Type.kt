package com.example.buslinkstudent.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.buslinkstudent.R

// Set of Material typography styles to start with
val UberFontFamily = FontFamily(
    Font(R.font.ubermove, FontWeight.Medium)
)
val Typography = Typography(

    titleLarge = TextStyle(
        fontFamily = UberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = UberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = UberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = UberFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)