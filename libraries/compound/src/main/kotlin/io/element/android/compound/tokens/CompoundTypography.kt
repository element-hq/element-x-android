/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.airbnb.android.showkase.annotation.ShowkaseTypography
import io.element.android.compound.tokens.generated.TypographyTokens

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
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)

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

// M3 Expressive display styles — needed for hero text on onboarding, empty states, etc.
@ShowkaseTypography(name = "M3 Display Large", group = "Compound")
internal val defaultDisplayLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)

@ShowkaseTypography(name = "M3 Display Medium", group = "Compound")
internal val defaultDisplayMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)

@ShowkaseTypography(name = "M3 Display Small", group = "Compound")
internal val defaultDisplaySmall = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
)

internal val compoundTypography = Typography(
    displayLarge = defaultDisplayLarge,
    displayMedium = defaultDisplayMedium,
    displaySmall = defaultDisplaySmall,
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
