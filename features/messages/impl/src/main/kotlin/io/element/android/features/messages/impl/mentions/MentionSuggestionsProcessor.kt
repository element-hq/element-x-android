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

package io.element.android.features.messages.impl.mentions

import io.element.android.features.messages.impl.messagecomposer.RoomMemberSuggestion
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType

object MentionSuggestionsProcessor {

    private const val MAX_BATCH_ITEMS = 100

    suspend fun process(
        suggestion: Suggestion?,
        roomMembersState: MatrixRoomMembersState,
        currentUserId: UserId,
        canSendRoomMention: suspend () -> Boolean,
    ): List<RoomMemberSuggestion> {
        val members = roomMembersState.roomMembers()
            // Take the first MAX_BATCH_ITEMS only
            ?.take(MAX_BATCH_ITEMS)
        return when {
            members.isNullOrEmpty() || suggestion == null -> {
                // Clear suggestions
                emptyList()
            }
            else -> {
                when (suggestion.type) {
                    SuggestionType.Mention -> {
                        // Replace suggestions
                        val matchingMembers = getMemberSuggestions(
                            query = suggestion.text,
                            roomMembers = roomMembersState.roomMembers(),
                            currentUserId = currentUserId,
                            canSendRoomMention = canSendRoomMention()
                        )
                        matchingMembers
                    }
                    else -> {
                        // Clear suggestions
                        emptyList()
                    }
                }
            }
        }
    }

    private fun getMemberSuggestions(
        query: String,
        roomMembers: List<RoomMember>?,
        currentUserId: UserId,
        canSendRoomMention: Boolean,
    ): List<RoomMemberSuggestion> {
        return if (roomMembers.isNullOrEmpty()) {
            emptyList()
        } else {
            fun isJoinedMemberAndNotSelf(member: RoomMember): Boolean {
                return member.membership == RoomMembershipState.JOIN && currentUserId != member.userId
            }

            fun memberMatchesQuery(member: RoomMember, query: String): Boolean {
                return member.userId.value.contains(query, ignoreCase = true)
                    || member.displayName?.contains(query, ignoreCase = true) == true
            }

            val matchingMembers = roomMembers
                // Search only in joined members, exclude the current user
                .filter { member ->
                    isJoinedMemberAndNotSelf(member) && memberMatchesQuery(member, query)
                }
                .map(RoomMemberSuggestion::Member)

            if ("room".contains(query) && canSendRoomMention) {
                listOf(RoomMemberSuggestion.Room) + matchingMembers
            } else {
                matchingMembers
            }
        }
    }
}
