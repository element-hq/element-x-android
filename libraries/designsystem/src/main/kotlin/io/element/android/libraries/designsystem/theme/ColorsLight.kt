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

import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.Azure
import io.element.android.libraries.designsystem.ElementGreen
import io.element.android.libraries.designsystem.Gray_100
import io.element.android.libraries.designsystem.Gray_50
import io.element.android.libraries.designsystem.SystemGrey5Light
import io.element.android.libraries.designsystem.SystemGrey6Light
import io.element.android.libraries.designsystem.theme.compound.LightDesignTokens
import io.element.android.libraries.designsystem.theme.previews.ColorsSchemePreview

fun elementColorsLight() = ElementColors(
    messageFromMeBackground = SystemGrey5Light,
    messageFromOtherBackground = SystemGrey6Light,
    messageHighlightedBackground = Azure,
    quaternary = Gray_100,
    quinary = Gray_50,
    gray300 = LightDesignTokens.colorGray300,
    accentColor = ElementGreen,
    placeholder = LightDesignTokens.colorGray800,
    isLight = true,
)

val materialColorSchemeLight = lightColorScheme(
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
    surfaceVariant = LightDesignTokens.colorGray400,
    onSurfaceVariant = LightDesignTokens.colorGray1400,
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

@Preview
@Composable
fun ColorsSchemePreviewLight() = ColorsSchemePreview(
    Color.Black,
    Color.White,
    materialColorSchemeLight,
)
