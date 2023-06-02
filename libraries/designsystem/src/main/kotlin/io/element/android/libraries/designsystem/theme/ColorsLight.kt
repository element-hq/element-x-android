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
import io.element.android.libraries.designsystem.Black_900
import io.element.android.libraries.designsystem.Compound_Gray_300_Light
import io.element.android.libraries.designsystem.Compound_Gray_400_Light
import io.element.android.libraries.designsystem.Compound_Gray_800_Light
import io.element.android.libraries.designsystem.Gray_100
import io.element.android.libraries.designsystem.Compound_Gray_1400_Light
import io.element.android.libraries.designsystem.Gray_150
import io.element.android.libraries.designsystem.Gray_200
import io.element.android.libraries.designsystem.Gray_25
import io.element.android.libraries.designsystem.Gray_50
import io.element.android.libraries.designsystem.SystemGrey5Light
import io.element.android.libraries.designsystem.SystemGrey6Light
import io.element.android.libraries.designsystem.TextColorCriticalLight
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
    placeholder = Compound_Gray_800_Light,
    isLight = true,
)

// TODO Lots of colors are missing
val materialColorSchemeLight = lightColorScheme(
    primary = Black_900,
    onPrimary = Color.White,
    // TODO primaryContainer = ColorLightTokens.PrimaryContainer,
    // TODO onPrimaryContainer = ColorLightTokens.OnPrimaryContainer,
    // TODO inversePrimary = ColorLightTokens.InversePrimary,
    secondary = Gray_200,
    // TODO onSecondary = ColorLightTokens.OnSecondary,
    // TODO secondaryContainer = ColorLightTokens.SecondaryContainer,
    // TODO onSecondaryContainer = ColorLightTokens.OnSecondaryContainer,
    tertiary = Gray_150,
    // TODO onTertiary = ColorLightTokens.OnTertiary,
    // TODO tertiaryContainer = ColorLightTokens.TertiaryContainer,
    // TODO onTertiaryContainer = ColorLightTokens.OnTertiaryContainer,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Gray_25,
    onSurfaceVariant = Gray_200,
    // TODO surfaceTint = primary,
    // TODO inverseSurface = ColorLightTokens.InverseSurface,
    // TODO inverseOnSurface = ColorLightTokens.InverseOnSurface,
    // TODO error = ColorLightTokens.Error,
    // TODO onError = ColorLightTokens.OnError,
    // TODO errorContainer = ColorLightTokens.ErrorContainer,
    // TODO onErrorContainer = ColorLightTokens.OnErrorContainer,
    // TODO outline = ColorLightTokens.Outline,
    outlineVariant = Gray_50,
    // TODO scrim = ColorLightTokens.Scrim,
)

@Preview
@Composable
fun ColorsSchemePreviewLight() = ColorsSchemePreview(
    Color.Black,
    Color.White,
    materialColorSchemeLight,
)
