/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.anAcceptDeclineInviteState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.ui.model.InviteSender

open class JoinRoomStateProvider : PreviewParameterProvider<JoinRoomState> {
    override val values: Sequence<JoinRoomState>
        get() = sequenceOf(
            aJoinRoomState(
                contentState = aLoadingContentState()
            ),
            aJoinRoomState(
                contentState = anUnknownContentState()
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    name = null,
                    alias = null,
                    topic = null,
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin)
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    joinAuthorisationStatus = JoinAuthorisationStatus.CanKnock,
                    topic = "lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt" +
                        " ut labore et dolore magna aliqua ut enim ad minim veniam quis nostrud exercitation ullamco" +
                        " laboris nisi ut aliquip ex ea commodo consequat duis aute irure dolor in reprehenderit in" +
                        " voluptate velit esse cillum dolore eu fugiat nulla pariatur excepteur sint occaecat cupidatat" +
                        " non proident sunt in culpa qui officia deserunt mollit anim id est laborum",
                    numberOfMembers = 888,
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(null))
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    numberOfMembers = 123,
                    joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(anInviteSender()),
                )
            ),
            aJoinRoomState(
                contentState = aFailureContentState()
            ),
            aJoinRoomState(
                contentState = aFailureContentState(roomIdOrAlias = A_ROOM_ALIAS.toRoomIdOrAlias())
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    roomId = RoomId("!aSpaceId:domain"),
                    name = "A space",
                    alias = null,
                    topic = "This is the topic of a space",
                    roomType = RoomType.Space,
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    name = "A DM",
                    isDm = true,
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    name = "A knocked Room",
                    joinAuthorisationStatus = JoinAuthorisationStatus.IsKnocked
                )
            )
        )
}

fun aFailureContentState(
    roomIdOrAlias: RoomIdOrAlias = A_ROOM_ID.toRoomIdOrAlias()
): ContentState {
    return ContentState.Failure(
        roomIdOrAlias = roomIdOrAlias,
        error = Exception("Error"),
    )
}

fun anUnknownContentState(roomId: RoomId = A_ROOM_ID) = ContentState.UnknownRoom(roomId.toRoomIdOrAlias())

fun aLoadingContentState(roomId: RoomId = A_ROOM_ID) = ContentState.Loading(roomId.toRoomIdOrAlias())

fun aLoadedContentState(
    roomId: RoomId = A_ROOM_ID,
    name: String? = "Element X android",
    alias: RoomAlias? = RoomAlias("#exa:matrix.org"),
    topic: String? = "Element X is a secure, private and decentralized messenger.",
    numberOfMembers: Long? = null,
    isDm: Boolean = false,
    roomType: RoomType = RoomType.Room,
    roomAvatarUrl: String? = null,
    joinAuthorisationStatus: JoinAuthorisationStatus = JoinAuthorisationStatus.Unknown
) = ContentState.Loaded(
    roomId = roomId,
    name = name,
    alias = alias,
    topic = topic,
    numberOfMembers = numberOfMembers,
    isDm = isDm,
    roomType = roomType,
    roomAvatarUrl = roomAvatarUrl,
    joinAuthorisationStatus = joinAuthorisationStatus
)

fun aJoinRoomState(
    contentState: ContentState = aLoadedContentState(),
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    joinAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    knockAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    cancelKnockAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    knockMessage: String = "",
    eventSink: (JoinRoomEvents) -> Unit = {}
) = JoinRoomState(
    contentState = contentState,
    acceptDeclineInviteState = acceptDeclineInviteState,
    joinAction = joinAction,
    knockAction = knockAction,
    cancelKnockAction = cancelKnockAction,
    applicationName = "AppName",
    knockMessage = knockMessage,
    eventSink = eventSink
)

internal fun anInviteSender(
    userId: UserId = UserId("@bob:domain"),
    displayName: String = "Bob",
    avatarData: AvatarData = AvatarData(userId.value, displayName, size = AvatarSize.InviteSender),
) = InviteSender(
    userId = userId,
    displayName = displayName,
    avatarData = avatarData,
)

private val A_ROOM_ID = RoomId("!exa:matrix.org")
private val A_ROOM_ALIAS = RoomAlias("#exa:matrix.org")
