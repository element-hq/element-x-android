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
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.element.android.libraries.designsystem.theme.compound.CompoundColorPalette
import io.element.android.libraries.designsystem.theme.compound.CompoundColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
    dynamicColor: Boolean = false, /* true to enable MaterialYou */
    lightColors: ElementColors = elementColorsLight(),
    darkColors: ElementColors = elementColorsDark(),
    materialLightColors: ColorScheme = materialColorSchemeLight,
    materialDarkColors: ColorScheme = materialColorSchemeDark,
    content: @Composable () -> Unit,
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !darkTheme
    val currentColor = remember { if (darkTheme) darkColors else lightColors }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> materialDarkColors
        else -> materialLightColors
    }
    var compoundColorPalette: CompoundColorPalette by remember { mutableStateOf(CompoundColorPalette.Light) }
    compoundColorPalette = if (darkTheme) CompoundColorPalette.Dark else CompoundColorPalette.Light
    SideEffect {
        systemUiController.setStatusBarColor(
            color = colorScheme.background
        )
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
    val rememberedColors = remember { currentColor.copy() }.apply { updateColorsFrom(currentColor) }
    CompositionLocalProvider(
        LocalColors provides rememberedColors,
        CompoundColors provides compoundColorPalette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            // TODO typography =
            content = content
        )
    }
}
