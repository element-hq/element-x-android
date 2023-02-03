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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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

@Composable
fun ElementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lightColors: ElementColors = elementColorsLight(),
    darkColors: ElementColors = elementColorsDark(),
    materialLightColors: ColorScheme = materialColorSchemeLight,
    materialDarkColors: ColorScheme = materialColorSchemeDark,
    content: @Composable () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !darkTheme
    val currentColor = remember { if (darkTheme) darkColors else lightColors }
    val rememberedColors = remember { currentColor.copy() }.apply { updateColorsFrom(currentColor) }
    CompositionLocalProvider(
        LocalColors provides rememberedColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) materialDarkColors else materialLightColors
        ) {
            val bgColor = MaterialTheme.colorScheme.background
            SideEffect {
                systemUiController.setStatusBarColor(
                    color = bgColor
                )
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }

            content()
        }
    }
}
