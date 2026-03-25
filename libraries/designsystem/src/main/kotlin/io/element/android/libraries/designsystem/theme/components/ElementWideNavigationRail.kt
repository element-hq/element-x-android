/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ElementWideNavigationRail(
    modifier: Modifier = Modifier,
    containerColor: Color = ElementWideNavigationRailDefaults.containerColor,
    contentColor: Color = ElementTheme.materialColors.contentColorFor(containerColor),
    windowInsets: WindowInsets = WideNavigationRailDefaults.windowInsets,
    header: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    WideNavigationRail(
        modifier = modifier,
        colors = WideNavigationRailDefaults.colors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        windowInsets = windowInsets,
        header = header,
        content = content,
    )
}

object ElementWideNavigationRailDefaults {
    val containerColor: Color
        @Composable get() = ElementTheme.colors.bgSubtlePrimary
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@PreviewsDayNight
@Composable
internal fun ElementWideNavigationRailPreview() = ElementThemedPreview {
    ElementWideNavigationRail {
        WideNavigationRailItem(
            railExpanded = true,
            icon = {
                Icon(
                    imageVector = CompoundIcons.ChatSolid(),
                    contentDescription = null,
                )
            },
            label = { Text("Chats") },
            selected = true,
            onClick = {},
        )
        WideNavigationRailItem(
            railExpanded = true,
            icon = {
                Icon(
                    imageVector = CompoundIcons.Threads(),
                    contentDescription = null,
                )
            },
            label = { Text("Spaces") },
            selected = false,
            onClick = {},
        )
    }
}
