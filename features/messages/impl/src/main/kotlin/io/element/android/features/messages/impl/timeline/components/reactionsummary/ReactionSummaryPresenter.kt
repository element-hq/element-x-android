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

package io.element.android.features.messages.impl.timeline.components.reactionsummary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class ReactionSummaryPresenter @Inject constructor(
    private val room: MatrixRoom,
) : Presenter<ReactionSummaryState> {
    @Composable
    override fun present(): ReactionSummaryState {
        val membersState by room.membersStateFlow.collectAsState()

        val target: MutableState<ReactionSummaryState.Summary?> = remember {
            mutableStateOf(null)
        }
        val targetWithAvatars = populateSenderAvatars(members = membersState.roomMembers().orEmpty().toImmutableList(), summary = target.value)

        fun handleEvents(event: ReactionSummaryEvents) {
            when (event) {
                is ReactionSummaryEvents.ShowReactionSummary -> target.value = ReactionSummaryState.Summary(
                    reactions = event.reactions,
                    selectedKey = event.selectedKey,
                    selectedEventId = event.eventId
                )
                ReactionSummaryEvents.Clear -> target.value = null
            }
        }
        return ReactionSummaryState(
            target = targetWithAvatars.value,
            eventSink = { handleEvents(it) }
        )
    }

    @Composable
    private fun populateSenderAvatars(members: ImmutableList<RoomMember>, summary: ReactionSummaryState.Summary?) = remember(summary) {
        derivedStateOf {
            summary?.let { summary ->
                summary.copy(reactions = summary.reactions.map { reaction ->
                    reaction.copy(senders = reaction.senders.map { sender ->
                        val member = members.firstOrNull { it.userId == sender.senderId }
                        val user = MatrixUser(
                            userId = sender.senderId,
                            displayName = member?.displayName,
                            avatarUrl = member?.avatarUrl
                        )
                        sender.copy(user = user)
                    })
                })
            }
        }
    }
}
