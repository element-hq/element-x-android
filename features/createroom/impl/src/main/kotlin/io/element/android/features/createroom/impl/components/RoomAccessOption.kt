/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.createroom.impl.configureroom.RoomAccessItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomAccessOption(
    roomAccessItem: RoomAccessItem,
    onOptionClick: (RoomAccessItem) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    Row(
        modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onOptionClick(roomAccessItem) },
                role = Role.RadioButton,
            )
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(roomAccessItem.title),
                style = ElementTheme.typography.fontBodyLgRegular,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = stringResource(roomAccessItem.description),
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
internal fun RoomAccessOptionPreview() = ElementPreview {
    val aRoomAccessItem = RoomAccessItem.Anyone
    Column {
        RoomAccessOption(
            roomAccessItem = aRoomAccessItem,
            onOptionClick = {},
            isSelected = true,
        )
        RoomAccessOption(
            roomAccessItem = aRoomAccessItem,
            onOptionClick = {},
            isSelected = false,
        )
    }
}
