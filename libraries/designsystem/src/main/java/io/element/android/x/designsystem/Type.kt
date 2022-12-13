package io.element.android.x.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.airbnb.android.showkase.annotation.ShowkaseTypography

@ShowkaseTypography(name = "Body Large", group = "Element")
val bodyLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

@ShowkaseTypography(name = "Headline Small", group = "Element")
val headlineSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = bodyLarge,
    headlineSmall = headlineSmall,
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
     */
)

object ElementTextStyles {

    object Bold {
        val largeTitle = TextStyle(
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            lineHeight = 41.sp,
            textAlign = TextAlign.Center
        )

        val title1 = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center
        )

        val title2 = TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center
        )

        val title3 = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center
        )

        val headline = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val body = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val callout = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        val subheadline = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )

        val footnote = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        val caption1 = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )

        val caption2 = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center
        )
    }

    object Regular {
        val largeTitle = TextStyle(
            fontSize = 34.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 41.sp,
            textAlign = TextAlign.Center
        )

        val title1 = TextStyle(
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 34.sp,
            textAlign = TextAlign.Center
        )

        val title2 = TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center
        )

        val title3 = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center
        )

        val headline = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val body = TextStyle(
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        val callout = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )

        val subheadline = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )

        val footnote = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )

        val caption1 = TextStyle(
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )

        val caption2 = TextStyle(
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
