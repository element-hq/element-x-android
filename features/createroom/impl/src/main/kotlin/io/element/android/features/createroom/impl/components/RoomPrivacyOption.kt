/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.createroom.impl.configureroom.RoomPrivacyItem
import io.element.android.features.createroom.impl.configureroom.roomPrivacyItems
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomPrivacyOption(
    roomPrivacyItem: RoomPrivacyItem,
    onOptionClick: (RoomPrivacyItem) -> Unit,
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
            .padding(8.dp),
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 8.dp),
            resourceId = roomPrivacyItem.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = roomPrivacyItem.title,
                style = ElementTheme.typography.fontBodyLgRegular,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = roomPrivacyItem.description,
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
internal fun RoomPrivacyOptionPreview() = ElementPreview {
    val aRoomPrivacyItem = roomPrivacyItems().first()
    Column {
        RoomPrivacyOption(
            roomPrivacyItem = aRoomPrivacyItem,
            onOptionClick = {},
            isSelected = true,
        )
        RoomPrivacyOption(
            roomPrivacyItem = aRoomPrivacyItem,
            onOptionClick = {},
            isSelected = false,
        )
    }
}
