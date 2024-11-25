/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications.edit

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import kotlinx.collections.immutable.persistentListOf

open class EditDefaultNotificationSettingStateProvider : PreviewParameterProvider<EditDefaultNotificationSettingState> {
    override val values: Sequence<EditDefaultNotificationSettingState>
        get() = sequenceOf(
            anEditDefaultNotificationSettingsState(),
            anEditDefaultNotificationSettingsState(isOneToOne = true),
            anEditDefaultNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Loading),
            anEditDefaultNotificationSettingsState(changeNotificationSettingAction = AsyncAction.Failure(Throwable("error"))),
            anEditDefaultNotificationSettingsState(displayMentionsOnlyDisclaimer = true),
        )
}

private fun anEditDefaultNotificationSettingsState(
    isOneToOne: Boolean = false,
    changeNotificationSettingAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    displayMentionsOnlyDisclaimer: Boolean = false,
) = EditDefaultNotificationSettingState(
    isOneToOne = isOneToOne,
    mode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
    roomsWithUserDefinedMode = persistentListOf(
        anEditNotificationSettingRoomInfo("Room"),
        anEditNotificationSettingRoomInfo(null),
    ),
    changeNotificationSettingAction = changeNotificationSettingAction,
    displayMentionsOnlyDisclaimer = displayMentionsOnlyDisclaimer,
    eventSink = {}
)

private fun anEditNotificationSettingRoomInfo(
    name: String?,
) = EditNotificationSettingRoomInfo(
    roomId = RoomId("!roomId:domain"),
    name = name,
    avatarData = AvatarData(
        id = "!roomId:domain",
        name = name,
        url = null,
        size = AvatarSize.CustomRoomNotificationSetting,
    ),
    heroesAvatar = persistentListOf(),
    notificationMode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
)
