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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.createroom.impl.configureroom.RoomPrivacyItem
import io.element.android.features.createroom.impl.configureroom.roomPrivacyItems
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun RoomPrivacyOption(
    roomPrivacyItem: RoomPrivacyItem,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onOptionSelected: (RoomPrivacyItem) -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onOptionSelected(roomPrivacyItem) },
                role = Role.RadioButton,
            )
            .padding(8.dp),
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = 8.dp),
            imageVector = roomPrivacyItem.icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.secondary,
        )

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = roomPrivacyItem.title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(3.dp))
            Text(
                text = roomPrivacyItem.description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        RadioButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(48.dp),
            selected = isSelected,
            onClick = null // null recommended for accessibility with screenreaders
        )
    }
}

@Preview
@Composable
fun RoomPrivacyOptionLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun RoomPrivacyOptionDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    val aRoomPrivacyItem = roomPrivacyItems().first()
    Column {
        RoomPrivacyOption(
            roomPrivacyItem = aRoomPrivacyItem,
            isSelected = true,
        )
        RoomPrivacyOption(
            roomPrivacyItem = aRoomPrivacyItem,
            isSelected = false,
        )
    }
}
