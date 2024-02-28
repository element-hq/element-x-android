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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomdetails.impl.aRoomNotificationSettings
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.RoomNotificationMode

internal class RoomNotificationSettingsStateProvider : PreviewParameterProvider<RoomNotificationSettingsState> {
    override val values: Sequence<RoomNotificationSettingsState>
        get() = sequenceOf(
            aRoomNotificationSettingsState(),
            aRoomNotificationSettingsState(isDefault = false),
            aRoomNotificationSettingsState(setNotificationSettingAction = AsyncAction.Loading),
            aRoomNotificationSettingsState(setNotificationSettingAction = AsyncAction.Failure(Throwable("error"))),
            aRoomNotificationSettingsState(restoreDefaultAction = AsyncAction.Loading),
            aRoomNotificationSettingsState(restoreDefaultAction = AsyncAction.Failure(Throwable("error"))),
            aRoomNotificationSettingsState(displayMentionsOnlyDisclaimer = true)
        )

    private fun aRoomNotificationSettingsState(
        isDefault: Boolean = true,
        setNotificationSettingAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
        restoreDefaultAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
        displayMentionsOnlyDisclaimer: Boolean = false,
    ): RoomNotificationSettingsState {
        return RoomNotificationSettingsState(
            showUserDefinedSettingStyle = false,
            roomName = "Room 1",
            AsyncData.Success(aRoomNotificationSettings(
                mode = RoomNotificationMode.MUTE,
                isDefault = isDefault
            )),
            pendingRoomNotificationMode = null,
            pendingSetDefault = null,
            defaultRoomNotificationMode = RoomNotificationMode.ALL_MESSAGES,
            setNotificationSettingAction = setNotificationSettingAction,
            restoreDefaultAction = restoreDefaultAction,
            displayMentionsOnlyDisclaimer = displayMentionsOnlyDisclaimer,
            eventSink = { },
        )
    }
}
