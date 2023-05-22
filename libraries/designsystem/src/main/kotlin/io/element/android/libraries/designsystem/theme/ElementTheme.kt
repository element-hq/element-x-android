/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.element.android.libraries.designsystem.theme.compound.CompoundColors
import io.element.android.libraries.designsystem.theme.compound.compoundColorsDark
import io.element.android.libraries.designsystem.theme.compound.compoundColorsLight

/**
 * Inspired from https://medium.com/@lucasyujideveloper/54cbcbde1ace
 */
object ElementTheme {
    val colors: ElementColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current
}

/* Global variables (application level) */
val LocalColors = staticCompositionLocalOf { elementColorsLight() }
val LocalCompoundColors = staticCompositionLocalOf { compoundColorsLight() }

@Composable
fun ElementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, /* true to enable MaterialYou */
    colors: ElementColors = if (darkTheme) elementColorsDark() else elementColorsLight(),
    compoundColors: CompoundColors = if (darkTheme) compoundColorsDark() else compoundColorsLight(),
    materialLightColors: ColorScheme = materialColorSchemeLight,
    materialDarkColors: ColorScheme = materialColorSchemeDark,
    content: @Composable () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    val currentColor = remember(darkTheme) {
        colors.copy()
    }.apply { updateColorsFrom(colors) }
    val currentCompoundColor = remember(darkTheme) {
        compoundColors.copy()
    }.apply { updateColorsFrom(compoundColors) }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> materialDarkColors
        else -> materialLightColors
    }
    SideEffect {
        systemUiController.applyTheme(colorScheme = colorScheme, darkTheme = darkTheme)
    }
    CompositionLocalProvider(
        LocalColors provides currentColor,
        LocalCompoundColors provides compoundColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            // TODO typography =
            content = content
        )
    }
}

/**
 * Can be used to force a composable in dark theme.
 * It will automatically change the system ui colors back to normal when leaving the composition.
 */
@Composable
fun ForcedDarkElementTheme(
    content: @Composable () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    val colorScheme = MaterialTheme.colorScheme
    val wasDarkTheme = !ElementTheme.colors.isLight
    DisposableEffect(Unit) {
        onDispose {
            systemUiController.applyTheme(colorScheme, wasDarkTheme)
        }
    }
    ElementTheme(darkTheme = true, content = content)
}

private fun SystemUiController.applyTheme(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
) {
    val useDarkIcons = !darkTheme
    setStatusBarColor(
        color = colorScheme.background
    )
    setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = useDarkIcons
    )
}
