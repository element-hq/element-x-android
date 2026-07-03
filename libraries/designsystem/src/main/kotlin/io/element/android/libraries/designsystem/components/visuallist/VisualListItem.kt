/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.visuallist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text

/**
 * Ref: https://www.figma.com/design/G1xy0HDZKJf5TCRFmKb5d5/Compound-Android-Components?node-id=2001-159
 */
@Composable
fun VisualListItem(
    message: @Composable () -> Unit,
    position: VisualListItemPosition,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    val radius = 14.dp
    val backgroundShape = remember(position) {
        when (position) {
            VisualListItemPosition.Single -> RoundedCornerShape(radius)
            VisualListItemPosition.Top -> RoundedCornerShape(topStart = radius, topEnd = radius)
            VisualListItemPosition.Middle -> RoundedCornerShape(0.dp)
            VisualListItemPosition.Bottom -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = backgroundShape,
            )
            .padding(vertical = 12.dp, horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        icon()
        message()
    }
}

@PreviewsDayNight
@Composable
internal fun VisualListItemPreview() {
    ElementPreview {
        val color = ElementTheme.colors.bgSubtleSecondary
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            VisualListItem(
                message = { Text("A single item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = VisualListItemPosition.Single,
                backgroundColor = color,
            )
            VisualListItem(
                message = { Text("A top item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = VisualListItemPosition.Top,
                backgroundColor = color,
            )
            VisualListItem(
                message = { Text("A middle item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = VisualListItemPosition.Middle,
                backgroundColor = color,
            )
            VisualListItem(
                message = { Text("A bottom item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = VisualListItemPosition.Bottom,
                backgroundColor = color,
            )
        }
    }
}

enum class VisualListItemPosition {
    Top,
    Middle,
    Bottom,
    Single,
}
