/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.response

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.ConfirmingDeclineInvite
import io.element.android.features.invite.api.response.InviteData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.push.api.notifications.NotificationCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AcceptDeclineInvitePresenter @Inject constructor(
    private val client: MatrixClient,
    private val joinRoom: JoinRoom,
    private val notificationCleaner: NotificationCleaner,
) : Presenter<AcceptDeclineInviteState> {
    @Composable
    override fun present(): AcceptDeclineInviteState {
        val localCoroutineScope = rememberCoroutineScope()
        val acceptedAction: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val declinedAction: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }

        fun handleEvents(event: AcceptDeclineInviteEvents) {
            when (event) {
                is AcceptDeclineInviteEvents.AcceptInvite -> {
                    val inviteData = event.invite
                    if (inviteData == null) {
                        acceptedAction.value = AsyncAction.Failure(InvalidDataException())
                    } else {
                        localCoroutineScope.acceptInvite(inviteData.roomId, acceptedAction)
                    }
                }

                is AcceptDeclineInviteEvents.DeclineInvite -> {
                    val inviteData = event.invite
                    if (inviteData == null) {
                        declinedAction.value = AsyncAction.Failure(InvalidDataException())
                    } else {
                        declinedAction.value = ConfirmingDeclineInvite(inviteData, event.blockUser)
                    }
                }

                is InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite -> {
                    when (val declinedActionValue = declinedAction.value) {
                        is ConfirmingDeclineInvite -> {
                            localCoroutineScope.declineInvite(
                                inviteData = declinedActionValue.inviteData,
                                declinedAction = declinedAction,
                                blockUser = declinedActionValue.blockUser,
                            )
                        }
                        else -> Unit
                    }
                }

                is InternalAcceptDeclineInviteEvents.CancelDeclineInvite -> {
                    declinedAction.value = AsyncAction.Uninitialized
                }

                is InternalAcceptDeclineInviteEvents.DismissAcceptError -> {
                    acceptedAction.value = AsyncAction.Uninitialized
                }

                is InternalAcceptDeclineInviteEvents.DismissDeclineError -> {
                    declinedAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return AcceptDeclineInviteState(
            acceptAction = acceptedAction.value,
            declineAction = declinedAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.acceptInvite(
        roomId: RoomId,
        acceptedAction: MutableState<AsyncAction<RoomId>>,
    ) = launch {
        acceptedAction.runUpdatingState {
            joinRoom(
                roomIdOrAlias = roomId.toRoomIdOrAlias(),
                serverNames = emptyList(),
                trigger = JoinedRoom.Trigger.Invite,
            )
                .onSuccess {
                    notificationCleaner.clearMembershipNotificationForRoom(client.sessionId, roomId)
                }
                .map { roomId }
        }
    }

    private fun CoroutineScope.declineInvite(
        inviteData: InviteData,
        blockUser: Boolean,
        declinedAction: MutableState<AsyncAction<RoomId>>,
    ) = launch {
        suspend {
            client.getPendingRoom(inviteData.roomId)?.use {
                it.leave().getOrThrow()
            }
            if (blockUser) {
                client.ignoreUser(inviteData.senderId).getOrThrow()
            }
            notificationCleaner.clearMembershipNotificationForRoom(client.sessionId, inviteData.roomId)
            inviteData.roomId
        }.runCatchingUpdatingState(declinedAction)
    }
}
