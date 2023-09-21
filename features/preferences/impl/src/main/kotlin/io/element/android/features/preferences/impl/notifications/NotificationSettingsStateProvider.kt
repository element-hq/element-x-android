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

package io.element.android.features.preferences.impl.notifications

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.room.RoomNotificationMode

open class NotificationSettingsStateProvider : PreviewParameterProvider<NotificationSettingsState> {
    override val values: Sequence<NotificationSettingsState>
        get() = sequenceOf(
            aNotificationSettingsState(),
        )
}

fun aNotificationSettingsState() = NotificationSettingsState(
    matrixSettings = NotificationSettingsState.MatrixSettings.Valid(
        atRoomNotificationsEnabled = true,
        callNotificationsEnabled = true,
        defaultGroupNotificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
        defaultOneToOneNotificationMode = RoomNotificationMode.ALL_MESSAGES,
    ),
    appSettings = NotificationSettingsState.AppSettings(
        systemNotificationsEnabled = false,
        appNotificationsEnabled = true,
    ),
    eventSink = {}
)
