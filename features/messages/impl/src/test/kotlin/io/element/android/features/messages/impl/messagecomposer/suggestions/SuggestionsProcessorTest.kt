/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.messagecomposer.suggestions

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.messagecomposer.RoomAliasSuggestion
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.SuggestionType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SuggestionsProcessorTest {
    private fun aMentionSuggestion(text: String) = Suggestion(0, 1, SuggestionType.Mention, text)
    private fun aRoomSuggestion(text: String) = Suggestion(0, 1, SuggestionType.Room, text)
    private val aCommandSuggestion = Suggestion(0, 1, SuggestionType.Command, "")
    private val aCustomSuggestion = Suggestion(0, 1, SuggestionType.Custom("*"), "")

    private val suggestionsProcessor = SuggestionsProcessor()

    @Test
    fun `processing null suggestion will return empty suggestion`() = runTest {
        val result = suggestionsProcessor.process(
            suggestion = null,
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember())),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Command will return empty suggestion`() = runTest {
        val result = suggestionsProcessor.process(
            suggestion = aCommandSuggestion,
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember())),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Custom will return empty suggestion`() = runTest {
        val result = suggestionsProcessor.process(
            suggestion = aCustomSuggestion,
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember())),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Mention suggestion with not loaded members will return empty suggestion`() = runTest {
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion(""),
            roomMembersState = MatrixRoomMembersState.Unknown,
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Mention suggestion with no members will return empty suggestion`() = runTest {
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion(""),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Room suggestion with no aliases will return empty suggestion`() = runTest {
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion(""),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Room suggestion with aliases ignoring cases will return a suggestion`() = runTest {
        val aRoomSummary = aRoomSummary(canonicalAlias = A_ROOM_ALIAS)
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion("ALI"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(RoomAliasSuggestion(A_ROOM_ALIAS, aRoomSummary)),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Alias(A_ROOM_ALIAS, aRoomSummary)
            )
        )
    }

    @Test
    fun `processing Room suggestion with aliases will return a suggestion`() = runTest {
        val aRoomSummary = aRoomSummary(canonicalAlias = A_ROOM_ALIAS)
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion("ali"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(RoomAliasSuggestion(A_ROOM_ALIAS, aRoomSummary)),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Alias(A_ROOM_ALIAS, aRoomSummary)
            )
        )
    }

    @Test
    fun `processing Room suggestion with aliases not found will return no suggestions`() = runTest {
        val aRoomSummary = aRoomSummary(canonicalAlias = A_ROOM_ALIAS)
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion("tot"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(RoomAliasSuggestion(A_ROOM_ALIAS, aRoomSummary)),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Mention suggestion with return matching matrix Id`() = runTest {
        val aRoomMember = aRoomMember(userId = UserId("@alice:server.org"), displayName = null)
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion("ali"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember)),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID_2,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Member(aRoomMember)
            )
        )
    }

    @Test
    fun `processing Mention suggestion with not return the current user`() = runTest {
        val aRoomMember = aRoomMember(userId = UserId("@alice:server.org"), displayName = null)
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion("ali"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember)),
            roomAliasSuggestions = emptyList(),
            currentUserId = UserId("@alice:server.org"),
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Mention suggestion with return empty list if there is no matches`() = runTest {
        val aRoomMember = aRoomMember(userId = UserId("@alice:server.org"), displayName = "alice")
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion("bo"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember)),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID_2,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Mention suggestion with not return not joined member`() = runTest {
        val aRoomMember = aRoomMember(userId = UserId("@alice:server.org"), membership = RoomMembershipState.INVITE)
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion("ali"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember)),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID_2,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Mention suggestion with return matching display name`() = runTest {
        val aRoomMember = aRoomMember(userId = UserId("@alice:server.org"), displayName = "bob")
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion("bo"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember)),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID_2,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Member(aRoomMember)
            )
        )
    }

    @Test
    fun `processing Mention suggestion with return matching display name and room if allowed`() = runTest {
        val aRoomMember = aRoomMember(userId = UserId("@alice:server.org"), displayName = "ro")
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion("ro"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember)),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID_2,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.AtRoom,
                ResolvedSuggestion.Member(aRoomMember),
            )
        )
    }

    @Test
    fun `processing Mention suggestion with return matching display name but not room if not allowed`() = runTest {
        val aRoomMember = aRoomMember(userId = UserId("@alice:server.org"), displayName = "ro")
        val result = suggestionsProcessor.process(
            suggestion = aMentionSuggestion("ro"),
            roomMembersState = MatrixRoomMembersState.Ready(persistentListOf(aRoomMember)),
            roomAliasSuggestions = emptyList(),
            currentUserId = A_USER_ID_2,
            canSendRoomMention = { false },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Member(aRoomMember),
            )
        )
    }
}
