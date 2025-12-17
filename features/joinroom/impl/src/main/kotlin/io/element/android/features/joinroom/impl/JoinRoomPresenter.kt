/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.joinroom.impl

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.InviteData
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.toInviteData
import io.element.android.features.joinroom.impl.di.CancelKnockRoom
import io.element.android.features.joinroom.impl.di.ForgetRoom
import io.element.android.features.joinroom.impl.di.KnockRoom
import io.element.android.features.roomdirectory.api.RoomDescription
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMembershipDetails
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.model.toInviteSender
import io.element.android.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@AssistedInject
class JoinRoomPresenter(
    @Assisted private val roomId: RoomId,
    @Assisted private val roomIdOrAlias: RoomIdOrAlias,
    @Assisted private val roomDescription: Optional<RoomDescription>,
    @Assisted private val serverNames: List<String>,
    @Assisted private val trigger: JoinedRoom.Trigger,
    private val matrixClient: MatrixClient,
    private val joinRoom: JoinRoom,
    private val knockRoom: KnockRoom,
    private val cancelKnockRoom: CancelKnockRoom,
    private val forgetRoom: ForgetRoom,
    private val acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
    private val buildMeta: BuildMeta,
    private val seenInvitesStore: SeenInvitesStore,
) : Presenter<JoinRoomState> {
    fun interface Factory {
        fun create(
            roomId: RoomId,
            roomIdOrAlias: RoomIdOrAlias,
            roomDescription: Optional<RoomDescription>,
            serverNames: List<String>,
            trigger: JoinedRoom.Trigger,
        ): JoinRoomPresenter
    }

    private val spaceList = matrixClient.spaceService.spaceRoomList(roomId)

    @Composable
    override fun present(): JoinRoomState {
        val coroutineScope = rememberCoroutineScope()
        var retryCount by remember { mutableIntStateOf(0) }
        val roomInfo by remember {
            matrixClient.getRoomInfoFlow(roomId)
        }.collectAsState(initial = Optional.empty())
        val spaceRoom by spaceList.currentSpaceFlow.collectAsState()
        val joinAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val knockAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val cancelKnockAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val forgetRoomAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        var knockMessage by rememberSaveable { mutableStateOf("") }
        var isDismissingContent by remember { mutableStateOf(false) }
        val hideInviteAvatars by matrixClient.rememberHideInvitesAvatar()
        val canReportRoom by produceState(false) { value = matrixClient.canReportRoom() }

        var contentState by remember {
            mutableStateOf<ContentState>(ContentState.Loading)
        }
        LaunchedEffect(roomInfo, retryCount, isDismissingContent, spaceRoom) {
            when {
                isDismissingContent -> contentState = ContentState.Dismissing
                roomInfo.isPresent -> {
                    val notJoinedRoom = matrixClient.getRoomPreview(roomIdOrAlias, serverNames).getOrNull()
                    val membershipDetails = notJoinedRoom?.membershipDetails()?.getOrNull()
                    val joinedMembersCountOverride = notJoinedRoom?.previewInfo?.numberOfJoinedMembers
                    contentState = roomInfo.get().toContentState(
                        joinedMembersCountOverride = joinedMembersCountOverride,
                        membershipDetails = membershipDetails,
                        childrenCount = spaceRoom.getOrNull()?.childrenCount,
                    )
                }
                spaceRoom.isPresent -> {
                    val spaceRoom = spaceRoom.get()
                    // Only use this state when space is not locally known
                    contentState = if (spaceRoom.state != null) {
                        ContentState.Loading
                    } else {
                        spaceRoom.toContentState()
                    }
                }
                roomDescription.isPresent -> {
                    contentState = roomDescription.get().toContentState()
                }
                else -> {
                    contentState = ContentState.Loading
                    val result = matrixClient.getRoomPreview(roomIdOrAlias, serverNames)
                    contentState = result.fold(
                        onSuccess = { preview ->
                            val membershipDetails = preview.membershipDetails().getOrNull()
                            preview.previewInfo.toContentState(membershipDetails)
                        },
                        onFailure = { throwable ->
                            ContentState.UnknownRoom
                        }
                    )
                }
            }
        }
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        LaunchedEffect(contentState) {
            contentState.markRoomInviteAsSeen()
        }

        fun handleEvent(event: JoinRoomEvents) {
            when (event) {
                JoinRoomEvents.JoinRoom -> coroutineScope.joinRoom(joinAction)
                is JoinRoomEvents.KnockRoom -> coroutineScope.knockRoom(knockAction, knockMessage)
                is JoinRoomEvents.AcceptInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.AcceptInvite(event.inviteData)
                    )
                }
                is JoinRoomEvents.DeclineInvite -> {
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.DeclineInvite(invite = event.inviteData, blockUser = event.blockUser, shouldConfirm = true)
                    )
                }
                is JoinRoomEvents.CancelKnock -> coroutineScope.cancelKnockRoom(event.requiresConfirmation, cancelKnockAction)
                JoinRoomEvents.RetryFetchingContent -> {
                    retryCount++
                }
                JoinRoomEvents.ClearActionStates -> {
                    knockAction.value = AsyncAction.Uninitialized
                    joinAction.value = AsyncAction.Uninitialized
                    cancelKnockAction.value = AsyncAction.Uninitialized
                    forgetRoomAction.value = AsyncAction.Uninitialized
                }
                is JoinRoomEvents.UpdateKnockMessage -> {
                    knockMessage = event.message.take(MAX_KNOCK_MESSAGE_LENGTH)
                }
                JoinRoomEvents.DismissErrorAndHideContent -> {
                    isDismissingContent = true
                }
                JoinRoomEvents.ForgetRoom -> coroutineScope.forgetRoom(forgetRoomAction)
            }
        }

        return JoinRoomState(
            roomIdOrAlias = roomIdOrAlias,
            contentState = contentState,
            acceptDeclineInviteState = acceptDeclineInviteState,
            joinAction = joinAction.value,
            knockAction = knockAction.value,
            forgetAction = forgetRoomAction.value,
            cancelKnockAction = cancelKnockAction.value,
            applicationName = buildMeta.applicationName,
            knockMessage = knockMessage,
            hideInviteAvatars = hideInviteAvatars,
            canReportRoom = canReportRoom,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.joinRoom(joinAction: MutableState<AsyncAction<Unit>>) = launch {
        joinAction.runUpdatingState {
            joinRoom.invoke(
                roomIdOrAlias = roomIdOrAlias,
                serverNames = serverNames,
                trigger = trigger
            )
        }
    }

    private fun CoroutineScope.knockRoom(knockAction: MutableState<AsyncAction<Unit>>, message: String) = launch {
        knockAction.runUpdatingState {
            knockRoom(roomIdOrAlias, message, serverNames)
        }
    }

    private fun CoroutineScope.cancelKnockRoom(requiresConfirmation: Boolean, cancelKnockAction: MutableState<AsyncAction<Unit>>) = launch {
        if (requiresConfirmation) {
            cancelKnockAction.value = AsyncAction.ConfirmingNoParams
        } else {
            cancelKnockAction.runUpdatingState {
                cancelKnockRoom(roomId)
            }
        }
    }

    private fun CoroutineScope.forgetRoom(forgetAction: MutableState<AsyncAction<Unit>>) = launch {
        forgetAction.runUpdatingState {
            forgetRoom.invoke(roomId)
        }
    }

    private suspend fun ContentState.markRoomInviteAsSeen() {
        if ((this as? ContentState.Loaded)?.joinAuthorisationStatus as? JoinAuthorisationStatus.IsInvited != null) {
            seenInvitesStore.markAsSeen(roomId)
        }
    }
}

