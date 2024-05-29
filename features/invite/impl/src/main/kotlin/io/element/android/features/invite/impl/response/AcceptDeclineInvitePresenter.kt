/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.invite.impl.response

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.InviteData
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runCatchingUpdatingState
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

class AcceptDeclineInvitePresenter @Inject constructor(
    private val client: MatrixClient,
    private val joinRoom: JoinRoom,
    private val notificationDrawerManager: NotificationDrawerManager,
) : Presenter<AcceptDeclineInviteState> {
    @Composable
    override fun present(): AcceptDeclineInviteState {
        val localCoroutineScope = rememberCoroutineScope()
        val acceptedAction: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val declinedAction: MutableState<AsyncAction<RoomId>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        var currentInvite by remember {
            mutableStateOf<Optional<InviteData>>(Optional.empty())
        }

        fun handleEvents(event: AcceptDeclineInviteEvents) {
            when (event) {
                is AcceptDeclineInviteEvents.AcceptInvite -> {
                    // currentInvite is used to render the decline confirmation dialog
                    // and to reuse the roomId when the user confirm the rejection of the invitation.
                    // Just set it to empty here.
                    currentInvite = Optional.empty()
                    localCoroutineScope.acceptInvite(event.invite.roomId, acceptedAction)
                }

                is AcceptDeclineInviteEvents.DeclineInvite -> {
                    currentInvite = Optional.of(event.invite)
                    declinedAction.value = AsyncAction.Confirming
                }

                is InternalAcceptDeclineInviteEvents.ConfirmDeclineInvite -> {
                    declinedAction.value = AsyncAction.Uninitialized
                    currentInvite.getOrNull()?.let {
                        localCoroutineScope.declineInvite(it.roomId, declinedAction)
                    }
                    currentInvite = Optional.empty()
                }

                is InternalAcceptDeclineInviteEvents.CancelDeclineInvite -> {
                    currentInvite = Optional.empty()
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
            invite = currentInvite,
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
                roomId = roomId,
                serverNames = emptyList(),
                trigger = JoinedRoom.Trigger.Invite,
            )
                .onSuccess {
                    notificationDrawerManager.clearMembershipNotificationForRoom(client.sessionId, roomId)
                }
                .map { roomId }
        }
    }

    private fun CoroutineScope.declineInvite(roomId: RoomId, declinedAction: MutableState<AsyncAction<RoomId>>) = launch {
        suspend {
            client.getRoom(roomId)?.use {
                it.leave().getOrThrow()
                notificationDrawerManager.clearMembershipNotificationForRoom(client.sessionId, roomId)
            }
            roomId
        }.runCatchingUpdatingState(declinedAction)
    }
}
