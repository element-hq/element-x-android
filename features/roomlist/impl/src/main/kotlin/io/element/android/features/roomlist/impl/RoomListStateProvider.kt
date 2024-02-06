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
import io.element.android.features.leaveroom.api.aLeaveRoomState
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.aRoomListRoomSummary
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class RoomListStateProvider : PreviewParameterProvider<RoomListState> {
    override val values: Sequence<RoomListState>
        get() = sequenceOf(
            aRoomListState(),
            aRoomListState().copy(displayVerificationPrompt = true),
            aRoomListState().copy(snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete)),
            aRoomListState().copy(hasNetworkConnection = false),
            aRoomListState().copy(invitesState = InvitesState.SeenInvites),
            aRoomListState().copy(invitesState = InvitesState.NewInvites),
            aRoomListState().copy(displaySearchResults = true, filter = "", filteredRoomList = persistentListOf()),
            aRoomListState().copy(displaySearchResults = true),
            aRoomListState().copy(
                contextMenu = RoomListState.ContextMenu.Shown(
                    roomId = RoomId("!aRoom:aDomain"),
                    roomName = "A nice room name",
                    isDm = false,
                    hasNewContent = false,
                )
            ),
            aRoomListState().copy(displayRecoveryKeyPrompt = true),
            aRoomListState().copy(roomList = AsyncData.Success(persistentListOf())),
            aRoomListState().copy(roomList = AsyncData.Loading(prevData = RoomListRoomSummaryFactory.createFakeList())),
            aRoomListState().copy(matrixUser = null, displayMigrationStatus = true),
        )
}

internal fun aRoomListState() = RoomListState(
    matrixUser = MatrixUser(userId = UserId("@id:domain"), displayName = "User#1"),
    showAvatarIndicator = false,
    roomList = AsyncData.Success(aRoomListRoomSummaryList()),
    filter = "filter",
    filteredRoomList = aRoomListRoomSummaryList(),
    hasNetworkConnection = true,
    snackbarMessage = null,
    displayVerificationPrompt = false,
    displayRecoveryKeyPrompt = false,
    invitesState = InvitesState.NoInvites,
    displaySearchResults = false,
    contextMenu = RoomListState.ContextMenu.Hidden,
    leaveRoomState = aLeaveRoomState(),
    displayMigrationStatus = false,
    eventSink = {}
)

internal fun aRoomListRoomSummaryList(): ImmutableList<RoomListRoomSummary> {
    return persistentListOf(
        aRoomListRoomSummary(
            name = "Room",
            numberOfUnreadMessages = 1,
            timestamp = "14:18",
            lastMessage = "A very very very very long message which suites on two lines",
            avatarData = AvatarData("!id", "R", size = AvatarSize.RoomListItem),
            id = "!roomId:domain",
        ),
        aRoomListRoomSummary(
            name = "Room#2",
            numberOfUnreadMessages = 0,
            timestamp = "14:16",
            lastMessage = "A short message",
            avatarData = AvatarData("!id", "Z", size = AvatarSize.RoomListItem),
            id = "!roomId2:domain",
        ),
        aRoomListRoomSummary(
            id = "!roomId3:domain",
            isPlaceholder = true,
        ),
        aRoomListRoomSummary(
            id = "!roomId4:domain",
            isPlaceholder = true,
        ),
    )
}
