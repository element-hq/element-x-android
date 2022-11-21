package io.element.android.x.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = DarkGrey,
    tertiary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    surfaceVariant = SystemGrey6Dark,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = LightGrey,
    tertiary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    surfaceVariant = SystemGrey6Light,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


@Composable
fun ElementXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = colorScheme.background
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}