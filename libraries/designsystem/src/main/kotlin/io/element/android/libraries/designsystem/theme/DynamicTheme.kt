/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight

@Composable
fun rememberDynamicSemanticColors(
    compoundColors: SemanticColors,
    isDark: Boolean,
    useDynamicTheme: Boolean,
): SemanticColors {
    val context = LocalContext.current
    return remember(compoundColors, isDark, context, useDynamicTheme) {
        if (useDynamicTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dynamicScheme = if (isDark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            dynamicScheme.toSemanticColors(isDark, compoundColors)
        } else {
            compoundColors
        }
    }
}

fun ColorScheme.toSemanticColors(
    isDark: Boolean,
    fallback: SemanticColors
): SemanticColors {
    // We start with the fallback (default Compound theme) and override key colors
    // with the dynamic system colors.
    
    return fallback.copy(
        // Backgrounds
        bgCanvasDefault = this.background,
        bgCanvasDefaultLevel1 = this.surface,
        bgSubtleSecondary = this.surfaceVariant,
        
        // Primary Actions / Accents
        bgActionPrimaryRest = this.primary,
        bgActionPrimaryHovered = this.primary.copy(alpha = 0.8f), // Approximation
        bgActionPrimaryPressed = this.primary.copy(alpha = 0.6f), // Approximation
        
        textOnSolidPrimary = this.onPrimary,
        
        // Text
        textPrimary = this.onBackground,
        textSecondary = this.onSurfaceVariant,
        
        // Accents (Icons, Text Links)
        iconAccentPrimary = this.primary,
        textActionAccent = this.primary,
        
        // Interactive Elements
        borderFocused = this.primary,
        
        // Note: This is an initial mapping. Complex tokens like gradients or specific 
        // decorative colors retain their Compound defaults to ensure consistency 
        // where dynamic colors might not provide a suitable equivalent.
    )
}
