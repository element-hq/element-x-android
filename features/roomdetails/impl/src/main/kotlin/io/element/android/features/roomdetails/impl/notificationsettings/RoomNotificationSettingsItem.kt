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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class RoomNotificationSettingsItem(
    val mode: RoomNotificationMode,
    val title: String,
)

@Composable
fun roomNotificationSettingsItems(): ImmutableList<RoomNotificationSettingsItem> {
    return RoomNotificationMode.entries
        .map {
            when (it) {
                RoomNotificationMode.ALL_MESSAGES -> RoomNotificationSettingsItem(
                    mode = it,
                    title = stringResource(R.string.screen_room_notification_settings_mode_all_messages),
                )
                RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY -> RoomNotificationSettingsItem(
                    mode = it,
                    title = stringResource(R.string.screen_room_notification_settings_mode_mentions_and_keywords),
                )
                RoomNotificationMode.MUTE -> RoomNotificationSettingsItem(
                    mode = it,
                    title = stringResource(CommonStrings.common_mute),
                )
            }
        }
        .toImmutableList()
}
