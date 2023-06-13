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

package io.element.android.features.roomdetails.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.MatrixRoomNotificationSettingsState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationSettings
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.ui.room.getDirectRoomMember
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class RoomDetailsPresenter @Inject constructor(
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val roomMembersDetailsPresenterFactory: RoomMemberDetailsPresenter.Factory,
    private val leaveRoomPresenter: LeaveRoomPresenter,
) : Presenter<RoomDetailsState> {

    @Composable
    override fun present(): RoomDetailsState {
        val scope = rememberCoroutineScope()
        val leaveRoomState = leaveRoomPresenter.present()
        val roomNotificationSettingsState by room.roomNotificationSettingsStateFlow.collectAsState()
        val roomNotificationSettings by getRoomNotificationSettings(roomNotificationSettingsState)
        LaunchedEffect(Unit) {
            room.updateMembers()
            room.roomNotificationSettingsStateFlow.onEach { roomNotificationSettingsState ->
                when (roomNotificationSettingsState) {
                    MatrixRoomNotificationSettingsState.ChangedNotificationSettings -> {
                        client.notificationSettingsService().getRoomNotificationMode(room.roomId)
                    }
                    is MatrixRoomNotificationSettingsState.Error -> TODO()
                    is MatrixRoomNotificationSettingsState.Pending -> TODO()
                    is MatrixRoomNotificationSettingsState.Ready -> TODO()
                    MatrixRoomNotificationSettingsState.Unknown -> TODO()
                }
            }.launchIn(scope)
        }

        val membersState by room.membersStateFlow.collectAsState()
        val canInvite by getCanInvite(membersState)
        val canEditName by getCanSendStateEvent(membersState, StateEventType.ROOM_NAME)
        val canEditAvatar by getCanSendStateEvent(membersState, StateEventType.ROOM_AVATAR)
        val canEditTopic by getCanSendStateEvent(membersState, StateEventType.ROOM_TOPIC)
        val dmMember by room.getDirectRoomMember(membersState)
        val roomMemberDetailsPresenter = roomMemberDetailsPresenter(dmMember)
        val roomType = getRoomType(dmMember)

        val topicState = remember(canEditTopic, room.topic) {
            val topic = room.topic

            when {
                !topic.isNullOrBlank() -> RoomTopicState.ExistingTopic(topic)
                canEditTopic -> RoomTopicState.CanAddTopic
                else -> RoomTopicState.Hidden
            }
        }

        fun handleEvents(event: RoomDetailsEvent) {
            when (event) {
                RoomDetailsEvent.LeaveRoom ->
                    leaveRoomState.eventSink(LeaveRoomEvent.ShowConfirmation(room.roomId))
                RoomDetailsEvent.MuteNotification -> {

                }
            }
        }

        val roomMemberDetailsState = roomMemberDetailsPresenter?.present()

        return RoomDetailsState(
            roomId = room.roomId.value,
            roomName = room.name ?: room.displayName,
            roomAlias = room.alias,
            roomAvatarUrl = room.avatarUrl,
            roomTopic = topicState,
            memberCount = room.joinedMemberCount,
            isEncrypted = room.isEncrypted,
            canInvite = canInvite,
            canEdit = canEditAvatar || canEditName || canEditTopic,
            roomType = roomType.value,
            roomMemberDetailsState = roomMemberDetailsState,
            leaveRoomState = leaveRoomState,
            roomNotificationSettings = roomNotificationSettings,
            eventSink = ::handleEvents,
        )
    }

    @Composable
    private fun roomMemberDetailsPresenter(dmMemberState: RoomMember?) = remember(dmMemberState) {
        dmMemberState?.let { roomMember ->
            roomMembersDetailsPresenterFactory.create(roomMember.userId)
        }
    }

    @Composable
    private fun getRoomType(dmMember: RoomMember?): State<RoomDetailsType> = remember(dmMember) {
        derivedStateOf {
            if (dmMember != null) {
                RoomDetailsType.Dm(dmMember)
            } else {
                RoomDetailsType.Room
            }
        }
    }

    @Composable
    private fun getCanInvite(membersState: MatrixRoomMembersState): State<Boolean> {
        val canInvite = remember(membersState) { mutableStateOf(false) }
        LaunchedEffect(membersState) {
            canInvite.value = room.canInvite().getOrElse { false }
        }
        return canInvite
    }

    @Composable
    private fun getCanSendStateEvent(membersState: MatrixRoomMembersState, type: StateEventType): State<Boolean> {
        val canSendEvent = remember(membersState) { mutableStateOf(false) }
        LaunchedEffect(membersState) {
            canSendEvent.value = room.canSendStateEvent(type).getOrElse { false }
        }
        return canSendEvent
    }

    @Composable
    private fun getRoomNotificationSettings(roomNotificationSettingsState: MatrixRoomNotificationSettingsState): State<Async<RoomNotificationSettings>> {
        return remember(roomNotificationSettingsState) {
            derivedStateOf {
                when (roomNotificationSettingsState) {
                    MatrixRoomNotificationSettingsState.Unknown -> Async.Uninitialized
                    MatrixRoomNotificationSettingsState.ChangedNotificationSettings -> TODO()
                    is MatrixRoomNotificationSettingsState.Pending -> Async.Loading(prevState = roomNotificationSettingsState.prevRoomNotificationSettings)
                    is MatrixRoomNotificationSettingsState.Error -> Async.Failure(roomNotificationSettingsState.failure, prevState = roomNotificationSettingsState.prevRoomNotificationSettings)
                    is MatrixRoomNotificationSettingsState.Ready -> Async.Success(roomNotificationSettingsState.roomNotificationSettings)
                }
            }
        }
    }
}
