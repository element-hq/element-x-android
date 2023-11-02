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

package io.element.android.features.roomdetails.impl.notificationsettings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.theme.ElementTheme

@Composable
fun RoomNotificationSettingsOption(
    roomNotificationSettingsItem: RoomNotificationSettingsItem,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isSelected: Boolean = false,
    onOptionSelected: (RoomNotificationSettingsItem) -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = enabled,
                onClick = { onOptionSelected(roomNotificationSettingsItem) },
                role = Role.RadioButton,
            )
            .padding(8.dp),
    ) {
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = roomNotificationSettingsItem.title,
                style = ElementTheme.typography.fontBodyLgRegular,
                color = enabled.toEnabledColor(),
            )
        }

        RadioButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(48.dp),
            selected = isSelected,
            enabled = enabled,
            onClick = null // null recommended for accessibility with screenreaders
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomPrivacyOptionPreview() = ElementPreview {
    Column {
        RoomNotificationSettingsOption(
            roomNotificationSettingsItem = roomNotificationSettingsItems().first(),
            isSelected = true,
        )
        RoomNotificationSettingsOption(
            roomNotificationSettingsItem = roomNotificationSettingsItems().last(),
            isSelected = false,
            enabled = false,
        )
    }
}
