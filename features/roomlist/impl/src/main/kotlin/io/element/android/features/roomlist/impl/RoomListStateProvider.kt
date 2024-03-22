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
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.leaveroom.api.aLeaveRoomState
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.filters.aRoomListFiltersState
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.aRoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchState
import io.element.android.features.roomlist.impl.search.aRoomListSearchState
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
            aRoomListState(snackbarMessage = SnackbarMessage(CommonStrings.common_verification_complete)),
            aRoomListState(hasNetworkConnection = false),
            aRoomListState(contentState = aRoomsContentState(invitesState = InvitesState.SeenInvites)),
            aRoomListState(contentState = aRoomsContentState(invitesState = InvitesState.NewInvites)),
            aRoomListState(contextMenu = aContextMenuShown(roomName = "A nice room name")),
            aRoomListState(contextMenu = aContextMenuShown(isFavorite = true)),
            aRoomListState(contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation)),
            aRoomListState(contentState = anEmptyContentState()),
            aRoomListState(contentState = aSkeletonContentState()),
            aRoomListState(matrixUser = MatrixUser(userId = UserId("@id:domain")), contentState = aMigrationContentState()),
            aRoomListState(searchState = aRoomListSearchState(isSearchActive = true, query = "Test")),
            aRoomListState(filtersState = aRoomListFiltersState(isFeatureEnabled = true)),
        )
}

internal fun aRoomListState(
    matrixUser: MatrixUser = MatrixUser(userId = UserId("@id:domain"), displayName = "User#1"),
    showAvatarIndicator: Boolean = false,
    hasNetworkConnection: Boolean = true,
    snackbarMessage: SnackbarMessage? = null,
    contextMenu: RoomListState.ContextMenu = RoomListState.ContextMenu.Hidden,
    leaveRoomState: LeaveRoomState = aLeaveRoomState(),
    searchState: RoomListSearchState = aRoomListSearchState(),
    filtersState: RoomListFiltersState = aRoomListFiltersState(isFeatureEnabled = false),
    contentState: RoomListContentState = aRoomsContentState(),
    eventSink: (RoomListEvents) -> Unit = {}
) = RoomListState(
    matrixUser = matrixUser,
    showAvatarIndicator = showAvatarIndicator,
    hasNetworkConnection = hasNetworkConnection,
    snackbarMessage = snackbarMessage,
    contextMenu = contextMenu,
    leaveRoomState = leaveRoomState,
    filtersState = filtersState,
    searchState = searchState,
    contentState = contentState,
    eventSink = eventSink,
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

internal fun aContextMenuShown(
    roomName: String = "aRoom",
    isDm: Boolean = false,
    hasNewContent: Boolean = false,
    isFavorite: Boolean = false,
) = RoomListState.ContextMenu.Shown(
    roomId = RoomId("!aRoom:aDomain"),
    roomName = roomName,
    isDm = isDm,
    markAsUnreadFeatureFlagEnabled = true,
    hasNewContent = hasNewContent,
    isFavorite = isFavorite,
)
