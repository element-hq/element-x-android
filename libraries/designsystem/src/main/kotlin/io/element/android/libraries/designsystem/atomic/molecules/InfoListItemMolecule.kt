/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.molecules

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

@Composable
fun InfoListItemMolecule(
    message: @Composable () -> Unit,
    position: InfoListItemPosition,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = {},
) {
    val radius = 14.dp
    val backgroundShape = remember(position) {
        when (position) {
            InfoListItemPosition.Single -> RoundedCornerShape(radius)
            InfoListItemPosition.Top -> RoundedCornerShape(topStart = radius, topEnd = radius)
            InfoListItemPosition.Middle -> RoundedCornerShape(0.dp)
            InfoListItemPosition.Bottom -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
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
internal fun InfoListItemMoleculePreview() {
    ElementPreview {
        val color = ElementTheme.colors.bgSubtleSecondary
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InfoListItemMolecule(
                message = { Text("A single item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = InfoListItemPosition.Single,
                backgroundColor = color,
            )
            InfoListItemMolecule(
                message = { Text("A top item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = InfoListItemPosition.Top,
                backgroundColor = color,
            )
            InfoListItemMolecule(
                message = { Text("A middle item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = InfoListItemPosition.Middle,
                backgroundColor = color,
            )
            InfoListItemMolecule(
                message = { Text("A bottom item") },
                icon = { Icon(imageVector = CompoundIcons.InfoSolid(), contentDescription = null) },
                position = InfoListItemPosition.Bottom,
                backgroundColor = color,
            )
        }
    }
}

enum class InfoListItemPosition {
    Top,
    Middle,
    Bottom,
    Single,
}
