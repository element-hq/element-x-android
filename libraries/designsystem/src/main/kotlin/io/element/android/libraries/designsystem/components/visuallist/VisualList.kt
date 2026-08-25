/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.visuallist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2001-140
 */
@Composable
fun VisualList(
    items: ImmutableList<VisualListItemData>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElementTheme.colors.bgSubtleSecondary,
    iconTint: Color = ElementTheme.colors.iconSecondary,
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = ElementTheme.typography.fontBodyMdMedium,
    textColor: Color = ElementTheme.colors.textPrimary,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
    ) {
        for ((index, item) in items.withIndex()) {
            val position = when {
                items.size == 1 -> VisualListItemPosition.Single
                index == 0 -> VisualListItemPosition.Top
                index == items.size - 1 -> VisualListItemPosition.Bottom
                else -> VisualListItemPosition.Middle
            }
            VisualListItem(
                message = {
                    if (item.message is AnnotatedString) {
                        Text(
                            text = item.message,
                            style = textStyle,
                            color = textColor,
                        )
                    } else {
                        Text(
                            text = item.message.toString(),
                            style = textStyle,
                            color = textColor,
                        )
                    }
                },
                icon = {
                    if (item.iconId != null) {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            resourceId = item.iconId,
                            contentDescription = null,
                            tint = iconTint,
                        )
                    } else if (item.iconVector != null) {
                        Icon(
                            modifier = Modifier.size(iconSize),
                            imageVector = item.iconVector,
                            contentDescription = null,
                            tint = iconTint,
                        )
                    } else {
                        item.iconComposable()
                    }
                },
                position = position,
                backgroundColor = backgroundColor,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun VisualListPreview() = ElementPreview {
    val items = persistentListOf(
        VisualListItemData(message = "A top item"),
        VisualListItemData(message = "A middle item"),
        VisualListItemData(message = "A bottom item"),
    )
    VisualList(
        items = items,
    )
}
