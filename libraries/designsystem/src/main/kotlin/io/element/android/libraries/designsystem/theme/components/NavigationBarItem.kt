/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.element.android.compound.theme.ElementTheme

@Composable
fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    colors: NavigationBarItemColors = ElementNavigationBarItemDefaults.colors(),
    interactionSource: MutableInteractionSource? = null
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = colors,
        interactionSource = interactionSource,
    )
}

object ElementNavigationBarItemDefaults {
    @Composable
    fun colors() = NavigationBarItemDefaults.colors().copy(
        selectedIconColor = ElementTheme.colors.iconPrimary,
        selectedTextColor = ElementTheme.colors.textPrimary,
        unselectedIconColor = ElementTheme.colors.iconTertiary,
        unselectedTextColor = ElementTheme.colors.textDisabled,
        selectedIndicatorColor = Color.Companion.Transparent,
    )
}
