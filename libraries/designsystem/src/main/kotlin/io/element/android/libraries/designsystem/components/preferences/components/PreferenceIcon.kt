/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.toIconSecondaryEnabledColor

@Composable
fun preferenceIcon(
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconBadge: Boolean = false,
    tintColor: Color? = null,
    enabled: Boolean = true,
    showIconAreaIfNoIcon: Boolean = false,
): ListItemContent.Custom? {
    return if (icon != null || iconResourceId != null || showIconAreaIfNoIcon) {
        ListItemContent.Custom {
            PreferenceIcon(
                icon = icon,
                iconResourceId = iconResourceId,
                showIconBadge = showIconBadge,
                enabled = enabled,
                isVisible = showIconAreaIfNoIcon,
                tintColor = tintColor,
            )
        }
    } else {
        null
    }
}

@Composable
private fun PreferenceIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconBadge: Boolean = false,
    tintColor: Color? = null,
    enabled: Boolean = true,
    isVisible: Boolean = true,
) {
    if (icon != null || iconResourceId != null) {
        Box(modifier = modifier) {
            Icon(
                imageVector = icon,
                resourceId = iconResourceId,
                contentDescription = null,
                tint = tintColor ?: enabled.toIconSecondaryEnabledColor(),
                modifier = Modifier
                    .size(24.dp),
            )
            if (showIconBadge) {
                RedIndicatorAtom(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                )
            }
        }
    } else if (isVisible) {
        Spacer(modifier = modifier.width(24.dp))
    }
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceIconPreview(@PreviewParameter(ImageVectorProvider::class) content: ImageVector?) =
    ElementThemedPreview {
        PreferenceIcon(
            icon = content,
            showIconBadge = false,
        )
    }

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceIconWithBadgePreview(@PreviewParameter(ImageVectorProvider::class) content: ImageVector?) =
    ElementThemedPreview {
        PreferenceIcon(
            icon = content,
            showIconBadge = true,
        )
    }
