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
import io.element.android.libraries.designsystem.Compound_Gray_1400_Light
import io.element.android.libraries.designsystem.Compound_Gray_300_Light
import io.element.android.libraries.designsystem.Compound_Gray_400_Light
import io.element.android.libraries.designsystem.Compound_Gray_800_Light
import io.element.android.libraries.designsystem.Gray_100
import io.element.android.libraries.designsystem.Gray_50
import io.element.android.libraries.designsystem.SystemGrey5Light
import io.element.android.libraries.designsystem.SystemGrey6Light
import io.element.android.libraries.designsystem.TextColorCriticalLight
import io.element.android.libraries.designsystem.theme.compound.compoundColorsLight
import io.element.android.libraries.designsystem.theme.previews.ColorsSchemePreview

fun elementColorsLight() = ElementColors(
    messageFromMeBackground = SystemGrey5Light,
    messageFromOtherBackground = SystemGrey6Light,
    messageHighlightedBackground = Azure,
    quaternary = Gray_100,
    quinary = Gray_50,
    gray300 = Compound_Gray_300_Light,
    gray400 = Compound_Gray_400_Light,
    gray1400 = Compound_Gray_1400_Light,
    textActionCritical = TextColorCriticalLight,
    accentColor = Color(0xFF0DBD8B),
    placeholder = Compound_Gray_800_Light,
    isLight = true,
)

// TODO Lots of colors are missing
val materialColorSchemeLight = lightColorScheme(
    primary = compoundColorsLight.colorIconPrimary,
    onPrimary = compoundColorsLight.colorIconOnSolidPrimary,
    primaryContainer = compoundColorsLight.colorBgSubtlePrimary,
    onPrimaryContainer = compoundColorsLight.colorTextActionAccent,
    // TODO inversePrimary = ColorLightTokens.InversePrimary,
    secondary = compoundColorsLight.colorIconSecondary,
    onSecondary = compoundColorsLight.colorIconOnSolidPrimary,
    // TODO secondaryContainer = ColorLightTokens.SecondaryContainer,
    // TODO onSecondaryContainer = ColorLightTokens.OnSecondaryContainer,
    tertiary = compoundColorsLight.colorIconTertiary,
    onTertiary = compoundColorsLight.colorIconOnSolidPrimary,
    // TODO tertiaryContainer = ColorLightTokens.TertiaryContainer,
    // TODO onTertiaryContainer = ColorLightTokens.OnTertiaryContainer,
    background = compoundColorsLight.colorBgCanvasDefault,
    onBackground = compoundColorsLight.colorIconPrimary,
    surface = compoundColorsLight.colorBgCanvasDefault,
    onSurface = compoundColorsLight.colorIconPrimary,
    surfaceVariant = compoundColorsLight.colorBgSubtlePrimary,
    onSurfaceVariant = compoundColorsLight.colorIconTertiary,
    surfaceTint = compoundColorsLight.colorIconPrimary,
    // TODO inverseSurface = ColorLightTokens.InverseSurface,
    // TODO inverseOnSurface = ColorLightTokens.InverseOnSurface,
    error = compoundColorsLight.colorIconCriticalPrimary,
    onError = compoundColorsLight.colorTextOnSolidPrimary,
    errorContainer = compoundColorsLight.colorBgCriticalSubtle,
    // TODO onErrorContainer = ColorLightTokens.OnErrorContainer,
    outline = compoundColorsLight.colorBorderInteractivePrimary,
    outlineVariant = compoundColorsLight.colorBorderInteractiveSecondary,
    // TODO scrim = ColorLightTokens.Scrim,
)

@Preview
@Composable
fun ColorsSchemePreviewLight() = ColorsSchemePreview(
    Color.Black,
    Color.White,
    materialColorSchemeLight,
)