private fun RoomPreviewInfo.toContentState(membershipDetails: RoomMembershipDetails?): ContentState {
    return ContentState.Loaded(
        roomId = roomId,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = numberOfJoinedMembers,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = computeJoinAuthorisationStatus(
            membership,
            membershipDetails,
            joinRule,
            { toInviteData() }
        ),
        joinRule = joinRule,
        details = when (roomType) {
            is RoomType.Other,
            RoomType.Room -> LoadedDetails.Room(
                isDm = false,
            )
            RoomType.Space -> LoadedDetails.Space(
                childrenCount = 0,
                heroes = persistentListOf(),
            )
        }
    )
}

private fun SpaceRoom.toContentState(): ContentState {
    return ContentState.Loaded(
        roomId = roomId,
        name = displayName,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = numJoinedMembers.toLong(),
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = computeJoinAuthorisationStatus(
            membership = state,
            membershipDetails = null,
            joinRule = joinRule,
            inviteData = { toInviteData() }
        ),
        joinRule = joinRule,
        details = LoadedDetails.Space(
            childrenCount = childrenCount,
            heroes = heroes.toImmutableList(),
        )
    )
}

@VisibleForTesting
internal fun RoomDescription.toContentState(): ContentState {
    return ContentState.Loaded(
        roomId = roomId,
        name = name,
        topic = topic,
        alias = alias,
        numberOfMembers = numberOfMembers,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when (joinRule) {
            RoomDescription.JoinRule.KNOCK -> JoinAuthorisationStatus.CanKnock
            RoomDescription.JoinRule.PUBLIC -> JoinAuthorisationStatus.CanJoin
            else -> JoinAuthorisationStatus.Unknown
        },
        joinRule = when (joinRule) {
            RoomDescription.JoinRule.KNOCK -> JoinRule.Knock
            RoomDescription.JoinRule.PUBLIC -> JoinRule.Public
            RoomDescription.JoinRule.RESTRICTED -> JoinRule.Restricted(persistentListOf())
            RoomDescription.JoinRule.KNOCK_RESTRICTED -> JoinRule.KnockRestricted(persistentListOf())
            RoomDescription.JoinRule.INVITE -> JoinRule.Invite
            RoomDescription.JoinRule.UNKNOWN -> null
        },
        details = LoadedDetails.Room(isDm = false)
    )
}

