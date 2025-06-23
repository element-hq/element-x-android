/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
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
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.exception.ErrorKind
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo
import io.element.android.libraries.matrix.ui.model.toInviteSender
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Optional

class JoinRoomPresenter @AssistedInject constructor(
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
    private val appPreferencesStore: AppPreferencesStore,
    private val seenInvitesStore: SeenInvitesStore,
) : Presenter<JoinRoomState> {
    interface Factory {
        fun create(
            roomId: RoomId,
            roomIdOrAlias: RoomIdOrAlias,
            roomDescription: Optional<RoomDescription>,
            serverNames: List<String>,
            trigger: JoinedRoom.Trigger,
        ): JoinRoomPresenter
    }

    @Composable
    override fun present(): JoinRoomState {
        val coroutineScope = rememberCoroutineScope()
        var retryCount by remember { mutableIntStateOf(0) }
        val roomInfo by remember {
            matrixClient.getRoomInfoFlow(roomId)
        }.collectAsState(initial = Optional.empty())
        val joinAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val knockAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val cancelKnockAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val forgetRoomAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        var knockMessage by rememberSaveable { mutableStateOf("") }
        var isDismissingContent by remember { mutableStateOf(false) }
        val hideInviteAvatars by remember {
            appPreferencesStore.getHideInviteAvatarsFlow()
        }.collectAsState(initial = false)
        val canReportRoom by produceState(false) { value = matrixClient.canReportRoom() }

        val contentState by produceState<ContentState>(
            initialValue = ContentState.Loading,
            key1 = roomInfo,
            key2 = retryCount,
            key3 = isDismissingContent,
        ) {
            when {
                isDismissingContent -> value = ContentState.Dismissing
                roomInfo.isPresent -> {
                    val notJoinedRoom = matrixClient.getRoomPreview(roomIdOrAlias, serverNames).getOrNull()
                    val (sender, reason) = when (roomInfo.get().currentUserMembership) {
                        CurrentUserMembership.BANNED -> {
                            // Workaround to get info about the sender for banned rooms
                            // TODO re-do this once we have a better API in the SDK
                            val membershipDetails = notJoinedRoom?.membershipDetails()?.getOrNull()
                            membershipDetails?.senderMember to membershipDetails?.currentUserMember?.membershipChangeReason
                        }
                        CurrentUserMembership.INVITED -> {
                            roomInfo.get().inviter to null
                        }
                        else -> null to null
                    }
                    val joinedMembersCountOverride = notJoinedRoom?.previewInfo?.numberOfJoinedMembers
                    value = roomInfo.get().toContentState(
                        membershipSender = sender,
                        joinedMembersCountOverride = joinedMembersCountOverride,
                        reason = reason,
                    )
                }
                roomDescription.isPresent -> {
                    value = roomDescription.get().toContentState()
                }
                else -> {
                    value = ContentState.Loading
                    val result = matrixClient.getRoomPreview(roomIdOrAlias, serverNames)
                    value = result.fold(
                        onSuccess = { preview ->
                            val membershipInfo = when (preview.previewInfo.membership) {
                                CurrentUserMembership.INVITED,
                                CurrentUserMembership.BANNED,
                                CurrentUserMembership.KNOCKED -> {
                                    preview.membershipDetails().getOrNull()
                                }
                                else -> null
                            }
                            preview.previewInfo.toContentState(
                                senderMember = membershipInfo?.senderMember,
                                reason = membershipInfo?.currentUserMember?.membershipChangeReason,
                            )
                        },
                        onFailure = { throwable ->
                            if (throwable is ClientException.MatrixApi && (throwable.kind == ErrorKind.NotFound || throwable.kind == ErrorKind.Forbidden)) {
                                ContentState.UnknownRoom
                            } else {
                                ContentState.Failure(throwable)
                            }
                        }
                    )
                }
            }
        }
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        LaunchedEffect(contentState) {
            contentState.markRoomInviteAsSeen()
        }

        fun handleEvents(event: JoinRoomEvents) {
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
            eventSink = ::handleEvents
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

private fun RoomPreviewInfo.toContentState(senderMember: RoomMember?, reason: String?): ContentState {
    return ContentState.Loaded(
        roomId = roomId,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = numberOfJoinedMembers,
        isDm = false,
        roomType = roomType,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when (membership) {
            CurrentUserMembership.INVITED -> {
                JoinAuthorisationStatus.IsInvited(
                    inviteData = toInviteData(),
                    inviteSender = senderMember?.toInviteSender()
                )
            }
            CurrentUserMembership.BANNED -> JoinAuthorisationStatus.IsBanned(senderMember?.toInviteSender(), reason)
            CurrentUserMembership.KNOCKED -> JoinAuthorisationStatus.IsKnocked
            else -> joinRule.toJoinAuthorisationStatus()
        }
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
        isDm = false,
        roomType = RoomType.Room,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when (joinRule) {
            RoomDescription.JoinRule.KNOCK -> JoinAuthorisationStatus.CanKnock
            RoomDescription.JoinRule.PUBLIC -> JoinAuthorisationStatus.CanJoin
            else -> JoinAuthorisationStatus.Unknown
        }
    )
}

@VisibleForTesting
internal fun RoomInfo.toContentState(
    membershipSender: RoomMember?,
    joinedMembersCountOverride: Long?,
    reason: String?,
): ContentState {
    return ContentState.Loaded(
        roomId = id,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = joinedMembersCountOverride ?: joinedMembersCount,
        isDm = isDm,
        roomType = if (isSpace) RoomType.Space else RoomType.Room,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when (currentUserMembership) {
            CurrentUserMembership.INVITED -> JoinAuthorisationStatus.IsInvited(
                inviteData = toInviteData(),
                inviteSender = membershipSender?.toInviteSender(),
            )
            CurrentUserMembership.BANNED -> JoinAuthorisationStatus.IsBanned(
                banSender = membershipSender?.toInviteSender(),
                reason = reason,
            )
            CurrentUserMembership.KNOCKED -> JoinAuthorisationStatus.IsKnocked
            else -> joinRule.toJoinAuthorisationStatus()
        }
    )
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
