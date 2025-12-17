/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer.suggestions

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.data.filterUpTo
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType

/**
 * This class is responsible for processing suggestions when `@`, `/` or `#` are type in the composer.
 */
@Inject
class SuggestionsProcessor {
    /**
     *  Process the suggestion.
     *  @param suggestion The current suggestion input
     *  @param roomMembersState The room members state, it contains the current users in the room
     *  @param roomAliasSuggestions The available room alias suggestions
     *  @param currentUserId The current user id
     *  @param canSendRoomMention Should return true if the current user can send room mentions
     *  @return The list of suggestions to display
     */
    suspend fun process(
        suggestion: Suggestion?,
        roomMembersState: RoomMembersState,
        roomAliasSuggestions: List<RoomAliasSuggestion>,
        currentUserId: UserId,
        canSendRoomMention: suspend () -> Boolean,
    ): List<ResolvedSuggestion> {
        suggestion ?: return emptyList()
        return when (suggestion.type) {
            SuggestionType.Mention -> {
                // Replace suggestions
                val members = roomMembersState.roomMembers()
                val matchingMembers = getMemberSuggestions(
                    query = suggestion.text,
                    roomMembers = members,
                    currentUserId = currentUserId,
                    canSendRoomMention = canSendRoomMention()
                )
                matchingMembers
            }
            SuggestionType.Room -> {
                roomAliasSuggestions
                    .filter { roomAliasSuggestion ->
                        // Filter by either room alias or room name (if available)
                        roomAliasSuggestion.roomAlias.value.contains(suggestion.text, ignoreCase = true) ||
                            roomAliasSuggestion.roomName?.contains(suggestion.text, ignoreCase = true) == true
                    }
                    .map {
                        ResolvedSuggestion.Alias(
                            roomAlias = it.roomAlias,
                            roomId = it.roomId,
                            roomName = it.roomName,
                            roomAvatarUrl = it.roomAvatarUrl,
                        )
                    }
            }
            SuggestionType.Command,
            SuggestionType.Emoji,
            is SuggestionType.Custom -> {
                // Clear suggestions
                emptyList()
            }
        }
    }

    private fun getMemberSuggestions(
        query: String,
        roomMembers: List<RoomMember>?,
        currentUserId: UserId,
        canSendRoomMention: Boolean,
    ): List<ResolvedSuggestion> {
        return if (roomMembers.isNullOrEmpty()) {
            emptyList()
        } else {
            fun isJoinedMemberAndNotSelf(member: RoomMember): Boolean {
                return member.membership == RoomMembershipState.JOIN && currentUserId != member.userId
            }

            fun memberMatchesQuery(member: RoomMember, query: String): Boolean {
                return member.userId.value.contains(query, ignoreCase = true) ||
                    member.displayName?.contains(query, ignoreCase = true) == true
            }

            val matchingMembers = roomMembers
                // Search only in joined members, up to MAX_BATCH_ITEMS, exclude the current user
                .filterUpTo(MAX_BATCH_ITEMS) { member ->
                    isJoinedMemberAndNotSelf(member) && memberMatchesQuery(member, query)
                }
                .map(ResolvedSuggestion::Member)

            if ("room".contains(query) && canSendRoomMention) {
                listOf(ResolvedSuggestion.AtRoom) + matchingMembers
            } else {
                matchingMembers
            }
        }
    }

    companion object {
        // We don't want to retrieve thousands of members
        private const val MAX_BATCH_ITEMS = 100
    }
}
