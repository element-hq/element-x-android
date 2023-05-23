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

package io.element.android.libraries.designsystem.theme.compound

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.airbnb.android.showkase.annotation.ShowkaseTypography

// 32px (Material) vs 34px, it's the closest one
@ShowkaseTypography(name = "M3 Headline Large", group = "Compound")
internal val headlineLarge = TypographyTokens.fontHeadingXlRegular

// both are 28px
@ShowkaseTypography(name = "M3 Headline Medium", group = "Compound")
internal val headlineMedium = TypographyTokens.fontHeadingLgRegular

// These are the default M3 values, but we're setting them manually so a update in M3 doesn't break our designs
@ShowkaseTypography(name = "M3 Headline Small", group = "Compound")
internal val headlineSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = TypographyTokens.fontWeightRegular,
    lineHeight = 32.sp,
    fontSize = 24.sp,
    letterSpacing = 0.em,
)

// 22px (Material) vs 20px, it's the closest one
@ShowkaseTypography(name = "M3 Title Large", group = "Compound")
internal val titleLarge = TypographyTokens.fontHeadingMdRegular

// 16px both
@ShowkaseTypography(name = "M3 Title Medium", group = "Compound")
internal val titleMedium = TypographyTokens.fontBodyLgMedium

// 14px both
@ShowkaseTypography(name = "M3 Title Small", group = "Compound")
internal val titleSmall = TypographyTokens.fontBodyMdMedium

// 16px both
@ShowkaseTypography(name = "M3 Body Large", group = "Compound")
internal val bodyLarge = TypographyTokens.fontBodyLgRegular

// 14px both
@ShowkaseTypography(name = "M3 Body Medium", group = "Compound")
internal val bodyMedium = TypographyTokens.fontBodyMdRegular

// 12px both
@ShowkaseTypography(name = "M3 Body Small", group = "Compound")
internal val bodySmall = TypographyTokens.fontBodySmRegular

// 14px both
@ShowkaseTypography(name = "M3 Label Large", group = "Compound")
internal val labelLarge = TypographyTokens.fontBodyMdMedium

// 12px both
@ShowkaseTypography(name = "M3 Label Medium", group = "Compound")
internal val labelMedium = TypographyTokens.fontBodySmMedium

// 11px both
@ShowkaseTypography(name = "M3 Label Small", group = "Compound")
internal val labelSmall = TypographyTokens.fontBodyXsMedium

val compoundTypography = Typography(
    // displayLarge = , 57px (Material) size. We have no equivalent
    // displayMedium = , 45px (Material) size. We have no equivalent
    // displaySmall = , 36px (Material) size. We have no equivalent
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall,
)
