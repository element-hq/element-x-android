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
import io.element.android.libraries.designsystem.Black_800
import io.element.android.libraries.designsystem.Black_950
import io.element.android.libraries.designsystem.Compound_Gray_300_Dark
import io.element.android.libraries.designsystem.DarkGrey
import io.element.android.libraries.designsystem.Gray_1400_Dark
import io.element.android.libraries.designsystem.Gray_300
import io.element.android.libraries.designsystem.Gray_400
import io.element.android.libraries.designsystem.Compound_Gray_400_Dark
import io.element.android.libraries.designsystem.Gray_450
import io.element.android.libraries.designsystem.SystemGrey5Dark
import io.element.android.libraries.designsystem.SystemGrey6Dark
import io.element.android.libraries.designsystem.TextColorCriticalDark
import io.element.android.libraries.designsystem.theme.previews.ColorsSchemePreview

fun elementColorsDark() = ElementColors(
    messageFromMeBackground = SystemGrey5Dark,
    messageFromOtherBackground = SystemGrey6Dark,
    messageHighlightedBackground = Azure,
    quaternary = Gray_400,
    quinary = Gray_450,
    gray300 = Compound_Gray_300_Dark,
    gray400 = Compound_Gray_400_Dark,
    gray1400 = Gray_1400_Dark,
    textActionCritical = TextColorCriticalDark,
    isLight = false,
)

// TODO Lots of colors are missing
val materialColorSchemeDark = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    // TODO primaryContainer = ColorDarkTokens.PrimaryContainer,
    // TODO onPrimaryContainer = ColorDarkTokens.OnPrimaryContainer,
    // TODO inversePrimary = ColorDarkTokens.InversePrimary,
    secondary = DarkGrey,
    // TODO onSecondary = ColorDarkTokens.OnSecondary,
    // TODO secondaryContainer = ColorDarkTokens.SecondaryContainer,
    // TODO onSecondaryContainer = ColorDarkTokens.OnSecondaryContainer,
    tertiary = Color.White,
    // TODO onTertiary = ColorDarkTokens.OnTertiary,
    // TODO tertiaryContainer = ColorDarkTokens.TertiaryContainer,
    // TODO onTertiaryContainer = ColorDarkTokens.OnTertiaryContainer,
    background = Black_800,
    onBackground = Color.White,
    surface = Black_800,
    onSurface = Color.White,
    surfaceVariant = Black_950,
    onSurfaceVariant = Gray_300,
    // TODO surfaceTint = primary,
    // TODO inverseSurface = ColorDarkTokens.InverseSurface,
    // TODO inverseOnSurface = ColorDarkTokens.InverseOnSurface,
    // TODO error = ColorDarkTokens.Error,
    // TODO onError = ColorDarkTokens.OnError,
    // TODO errorContainer = ColorDarkTokens.ErrorContainer,
    // TODO onErrorContainer = ColorDarkTokens.OnErrorContainer,
    // TODO outline = ColorDarkTokens.Outline,
    outlineVariant = Gray_450,
    // TODO scrim = ColorDarkTokens.Scrim,
)

@Preview
@Composable
fun ColorsSchemePreviewDark() = ColorsSchemePreview(
    Color.White,
    Color.Black,
    materialColorSchemeDark,
)
