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

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.Azure
import io.element.android.libraries.designsystem.ElementGreen
import io.element.android.libraries.designsystem.Gray_400
import io.element.android.libraries.designsystem.Gray_450
import io.element.android.libraries.designsystem.SystemGrey5Dark
import io.element.android.libraries.designsystem.SystemGrey6Dark
import io.element.android.libraries.designsystem.theme.compound.DarkDesignTokens
import io.element.android.libraries.designsystem.theme.previews.ColorsSchemePreview

fun elementColorsDark() = ElementColors(
    messageFromMeBackground = SystemGrey5Dark,
    messageFromOtherBackground = SystemGrey6Dark,
    messageHighlightedBackground = Azure,
    quaternary = Gray_400,
    quinary = Gray_450,
    gray300 = DarkDesignTokens.colorGray300,
    accentColor = ElementGreen,
    placeholder = DarkDesignTokens.colorGray800,
    isLight = false,
)

val materialColorSchemeDark = darkColorScheme(
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
    surfaceVariant = DarkDesignTokens.colorGray400,
    onSurfaceVariant = DarkDesignTokens.colorGray1400,
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
fun ColorsSchemePreviewDark() = ColorsSchemePreview(
    Color.White,
    Color.Black,
    materialColorSchemeDark,
)
