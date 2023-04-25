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

package io.element.android.features.roomlist.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryPlaceholders
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.utils.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import io.element.android.libraries.ui.strings.R as StringR

open class RoomListStateProvider : PreviewParameterProvider<RoomListState> {
    override val values: Sequence<RoomListState>
        get() = sequenceOf(
            aRoomListState(),
            aRoomListState().copy(displayVerificationPrompt = true),
            aRoomListState().copy(snackbarMessage = SnackbarMessage(StringR.string.common_verification_complete)),
            aRoomListState().copy(hasNetworkConnection = false),
            aRoomListState().copy(displayInvites = true),
        )
}

internal fun aRoomListState() = RoomListState(
    matrixUser = MatrixUser(id = UserId("@id:domain"), username = "User#1", avatarData = AvatarData("@id:domain", "U")),
    roomList = aRoomListRoomSummaryList(),
    filter = "filter",
    hasNetworkConnection = true,
    snackbarMessage = null,
    displayVerificationPrompt = false,
    displayInvites = false,
    eventSink = {}
)

internal fun aRoomListRoomSummaryList(): ImmutableList<RoomListRoomSummary> {
    return persistentListOf(
        RoomListRoomSummary(
            name = "Room",
            hasUnread = true,
            timestamp = "14:18",
            lastMessage = "A very very very very long message which suites on two lines",
            avatarData = AvatarData("!id", "R"),
            id = "!roomId:domain",
            roomId = RoomId("!roomId:domain")
        ),
        RoomListRoomSummary(
            name = "Room#2",
            hasUnread = false,
            timestamp = "14:16",
            lastMessage = "A short message",
            avatarData = AvatarData("!id", "Z"),
            id = "!roomId2:domain",
            roomId = RoomId("!roomId2:domain")
        ),
        RoomListRoomSummaryPlaceholders.create("!roomId2:domain")
    )
}
