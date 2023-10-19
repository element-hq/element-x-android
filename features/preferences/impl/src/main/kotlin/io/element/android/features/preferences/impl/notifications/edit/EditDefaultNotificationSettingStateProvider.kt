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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails

open class EditDefaultNotificationSettingStateProvider: PreviewParameterProvider<EditDefaultNotificationSettingState> {
    override val values: Sequence<EditDefaultNotificationSettingState>
        get() = sequenceOf(
            anEditDefaultNotificationSettingsState(),
        )
}

fun anEditDefaultNotificationSettingsState() = EditDefaultNotificationSettingState(
    isOneToOne = false,
    mode = RoomNotificationMode.MENTIONS_AND_KEYWORDS_ONLY,
    roomsWithUserDefinedMode = listOf(aRoomSummary()),
    changeNotificationSettingAction = Async.Uninitialized,
    eventSink = {}
)

private fun aRoomSummary() = RoomSummary.Filled(
    RoomSummaryDetails(
        roomId = RoomId("!roomId:domain"),
        name = "Room",
        avatarURLString = null,
        isDirect = false,
        lastMessage = null,
        lastMessageTimestamp = null,
        unreadNotificationCount = 0,
    )
)
