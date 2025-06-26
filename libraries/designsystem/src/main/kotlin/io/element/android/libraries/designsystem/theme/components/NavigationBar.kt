/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.CounterAtom
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import androidx.compose.material3.NavigationBarItem as MaterialNavigationBarItem

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = ElementNavigationBarDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    tonalElevation: Dp = ElementNavigationBarDefaults.tonalElevation,
    windowInsets: WindowInsets = ElementNavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.material3.NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        windowInsets = windowInsets,
        content = content
    )
}

object ElementNavigationBarDefaults {
    val containerColor: Color
        @Composable get() = if (ElementTheme.isLightTheme) {
            ElementTheme.colors.bgSubtlePrimary
        } else {
            ElementTheme.colors.textOnSolidPrimary
        }

    val tonalElevation: Dp = NavigationBarDefaults.Elevation

    val windowInsets: WindowInsets
        @Composable get() = NavigationBarDefaults.windowInsets
}

@Composable
fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    colors: NavigationBarItemColors = ElementNavigationBarItemDefaultsDefaults.colors(),
    interactionSource: MutableInteractionSource? = null
) {
    MaterialNavigationBarItem(
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

object ElementNavigationBarItemDefaultsDefaults {
    @Composable
    fun colors() = NavigationBarItemDefaults.colors().copy(
        selectedIconColor = ElementTheme.colors.iconPrimary,
        selectedTextColor = ElementTheme.colors.textPrimary,
        unselectedIconColor = ElementTheme.colors.iconTertiary,
        unselectedTextColor = ElementTheme.colors.textDisabled,
        selectedIndicatorColor = Color.Transparent,
    )
}

@Composable
fun NavigationBarIcon(
    imageVector: ImageVector,
    count: Int,
    isCritical: Boolean,
) {
    Box {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
        )
        CounterAtom(
            modifier = Modifier.offset(11.dp, (-11).dp),
            textStyle = ElementTheme.typography.fontBodyXsMedium,
            count = count,
            isCritical = isCritical,
        )
    }
}

@Composable
fun NavigationBarText(
    text: String,
) {
    Text(
        text = text,
        style = ElementTheme.typography.fontBodySmMedium,
    )
}

@Preview(group = PreviewGroup.AppBars)
@Composable
internal fun NavigationBarPreview() = ElementThemedPreview {
    NavigationBar {
        NavigationBarItem(
            icon = {
                NavigationBarIcon(
                    imageVector = CompoundIcons.ChatSolid(),
                    count = 5,
                    isCritical = false,
                )
            },
            label = {
                NavigationBarText(
                    text = "Chats"
                )
            },
            selected = true,
            onClick = {},
        )
        NavigationBarItem(
            icon = {
                NavigationBarIcon(
                    imageVector = CompoundIcons.ChatSolid(),
                    count = 5,
                    isCritical = true,
                )
            },
            label = {
                NavigationBarText(
                    text = "Teams"
                )
            },
            selected = false,
            onClick = {},
        )
        NavigationBarItem(
            icon = {
                NavigationBarIcon(
                    imageVector = CompoundIcons.ChatSolid(),
                    count = 0,
                    isCritical = false,
                )
            },
            label = {
                NavigationBarText(
                    text = "Other"
                )
            },
            selected = false,
            onClick = {},
        )
    }
}
