/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.createroom.impl.configureroom.RoomVisibilityItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomVisibilityOption(
    roomPrivacyItem: RoomVisibilityItem,
    onOptionClick: (RoomVisibilityItem) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    Row(
        modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onOptionClick(roomPrivacyItem) },
                role = Role.RadioButton,
            )
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ElementTheme.colors.bgSubtleSecondary)
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                resourceId = roomPrivacyItem.icon,
                contentDescription = null,
                tint = if (isSelected) ElementTheme.colors.iconPrimary else ElementTheme.colors.iconSecondary,
            )
        }
        Spacer(Modifier.size(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(roomPrivacyItem.title),
                style = ElementTheme.typography.fontBodyLgRegular,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = stringResource(roomPrivacyItem.description),
                style = ElementTheme.typography.fontBodySmRegular,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        RadioButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(48.dp),
            selected = isSelected,
            // null recommended for accessibility with screenreaders
            onClick = null
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomVisibilityOptionPreview() = ElementPreview {
    val aRoomPrivacyItem = RoomVisibilityItem.Private
    Column {
        RoomVisibilityOption(
            roomPrivacyItem = aRoomPrivacyItem,
            onOptionClick = {},
            isSelected = true,
        )
        RoomVisibilityOption(
            roomPrivacyItem = aRoomPrivacyItem,
            onOptionClick = {},
            isSelected = false,
        )
    }
}
