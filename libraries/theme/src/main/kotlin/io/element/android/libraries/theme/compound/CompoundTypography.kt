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

package io.element.android.libraries.theme.compound

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.element.android.libraries.theme.compound.generated.TypographyTokens
import com.airbnb.android.showkase.annotation.ShowkaseTypography

// 32px (Material) vs 34px, it's the closest one
@ShowkaseTypography(name = "M3 Headline Large", group = "Compound")
internal val compoundHeadingXlRegular = TypographyTokens.fontHeadingXlRegular

// both are 28px
@ShowkaseTypography(name = "M3 Headline Medium", group = "Compound")
internal val compoundHeadingLgRegular = TypographyTokens.fontHeadingLgRegular

// These are the default M3 values, but we're setting them manually so an update in M3 doesn't break our designs
@ShowkaseTypography(name = "M3 Headline Small", group = "Compound")
internal val defaultHeadlineSmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    lineHeight = 32.sp,
    fontSize = 24.sp,
    letterSpacing = 0.em,
).forceLineHeight()

// 22px (Material) vs 20px, it's the closest one
@ShowkaseTypography(name = "M3 Title Large", group = "Compound")
internal val compoundHeadingMdRegular = TypographyTokens.fontHeadingMdRegular

// 16px both
@ShowkaseTypography(name = "M3 Title Medium", group = "Compound")
internal val compoundBodyLgMedium = TypographyTokens.fontBodyLgMedium

// 14px both
@ShowkaseTypography(name = "M3 Title Small", group = "Compound")
internal val compoundBodyMdMedium = TypographyTokens.fontBodyMdMedium

// 16px both
@ShowkaseTypography(name = "M3 Body Large", group = "Compound")
internal val compoundBodyLgRegular = TypographyTokens.fontBodyLgRegular

// 14px both
@ShowkaseTypography(name = "M3 Body Medium", group = "Compound")
internal val compoundBodyMdRegular = TypographyTokens.fontBodyMdRegular

// 12px both
@ShowkaseTypography(name = "M3 Body Small", group = "Compound")
internal val compoundBodySmRegular = TypographyTokens.fontBodySmRegular

// 14px both, Title Small uses the same token so we have to declare it twice
@ShowkaseTypography(name = "M3 Label Large", group = "Compound")
internal val compoundBodyMdMedium_LabelLarge = TypographyTokens.fontBodyMdMedium

// 12px both
@ShowkaseTypography(name = "M3 Label Medium", group = "Compound")
internal val compoundBodySmMedium = TypographyTokens.fontBodySmMedium

// 11px both
@ShowkaseTypography(name = "M3 Label Small", group = "Compound")
internal val compoundBodyXsMedium = TypographyTokens.fontBodyXsMedium

internal val compoundTypography = Typography(
    // displayLarge = , 57px (Material) size. We have no equivalent
    // displayMedium = , 45px (Material) size. We have no equivalent
    // displaySmall = , 36px (Material) size. We have no equivalent
    headlineLarge = compoundHeadingXlRegular,
    headlineMedium = compoundHeadingLgRegular,
    headlineSmall = defaultHeadlineSmall,
    titleLarge = compoundHeadingMdRegular,
    titleMedium = compoundBodyLgMedium,
    titleSmall = compoundBodyMdMedium,
    bodyLarge = compoundBodyLgRegular,
    bodyMedium = compoundBodyMdRegular,
    bodySmall = compoundBodySmRegular,
    labelLarge = compoundBodyMdMedium_LabelLarge,
    labelMedium = compoundBodySmMedium,
    labelSmall = compoundBodyXsMedium,
)

fun TextStyle.forceLineHeight() = copy(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)
