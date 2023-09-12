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

package io.element.android.features.preferences.impl.notifications.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.ui.strings.R
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun EditDefaultNotificationSettingView(
    state: EditDefaultNotificationSettingState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val title = if(state.isOneToOne) {
        CommonStrings.screen_notification_settings_direct_chats
    } else {
        CommonStrings.screen_notification_settings_group_chats
    }
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = title)
    ) {

        val validModes = listOf(RoomNotificationMode.ALL_MESSAGES, RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY)

        val categoryTitle = if(state.isOneToOne) {
            R.string.screen_notification_settings_edit_screen_direct_section_header
        } else {
            R.string.screen_notification_settings_edit_screen_group_section_header
        }
        PreferenceCategory(title = stringResource(id = categoryTitle)) {

            if (state.mode != null) {
                Column(modifier = modifier.selectableGroup()) {
                    validModes.forEach { item ->
                        DefaultNotificationSettingOption(
                            mode = item,
                            isSelected = state.mode == item,
                            onOptionSelected = { state.eventSink(EditDefaultNotificationSettingStateEvents.SetNotificationMode(it)) }
                        )
                    }
                }
            }
        }
    }
}

