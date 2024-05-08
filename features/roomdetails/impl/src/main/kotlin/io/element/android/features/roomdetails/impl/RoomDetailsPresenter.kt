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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.canInvite
import io.element.android.libraries.matrix.api.room.powerlevels.canSendState
import io.element.android.libraries.matrix.api.room.roomNotificationSettings
import io.element.android.libraries.matrix.ui.room.canCall
import io.element.android.libraries.matrix.ui.room.getDirectRoomMember
import io.element.android.libraries.matrix.ui.room.isOwnUserAdmin
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomDetailsPresenter @Inject constructor(
    private val client: MatrixClient,
    private val room: MatrixRoom,
    private val featureFlagService: FeatureFlagService,
    private val notificationSettingsService: NotificationSettingsService,
    private val roomMembersDetailsPresenterFactory: RoomMemberDetailsPresenter.Factory,
    private val leaveRoomPresenter: LeaveRoomPresenter,
    private val dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
) : Presenter<RoomDetailsState> {
    @Composable
    override fun present(): RoomDetailsState {
        val scope = rememberCoroutineScope()
        val leaveRoomState = leaveRoomPresenter.present()
        val canShowNotificationSettings = remember { mutableStateOf(false) }
        val roomInfo by room.roomInfoFlow.collectAsState(initial = null)
        val isUserAdmin = room.isOwnUserAdmin()

        val roomAvatar by remember { derivedStateOf { roomInfo?.avatarUrl ?: room.avatarUrl } }

        val roomName by remember { derivedStateOf { (roomInfo?.name ?: room.name ?: room.displayName).trim() } }
        val roomTopic by remember { derivedStateOf { roomInfo?.topic ?: room.topic } }
        val isFavorite by remember { derivedStateOf { roomInfo?.isFavorite.orFalse() } }
        val isPublic by remember { derivedStateOf { roomInfo?.isPublic.orFalse() } }

        LaunchedEffect(Unit) {
            canShowNotificationSettings.value = featureFlagService.isFeatureEnabled(FeatureFlags.NotificationSettings)
            if (canShowNotificationSettings.value) {
                room.updateRoomNotificationSettings()
                observeNotificationSettings()
            }
        }

        val syncUpdateTimestamp by room.syncUpdateFlow.collectAsState()

        val membersState by room.membersStateFlow.collectAsState()
        val canInvite by getCanInvite(membersState)
        val canEditName by getCanSendState(membersState, StateEventType.ROOM_NAME)
        val canEditAvatar by getCanSendState(membersState, StateEventType.ROOM_AVATAR)
        val canEditTopic by getCanSendState(membersState, StateEventType.ROOM_TOPIC)
        val canJoinCall by room.canCall(updateKey = syncUpdateTimestamp)
        val dmMember by room.getDirectRoomMember(membersState)
        val roomMemberDetailsPresenter = roomMemberDetailsPresenter(dmMember)
        val roomType by getRoomType(dmMember)

        val topicState = remember(canEditTopic, roomTopic, roomType) {
            val topic = roomTopic

            when {
                !topic.isNullOrBlank() -> RoomTopicState.ExistingTopic(topic)
                canEditTopic && roomType is RoomDetailsType.Room -> RoomTopicState.CanAddTopic
                else -> RoomTopicState.Hidden
            }
        }

        val roomNotificationSettingsState by room.roomNotificationSettingsStateFlow.collectAsState()

        fun handleEvents(event: RoomDetailsEvent) {
            when (event) {
                RoomDetailsEvent.LeaveRoom ->
                    leaveRoomState.eventSink(LeaveRoomEvent.ShowConfirmation(room.roomId))
                RoomDetailsEvent.MuteNotification -> {
                    scope.launch(dispatchers.io) {
                        client.notificationSettingsService().muteRoom(room.roomId)
                    }
                }
                RoomDetailsEvent.UnmuteNotification -> {
                    scope.launch(dispatchers.io) {
                        client.notificationSettingsService().unmuteRoom(room.roomId, room.isEncrypted, room.isOneToOne)
                    }
                }
                is RoomDetailsEvent.SetFavorite -> scope.setFavorite(event.isFavorite)
            }
        }

        val roomMemberDetailsState = roomMemberDetailsPresenter?.present()

        return RoomDetailsState(
            roomId = room.roomId,
            roomName = roomName,
            roomAlias = room.alias,
            roomAvatarUrl = roomAvatar,
            roomTopic = topicState,
            memberCount = room.joinedMemberCount,
            isEncrypted = room.isEncrypted,
            canInvite = canInvite,
            canEdit = (canEditAvatar || canEditName || canEditTopic) && roomType == RoomDetailsType.Room,
            canShowNotificationSettings = canShowNotificationSettings.value,
            canCall = canJoinCall,
            roomType = roomType,
            roomMemberDetailsState = roomMemberDetailsState,
            leaveRoomState = leaveRoomState,
            roomNotificationSettings = roomNotificationSettingsState.roomNotificationSettings(),
            isFavorite = isFavorite,
            displayRolesAndPermissionsSettings = !room.isDm && isUserAdmin,
            isPublic = isPublic,
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
    private fun getCanInvite(membersState: MatrixRoomMembersState) = produceState(false, membersState) {
        value = room.canInvite().getOrElse { false }
    }

    @Composable
    private fun getCanSendState(membersState: MatrixRoomMembersState, type: StateEventType) = produceState(false, membersState) {
        value = room.canSendState(type).getOrElse { false }
    }

    private fun CoroutineScope.observeNotificationSettings() {
        notificationSettingsService.notificationSettingsChangeFlow.onEach {
            room.updateRoomNotificationSettings()
        }.launchIn(this)
    }

    private fun CoroutineScope.setFavorite(isFavorite: Boolean) = launch {
        room.setIsFavorite(isFavorite)
            .onSuccess {
                analyticsService.captureInteraction(Interaction.Name.MobileRoomFavouriteToggle)
            }
    }
}
