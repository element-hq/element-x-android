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

package io.element.android.features.messages.impl.typing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class TypingNotificationPresenter @Inject constructor(
    private val room: MatrixRoom,
) : Presenter<TypingNotificationState> {
    @Composable
    override fun present(): TypingNotificationState {
        var typingMembers by remember { mutableStateOf(emptyList<RoomMember>()) }
        LaunchedEffect(Unit) {
            combine(room.roomTypingMembersFlow, room.membersStateFlow) { typingMembers, membersState ->
                typingMembers
                    .map { userId ->
                        membersState.roomMembers()
                            ?.firstOrNull { roomMember -> roomMember.userId == userId }
                            ?: createDefaultRoomMemberForTyping(userId)
                    }
            }
                .distinctUntilChanged()
                .onEach { members ->
                    typingMembers = members
                }
                .launchIn(this)
        }

        return TypingNotificationState(
            typingMembers = typingMembers.toImmutableList(),
        )
    }
}

/**
 * Create a default [RoomMember] for typing events.
 * In this case, only the userId will be used for rendering, other fields are not used, but keep them
 * as close as possible to the actual data.
 */
private fun createDefaultRoomMemberForTyping(userId: UserId): RoomMember {
    return RoomMember(
        userId = userId,
        displayName = null,
        avatarUrl = null,
        membership = RoomMembershipState.JOIN,
        isNameAmbiguous = false,
        powerLevel = 0,
        normalizedPowerLevel = 0,
        isIgnored = false,
    )
}
