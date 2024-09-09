/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.anAcceptDeclineInviteState
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.leaveroom.api.aLeaveRoomState
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.aDirectLogoutState
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.filters.aRoomListFiltersState
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.features.roomlist.impl.model.aRoomListRoomSummary
import io.element.android.features.roomlist.impl.model.anInviteSender
import io.element.android.features.roomlist.impl.search.RoomListSearchState
import io.element.android.features.roomlist.impl.search.aRoomListSearchState
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
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
            aRoomListState(contextMenu = aContextMenuShown(roomName = null)),
            aRoomListState(contextMenu = aContextMenuShown(roomName = "A nice room name")),
            aRoomListState(contextMenu = aContextMenuShown(isFavorite = true)),
            aRoomListState(contentState = aRoomsContentState(securityBannerState = SecurityBannerState.RecoveryKeyConfirmation)),
            aRoomListState(contentState = anEmptyContentState()),
            aRoomListState(contentState = aSkeletonContentState()),
            aRoomListState(searchState = aRoomListSearchState(isSearchActive = true, query = "Test")),
            aRoomListState(contentState = aRoomsContentState(securityBannerState = SecurityBannerState.SetUpRecovery)),
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
    filtersState: RoomListFiltersState = aRoomListFiltersState(),
    contentState: RoomListContentState = aRoomsContentState(),
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    directLogoutState: DirectLogoutState = aDirectLogoutState(),
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
    acceptDeclineInviteState = acceptDeclineInviteState,
    directLogoutState = directLogoutState,
    eventSink = eventSink,
)

internal fun aRoomListRoomSummaryList(): ImmutableList<RoomListRoomSummary> {
    return persistentListOf(
        aRoomListRoomSummary(
            name = "Room Invited",
            avatarData = AvatarData("!roomId", "Room with Alice and Bob", size = AvatarSize.RoomListItem),
            id = "!roomId:domain",
            inviteSender = anInviteSender(),
            displayType = RoomSummaryDisplayType.INVITE,
        ),
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
            displayType = RoomSummaryDisplayType.PLACEHOLDER,
        ),
        aRoomListRoomSummary(
            id = "!roomId4:domain",
            displayType = RoomSummaryDisplayType.PLACEHOLDER,
        ),
    )
}
