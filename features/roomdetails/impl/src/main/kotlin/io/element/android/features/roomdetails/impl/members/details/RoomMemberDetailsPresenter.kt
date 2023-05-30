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

package io.element.android.features.roomdetails.impl.members.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsState.ConfirmationDialog
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.room.getRoomMemberAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RoomMemberDetailsPresenter @AssistedInject constructor(
    private val client: MatrixClient,
    private val room: MatrixRoom,
    @Assisted private val roomMemberId: UserId,
) : Presenter<RoomMemberDetailsState> {

    interface Factory {
        fun create(roomMemberId: UserId): RoomMemberDetailsPresenter
    }

    @Composable
    override fun present(): RoomMemberDetailsState {
        val coroutineScope = rememberCoroutineScope()
        var confirmationDialog by remember { mutableStateOf<ConfirmationDialog?>(null) }
        val roomMember by room.getRoomMemberAsState(roomMemberId)
        // the room member is not really live...
        val isBlocked = remember {
            mutableStateOf(roomMember?.isIgnored.orFalse())
        }
        LaunchedEffect(Unit) {
            room.updateMembers()
        }

        fun handleEvents(event: RoomMemberDetailsEvents) {
            when (event) {
                is RoomMemberDetailsEvents.BlockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Block
                    } else {
                        confirmationDialog = null
                        coroutineScope.blockUser(roomMemberId, isBlocked)
                    }
                }
                is RoomMemberDetailsEvents.UnblockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Unblock
                    } else {
                        confirmationDialog = null
                        coroutineScope.unblockUser(roomMemberId, isBlocked)
                    }
                }
                RoomMemberDetailsEvents.ClearConfirmationDialog -> confirmationDialog = null
            }
        }

        val userName by produceState(initialValue = roomMember?.displayName) {
            room.userDisplayName(roomMemberId).onSuccess { displayName ->
                if (displayName != null) value = displayName
            }
        }

        val userAvatar by produceState(initialValue = roomMember?.avatarUrl) {
            room.userAvatarUrl(roomMemberId).onSuccess { avatarUrl ->
                if (avatarUrl != null) value = avatarUrl
            }
        }

        return RoomMemberDetailsState(
            userId = roomMemberId.value,
            userName = userName,
            avatarUrl = userAvatar,
            isBlocked = isBlocked.value,
            displayConfirmationDialog = confirmationDialog,
            isCurrentUser = roomMember?.userId == client.sessionId,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.blockUser(userId: UserId, isBlockedState: MutableState<Boolean>) = launch {
        client.ignoreUser(userId)
            .map {
                isBlockedState.value = true
                room.updateMembers()
            }

    }

    private fun CoroutineScope.unblockUser(userId: UserId, isBlockedState: MutableState<Boolean>) = launch {
        client.unignoreUser(userId)
            .map {
                isBlockedState.value = false
                room.updateMembers()
            }
    }
}
