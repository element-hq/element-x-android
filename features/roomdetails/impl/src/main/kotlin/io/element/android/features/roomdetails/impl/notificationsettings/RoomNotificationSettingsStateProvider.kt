/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