@VisibleForTesting
internal fun RoomInfo.toContentState(
    joinedMembersCountOverride: Long?,
    membershipDetails: RoomMembershipDetails?,
    childrenCount: Int?,
): ContentState {
    return ContentState.Loaded(
        roomId = id,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = joinedMembersCountOverride ?: joinedMembersCount,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = computeJoinAuthorisationStatus(
            membership = currentUserMembership,
            membershipDetails = membershipDetails,
            joinRule = joinRule,
            inviteData = { toInviteData() }
        ),
        joinRule = joinRule,
        details = if (isSpace) {
            LoadedDetails.Space(
                childrenCount = childrenCount ?: 0,
                heroes = heroes,
            )
        } else {
            LoadedDetails.Room(
                isDm = isDm,
            )
        },
    )
}

private fun computeJoinAuthorisationStatus(
    membership: CurrentUserMembership?,
    membershipDetails: RoomMembershipDetails?,
    joinRule: JoinRule?,
    inviteData: () -> InviteData,
): JoinAuthorisationStatus {
    return when (membership) {
        CurrentUserMembership.INVITED -> {
            JoinAuthorisationStatus.IsInvited(
                inviteData = inviteData(),
                inviteSender = membershipDetails?.senderMember?.toInviteSender()
            )
        }
        CurrentUserMembership.BANNED -> JoinAuthorisationStatus.IsBanned(
            membershipDetails?.senderMember?.toInviteSender(),
            membershipDetails?.membershipChangeReason
        )
        CurrentUserMembership.KNOCKED -> JoinAuthorisationStatus.IsKnocked
        else -> joinRule.toJoinAuthorisationStatus()
    }
}

private fun JoinRule?.toJoinAuthorisationStatus(): JoinAuthorisationStatus {
    return when (this) {
        JoinRule.Knock,
        is JoinRule.KnockRestricted -> JoinAuthorisationStatus.CanKnock
        JoinRule.Invite,
        JoinRule.Private -> JoinAuthorisationStatus.NeedInvite
        is JoinRule.Restricted -> JoinAuthorisationStatus.Restricted
        JoinRule.Public -> JoinAuthorisationStatus.CanJoin
        else -> JoinAuthorisationStatus.Unknown
    }
}
