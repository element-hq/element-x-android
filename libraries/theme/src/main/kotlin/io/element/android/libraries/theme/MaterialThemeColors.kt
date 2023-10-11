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

package io.element.android.libraries.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.theme.compound.generated.internal.DarkDesignTokens
import io.element.android.libraries.theme.compound.generated.internal.LightDesignTokens
import io.element.android.libraries.theme.previews.ColorsSchemePreview

internal val materialColorSchemeLight = lightColorScheme(
    primary = LightDesignTokens.colorGray1400,
    onPrimary = LightDesignTokens.colorThemeBg,
    primaryContainer = LightDesignTokens.colorThemeBg,
    onPrimaryContainer = LightDesignTokens.colorGray1400,
    inversePrimary = LightDesignTokens.colorThemeBg,
    secondary = LightDesignTokens.colorGray900,
    onSecondary = LightDesignTokens.colorThemeBg,
    secondaryContainer = LightDesignTokens.colorGray400,
    onSecondaryContainer = LightDesignTokens.colorGray1400,
    tertiary = LightDesignTokens.colorGray900,
    onTertiary = LightDesignTokens.colorThemeBg,
    tertiaryContainer = LightDesignTokens.colorGray1400,
    onTertiaryContainer = LightDesignTokens.colorThemeBg,
    background = LightDesignTokens.colorThemeBg,
    onBackground = LightDesignTokens.colorGray1400,
    surface = LightDesignTokens.colorThemeBg,
    onSurface = LightDesignTokens.colorGray1400,
    surfaceVariant = LightDesignTokens.colorGray300,
    onSurfaceVariant = LightDesignTokens.colorGray900,
    surfaceTint = LightDesignTokens.colorGray1000,
    inverseSurface = LightDesignTokens.colorGray1300,
    inverseOnSurface = LightDesignTokens.colorThemeBg,
    error = LightDesignTokens.colorRed900,
    onError = LightDesignTokens.colorThemeBg,
    errorContainer = LightDesignTokens.colorRed400,
    onErrorContainer = LightDesignTokens.colorRed900,
    outline = LightDesignTokens.colorGray800,
    outlineVariant = LightDesignTokens.colorAlphaGray400,
    scrim = LightDesignTokens.colorGray1400,
)

internal val materialColorSchemeDark = darkColorScheme(
    primary = DarkDesignTokens.colorGray1400,
    onPrimary = DarkDesignTokens.colorThemeBg,
    primaryContainer = DarkDesignTokens.colorThemeBg,
    onPrimaryContainer = DarkDesignTokens.colorGray1400,
    inversePrimary = DarkDesignTokens.colorThemeBg,
    secondary = DarkDesignTokens.colorGray900,
    onSecondary = DarkDesignTokens.colorThemeBg,
    secondaryContainer = DarkDesignTokens.colorGray400,
    onSecondaryContainer = DarkDesignTokens.colorGray1400,
    tertiary = DarkDesignTokens.colorGray900,
    onTertiary = DarkDesignTokens.colorThemeBg,
    tertiaryContainer = DarkDesignTokens.colorGray1400,
    onTertiaryContainer = DarkDesignTokens.colorThemeBg,
    background = DarkDesignTokens.colorThemeBg,
    onBackground = DarkDesignTokens.colorGray1400,
    surface = DarkDesignTokens.colorThemeBg,
    onSurface = DarkDesignTokens.colorGray1400,
    surfaceVariant = DarkDesignTokens.colorGray300,
    onSurfaceVariant = DarkDesignTokens.colorGray900,
    surfaceTint = DarkDesignTokens.colorGray1000,
    inverseSurface = DarkDesignTokens.colorGray1300,
    inverseOnSurface = DarkDesignTokens.colorThemeBg,
    error = DarkDesignTokens.colorRed900,
    onError = DarkDesignTokens.colorThemeBg,
    errorContainer = DarkDesignTokens.colorRed400,
    onErrorContainer = DarkDesignTokens.colorRed900,
    outline = DarkDesignTokens.colorGray800,
    outlineVariant = DarkDesignTokens.colorAlphaGray400,
    scrim = DarkDesignTokens.colorGray300,
)

@Preview
@Composable
internal fun ColorsSchemeLightPreview() = ColorsSchemePreview(
    Color.Black,
    Color.White,
    materialColorSchemeLight,
)

@Preview
@Composable
internal fun ColorsSchemeDarkPreview() = ColorsSchemePreview(
    Color.White,
    Color.Black,
    materialColorSchemeDark,
)
