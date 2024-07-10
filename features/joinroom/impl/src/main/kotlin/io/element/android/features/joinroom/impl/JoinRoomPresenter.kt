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

package io.element.android.features.joinroom.impl

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.response.AcceptDeclineInviteEvents
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.InviteData
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
import io.element.android.libraries.matrix.api.room.MatrixRoomInfo
import io.element.android.libraries.matrix.api.room.RoomType
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.room.preview.RoomPreview
import io.element.android.libraries.matrix.ui.model.toInviteSender
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
    private val acceptDeclineInvitePresenter: Presenter<AcceptDeclineInviteState>,
    private val buildMeta: BuildMeta,
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
        val roomInfo by matrixClient.getRoomInfoFlow(roomId).collectAsState(initial = Optional.empty())
        val joinAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val knockAction: MutableState<AsyncAction<Unit>> = remember { mutableStateOf(AsyncAction.Uninitialized) }
        val contentState by produceState<ContentState>(
            initialValue = ContentState.Loading(roomIdOrAlias),
            key1 = roomInfo,
            key2 = retryCount,
        ) {
            when {
                roomInfo.isPresent -> {
                    value = roomInfo.get().toContentState()
                }
                roomDescription.isPresent -> {
                    value = roomDescription.get().toContentState()
                }
                else -> {
                    value = ContentState.Loading(roomIdOrAlias)
                    val result = matrixClient.getRoomPreviewFromRoomId(roomId, serverNames)
                    value = result.fold(
                        onSuccess = { roomPreview ->
                            roomPreview.toContentState()
                        },
                        onFailure = { throwable ->
                            if (throwable.message?.contains("403") == true) {
                                ContentState.UnknownRoom(roomIdOrAlias)
                            } else {
                                ContentState.Failure(roomIdOrAlias, throwable)
                            }
                        }
                    )
                }
            }
        }
        val acceptDeclineInviteState = acceptDeclineInvitePresenter.present()

        fun handleEvents(event: JoinRoomEvents) {
            when (event) {
                JoinRoomEvents.JoinRoom -> coroutineScope.joinRoom(joinAction)
                JoinRoomEvents.KnockRoom -> coroutineScope.knockRoom(knockAction)
                JoinRoomEvents.AcceptInvite -> {
                    val inviteData = contentState.toInviteData() ?: return
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.AcceptInvite(inviteData)
                    )
                }
                JoinRoomEvents.DeclineInvite -> {
                    val inviteData = contentState.toInviteData() ?: return
                    acceptDeclineInviteState.eventSink(
                        AcceptDeclineInviteEvents.DeclineInvite(inviteData)
                    )
                }
                JoinRoomEvents.RetryFetchingContent -> {
                    retryCount++
                }
                JoinRoomEvents.ClearError -> {
                    knockAction.value = AsyncAction.Uninitialized
                    joinAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return JoinRoomState(
            contentState = contentState,
            acceptDeclineInviteState = acceptDeclineInviteState,
            joinAction = joinAction.value,
            knockAction = knockAction.value,
            applicationName = buildMeta.applicationName,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.joinRoom(joinAction: MutableState<AsyncAction<Unit>>) = launch {
        joinAction.runUpdatingState {
            joinRoom.invoke(
                roomId = roomId,
                serverNames = serverNames,
                trigger = trigger
            )
        }
    }

    private fun CoroutineScope.knockRoom(knockAction: MutableState<AsyncAction<Unit>>) = launch {
        knockAction.runUpdatingState {
            knockRoom(roomId)
        }
    }
}

private fun RoomPreview.toContentState(): ContentState {
    return ContentState.Loaded(
        roomId = roomId,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = numberOfJoinedMembers,
        isDm = false,
        roomType = roomType,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when {
            // Note when isInvited, roomInfo will be used, so if this happen, it will be temporary.
            isInvited -> JoinAuthorisationStatus.IsInvited(null)
            canKnock -> JoinAuthorisationStatus.CanKnock
            isPublic -> JoinAuthorisationStatus.CanJoin
            else -> JoinAuthorisationStatus.Unknown
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
internal fun MatrixRoomInfo.toContentState(): ContentState {
    return ContentState.Loaded(
        roomId = id,
        name = name,
        topic = topic,
        alias = canonicalAlias,
        numberOfMembers = activeMembersCount,
        isDm = isDm,
        roomType = if (isSpace) RoomType.Space else RoomType.Room,
        roomAvatarUrl = avatarUrl,
        joinAuthorisationStatus = when {
            currentUserMembership == CurrentUserMembership.INVITED -> JoinAuthorisationStatus.IsInvited(
                inviteSender = inviter?.toInviteSender()
            )
            isPublic -> JoinAuthorisationStatus.CanJoin
            else -> JoinAuthorisationStatus.Unknown
        }
    )
}

@VisibleForTesting
internal fun ContentState.toInviteData(): InviteData? {
    return when (this) {
        is ContentState.Loaded -> InviteData(
            roomId = roomId,
            // Note: name should not be null at this point, but use Id just in case...
            roomName = name ?: roomId.value,
            isDm = isDm
        )
        else -> null
    }
}
