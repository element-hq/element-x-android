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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsState.ConfirmationDialog
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.coroutines.launch

class RoomMemberDetailsPresenter @AssistedInject constructor(
    private val currentUserSessionId: SessionId,
    private val room: MatrixRoom,
    @Assisted private val roomMember: RoomMember,
) : Presenter<RoomMemberDetailsState> {

    interface Factory {
        fun create(roomMember: RoomMember): RoomMemberDetailsPresenter
    }

    @Composable
    override fun present(): RoomMemberDetailsState {
        val coroutineScope = rememberCoroutineScope()
        var confirmationDialog by remember { mutableStateOf<ConfirmationDialog?>(null) }
        var isBlocked by remember { mutableStateOf(roomMember.isIgnored) }

        fun handleEvents(event: RoomMemberDetailsEvents) {
            when (event) {
                is RoomMemberDetailsEvents.BlockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Block
                    } else {
                        coroutineScope.launch {
                            room.ignoreUser(roomMember.userId).onSuccess { isBlocked = true }
                            confirmationDialog = null
                        }
                    }
                }
                is RoomMemberDetailsEvents.UnblockUser -> {
                    if (event.needsConfirmation) {
                        confirmationDialog = ConfirmationDialog.Unblock
                    } else {
                        coroutineScope.launch {
                            room.unignoreUser(roomMember.userId).onSuccess { isBlocked = false }
                            confirmationDialog = null
                        }
                    }
                }
                RoomMemberDetailsEvents.ClearConfirmationDialog -> confirmationDialog = null
            }
        }

        val userName by produceState(initialValue = roomMember.displayName) {
            room.userDisplayName(roomMember.userId).onSuccess { displayName ->
                if (displayName != null) value = displayName
            }
        }

        val userAvatar by produceState(initialValue = roomMember.avatarUrl) {
            room.userAvatarUrl(roomMember.userId).onSuccess { avatarUrl ->
                if (avatarUrl != null) value = avatarUrl
            }
        }

        return RoomMemberDetailsState(
            userId = roomMember.userId.value,
            userName = userName,
            avatarUrl = userAvatar,
            isBlocked = isBlocked,
            displayConfirmationDialog = confirmationDialog,
            isCurrentUser = roomMember.userId == currentUserSessionId,
            eventSink = ::handleEvents
        )
    }
}
