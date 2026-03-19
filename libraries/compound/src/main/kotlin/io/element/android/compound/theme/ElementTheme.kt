/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import io.element.android.compound.tokens.compoundTypography
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.TypographyTokens
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight
import androidx.compose.material3.ColorScheme

/**
 * Inspired from https://medium.com/@lucasyujideveloper/54cbcbde1ace
 */
object ElementTheme {
    /**
     * The current [SemanticColors] provided by [ElementTheme].
     * These come from Compound and are the recommended colors to use for custom components.
     * In Figma, these colors usually have the `Light/` or `Dark/` prefix.
     */
    val colors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCompoundColors.current

    /**
     * The current Material 3 [ColorScheme] provided by [ElementTheme], coming from [MaterialTheme].
     * In Figma, these colors usually have the `M3/` prefix.
     */
    val materialColors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    /**
     * Compound [Typography] tokens. In Figma, these have the `Android/font/` prefix.
     */
    val typography: TypographyTokens = TypographyTokens

    /**
     * Returns whether the theme version used is the light or the dark one.
     */
    val isLightTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalCompoundColors.current.isLight
}

// Global variables (application level)
internal val LocalCompoundColors = staticCompositionLocalOf { compoundColorsLight }

/**
 * Sets up the theme for the application, or a part of it.
 *
 * @param darkTheme whether to use the dark theme or not. If `true`, the dark theme will be used.
 * @param applySystemBarsUpdate whether to update the system bars color scheme or not when the theme changes. It's `true` by default.
 * This is specially useful when you want to apply an alternate theme to a part of the app but don't want it to affect the system bars.
 * @param lightStatusBar whether to use a light status bar color scheme or not. By default, it's the opposite of [darkTheme].
 * @param dynamicColor whether to enable MaterialYou or not. It's `false` by default.
 * @param compoundLight the [SemanticColors] to use in light theme.
 * @param compoundDark the [SemanticColors] to use in dark theme.
 * @param materialColorsLight the Material 3 [ColorScheme] to use in light theme.
 * @param materialColorsDark the Material 3 [ColorScheme] to use in dark theme.
 * @param typography the Material 3 [Typography] tokens to use. It'll use [compoundTypography] by default.
 * @param content the content to apply the theme to.
 */
@Composable
fun ElementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    applySystemBarsUpdate: Boolean = true,
    lightStatusBar: Boolean = !darkTheme,
    // true to enable MaterialYou
    dynamicColor: Boolean = false,
    compoundLight: SemanticColors = compoundColorsLight,
    compoundDark: SemanticColors = compoundColorsDark,
    materialColorsLight: ColorScheme = compoundLight.toMaterialColorScheme(),
    materialColorsDark: ColorScheme = compoundDark.toMaterialColorScheme(),
    typography: Typography = compoundTypography,
    content: @Composable () -> Unit,
) {
    val baseCompoundColor = when {
        darkTheme -> compoundDark
        else -> compoundLight
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> materialColorsDark
        else -> materialColorsLight
    }

    // When dynamic color is active, adapt key Compound semantic tokens from the
    // system-derived M3 ColorScheme so ElementTheme.colors reflects wallpaper colors.
    val currentCompoundColor = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseCompoundColor.withDynamicColors(colorScheme)
    } else {
        baseCompoundColor
    }

    val statusBarColorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (lightStatusBar) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        colorScheme
    }

    if (applySystemBarsUpdate) {
        val activity = LocalActivity.current as? ComponentActivity
        LaunchedEffect(statusBarColorScheme, darkTheme, lightStatusBar) {
            activity?.enableEdgeToEdge(
                // For Status bar use the background color of the app
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = statusBarColorScheme.background.toArgb(),
                    darkScrim = statusBarColorScheme.background.toArgb(),
                    detectDarkMode = { !lightStatusBar }
                ),
                // For Navigation bar use a transparent color so the content can be seen through it
                navigationBarStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(Color.Transparent.toArgb(), Color.Transparent.toArgb())
                }
            )
        }
    }
    CompositionLocalProvider(
        LocalCompoundColors provides currentCompoundColor,
        LocalContentColor provides colorScheme.onSurface,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

/**
 * Adapt key Compound semantic colors from a dynamic M3 ColorScheme (Material You).
 * This maps the wallpaper-derived palette onto the most visible Compound tokens
 * while preserving brand-specific tokens (decorative, gradient, badge colors).
 */
private fun SemanticColors.withDynamicColors(scheme: ColorScheme): SemanticColors = copy(
    // Primary action backgrounds → M3 primary
    bgActionPrimaryRest = scheme.primary,
    bgActionPrimaryHovered = scheme.primary,
    bgActionPrimaryPressed = scheme.primary,
    bgActionPrimaryDisabled = scheme.onSurface.copy(alpha = 0.12f),
    // Accent backgrounds → M3 primary
    bgAccentRest = scheme.primary,
    bgAccentHovered = scheme.primary,
    bgAccentPressed = scheme.primary,
    bgAccentSelected = scheme.primary,
    // Canvas/surface backgrounds → M3 surface
    bgCanvasDefault = scheme.surface,
    bgCanvasDefaultLevel1 = scheme.surfaceContainerLow,
    bgCanvasDisabled = scheme.onSurface.copy(alpha = 0.12f),
    // Subtle backgrounds → M3 surface variants
    bgSubtlePrimary = scheme.surfaceContainerHigh,
    bgSubtleSecondary = scheme.surfaceContainerLow,
    // Secondary/tertiary actions → M3 secondary
    bgActionSecondaryRest = scheme.surfaceContainerHighest,
    bgActionSecondaryHovered = scheme.surfaceContainerHighest,
    bgActionSecondaryPressed = scheme.surfaceContainerHighest,
    bgActionTertiaryRest = scheme.surfaceContainerLow,
    bgActionTertiaryHovered = scheme.surfaceContainerLow,
    bgActionTertiarySelected = scheme.secondaryContainer,
    // Text colors → M3 on* roles
    textPrimary = scheme.onSurface,
    textSecondary = scheme.onSurfaceVariant,
    textOnSolidPrimary = scheme.onPrimary,
    textDisabled = scheme.onSurface.copy(alpha = 0.38f),
    textActionPrimary = scheme.primary,
    textActionAccent = scheme.primary,
    textLinkExternal = scheme.primary,
    // Icon colors → M3 on* roles
    iconPrimary = scheme.onSurface,
    iconSecondary = scheme.onSurfaceVariant,
    iconTertiary = scheme.onSurfaceVariant,
    iconQuaternary = scheme.outline,
    iconOnSolidPrimary = scheme.onPrimary,
    iconAccentPrimary = scheme.primary,
    iconAccentTertiary = scheme.tertiary,
    iconDisabled = scheme.onSurface.copy(alpha = 0.38f),
    // Border colors → M3 outline roles
    borderInteractivePrimary = scheme.outline,
    borderInteractiveHovered = scheme.primary,
    borderInteractiveSecondary = scheme.outlineVariant,
    borderDisabled = scheme.onSurface.copy(alpha = 0.12f),
    borderFocused = scheme.primary,
    // Critical/error colors stay from Compound (brand-critical, shouldn't change)
    // Decorative/gradient colors stay from Compound (brand-specific)
)
