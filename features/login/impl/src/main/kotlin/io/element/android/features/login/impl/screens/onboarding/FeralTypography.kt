/*
 * Copyright 2025 Feral
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Feral onboarding typography: serif faces matching the iOS landing page.
 */
object FeralTypography {
    val serifFontFamily = FontFamily.Serif

    val welcomeTitle = TextStyle(
        fontFamily = serifFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        letterSpacing = (-0.5).sp,
    )

    val sectionTitle = TextStyle(
        fontFamily = serifFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
    )
}
