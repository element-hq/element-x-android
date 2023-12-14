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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.matrix.api.room.RoomNotificationMode

@Composable
fun RoomNotificationSettingsOptions(
    selected: RoomNotificationMode?,
    enabled: Boolean,
    onOptionSelected: (RoomNotificationSettingsItem) -> Unit,
    displayMentionsOnlyDisclaimer: Boolean,
    modifier: Modifier = Modifier,
) {
    val items = roomNotificationSettingsItems()
    Column(modifier = modifier.selectableGroup()) {
        items.forEach { item ->
            RoomNotificationSettingsOption(
                roomNotificationSettingsItem = item,
                isSelected = selected == item.mode,
                onOptionSelected = onOptionSelected,
                displayMentionsOnlyDisclaimer = displayMentionsOnlyDisclaimer,
                enabled = enabled
            )
        }
    }
}
