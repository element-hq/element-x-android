/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.InviteSender
import kotlinx.collections.immutable.toImmutableList

open class JoinRoomStateProvider : PreviewParameterProvider<JoinRoomState> {
    override val values: Sequence<JoinRoomState>
        get() = sequenceOf(
            aJoinRoomState(
                contentState = ContentState.Loading
            ),
            aJoinRoomState(
                contentState = ContentState.UnknownRoom
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
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin),
                joinAction = AsyncAction.Failure(JoinRoom.Failures.UnauthorizedJoin)
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(joinAuthorisationStatus = JoinAuthorisationStatus.CanJoin),
                joinAction = AsyncAction.Failure(ClientException.Generic("Something went wrong", null))
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(
                        inviteData = anInviteData(),
                        inviteSender = null,
                    )
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    numberOfMembers = 123,
                    joinAuthorisationStatus = JoinAuthorisationStatus.IsInvited(
                        inviteData = anInviteData(),
                        inviteSender = anInviteSender(),
                    ),
                )
            ),
            aJoinRoomState(
                contentState = aFailureContentState()
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    roomId = RoomId("!aSpaceId:domain"),
                    name = "A space",
                    alias = null,
                    topic = "This is the topic of a space",
                    details = aLoadedDetailsSpace(
                        childrenCount = 42,
                    ),
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    name = "A DM",
                    details = aLoadedDetailsRoom(
                        isDm = true,
                    ),
                )
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
                knockMessage = "Let me in please!",
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
                contentState = aLoadedContentState(
                    name = "A knocked Room",
                    joinAuthorisationStatus = JoinAuthorisationStatus.IsKnocked
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    name = "A private room",
                    joinAuthorisationStatus = JoinAuthorisationStatus.NeedInvite
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    name = "A banned room",
                    joinAuthorisationStatus = JoinAuthorisationStatus.IsBanned(
                        banSender = InviteSender(
                            userId = UserId("@alice:domain"),
                            displayName = "Alice",
                            avatarData = AvatarData("alice", "Alice", size = AvatarSize.InviteSender),
                            membershipChangeReason = "spamming"
                        ),
                        reason = "spamming",
                    ),
                )
            ),
            aJoinRoomState(
                contentState = aLoadedContentState(
                    name = "A restricted room",
                    joinAuthorisationStatus = JoinAuthorisationStatus.Restricted,
                )
            ),
        )
}

fun aFailureContentState(): ContentState {
    return ContentState.Failure(
        error = Exception("Error"),
    )
}

fun aLoadedContentState(
    roomId: RoomId = A_ROOM_ID,
    name: String? = "Element X android",
    alias: RoomAlias? = RoomAlias("#exa:matrix.org"),
    topic: String? = "Element X is a secure, private and decentralized messenger.",
    numberOfMembers: Long? = null,
    roomAvatarUrl: String? = null,
    joinAuthorisationStatus: JoinAuthorisationStatus = JoinAuthorisationStatus.Unknown,
    joinRule: JoinRule? = null,
    details: LoadedDetails = aLoadedDetailsRoom(isDm = false),
) = ContentState.Loaded(
    roomId = roomId,
    name = name,
    alias = alias,
    topic = topic,
    numberOfMembers = numberOfMembers,
    roomAvatarUrl = roomAvatarUrl,
    joinAuthorisationStatus = joinAuthorisationStatus,
    joinRule = joinRule,
    details = details,
)

fun aLoadedDetailsRoom(
    isDm: Boolean = false,
) = LoadedDetails.Room(
    isDm = isDm
)

fun aLoadedDetailsSpace(
    childrenCount: Int = 0,
    heroes: List<MatrixUser> = emptyList(),
) = LoadedDetails.Space(
    childrenCount = childrenCount,
    heroes = heroes.toImmutableList()
)

fun aJoinRoomState(
    roomIdOrAlias: RoomIdOrAlias = A_ROOM_ALIAS.toRoomIdOrAlias(),
    contentState: ContentState = aLoadedContentState(),
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
    joinAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    knockAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    forgetAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    cancelKnockAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    knockMessage: String = "",
    hideInviteAvatars: Boolean = false,
    canReportRoom: Boolean = true,
    eventSink: (JoinRoomEvents) -> Unit = {}
) = JoinRoomState(
    roomIdOrAlias = roomIdOrAlias,
    contentState = contentState,
    acceptDeclineInviteState = acceptDeclineInviteState,
    joinAction = joinAction,
    knockAction = knockAction,
    cancelKnockAction = cancelKnockAction,
    forgetAction = forgetAction,
    applicationName = "AppName",
    knockMessage = knockMessage,
    hideInviteAvatars = hideInviteAvatars,
    canReportRoom = canReportRoom,
    eventSink = eventSink
)

internal fun anInviteSender(
    userId: UserId = UserId("@bob:domain"),
    displayName: String = "Bob",
    avatarData: AvatarData = AvatarData(userId.value, displayName, size = AvatarSize.InviteSender),
    membershipChangeReason: String? = null,
) = InviteSender(
    userId = userId,
    displayName = displayName,
    avatarData = avatarData,
    membershipChangeReason = membershipChangeReason,
)

internal fun anInviteData(
    roomId: RoomId = A_ROOM_ID,
    roomName: String = "Room name",
    isDm: Boolean = false,
) = InviteData(
    roomId = roomId,
    roomName = roomName,
    isDm = isDm,
)

private val A_ROOM_ID = RoomId("!exa:matrix.org")
private val A_ROOM_ALIAS = RoomAlias("#exa:matrix.org")
