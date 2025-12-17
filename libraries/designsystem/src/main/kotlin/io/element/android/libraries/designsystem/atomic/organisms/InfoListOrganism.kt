/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.organisms

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.molecules.InfoListItemMolecule
import io.element.android.libraries.designsystem.atomic.molecules.InfoListItemPosition
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun InfoListOrganism(
    items: ImmutableList<InfoListItem>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ElementTheme.colors.bgSubtleSecondary,
    iconTint: Color = LocalContentColor.current,
    iconSize: Dp = 20.dp,
    textStyle: TextStyle = LocalTextStyle.current,
    textColor: Color = ElementTheme.colors.textPrimary,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
    ) {
        for ((index, item) in items.withIndex()) {
            val position = when {
                items.size == 1 -> InfoListItemPosition.Single
                index == 0 -> InfoListItemPosition.Top
                index == items.size - 1 -> InfoListItemPosition.Bottom
                else -> InfoListItemPosition.Middle
            }
            InfoListItemMolecule(
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

data class InfoListItem(
    val message: CharSequence,
    @DrawableRes val iconId: Int? = null,
    val iconVector: ImageVector? = null,
    val iconComposable: @Composable () -> Unit = {},
)

@PreviewsDayNight
@Composable
internal fun InfoListOrganismPreview() = ElementPreview {
    val items = persistentListOf(
        InfoListItem(message = "A top item"),
        InfoListItem(message = "A middle item"),
        InfoListItem(message = "A bottom item"),
    )
    InfoListOrganism(
        items = items,
    )
}
