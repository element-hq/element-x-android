/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class ReactionSummaryPresenter @Inject constructor(
    private val buildMeta: BuildMeta,
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
                    isDebugBuild = buildMeta.isDebuggable,
                    reactions = event.reactions.toImmutableList(),
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
                    }.toImmutableList())
                }.toImmutableList())
            }
        }
    }
}
