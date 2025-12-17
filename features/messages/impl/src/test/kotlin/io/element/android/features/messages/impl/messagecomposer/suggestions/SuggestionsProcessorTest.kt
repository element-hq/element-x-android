/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer.suggestions

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMembersState
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember())),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember())),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember())),
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
            roomMembersState = RoomMembersState.Unknown,
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
            roomMembersState = RoomMembersState.Ready(persistentListOf()),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf()),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(
                RoomAliasSuggestion(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = aRoomSummary.info.name,
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            ),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Alias(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = aRoomSummary.info.name,
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            )
        )
    }

    @Test
    fun `processing Room suggestion with aliases will return a suggestion when matching on alias`() = runTest {
        val aRoomSummary = aRoomSummary(canonicalAlias = A_ROOM_ALIAS)
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion("ali"),
            roomMembersState = RoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(
                RoomAliasSuggestion(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = aRoomSummary.info.name,
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            ),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Alias(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = aRoomSummary.info.name,
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            )
        )
    }

    @Test
    fun `processing Room suggestion with aliases not found will return no suggestions`() = runTest {
        val aRoomSummary = aRoomSummary(canonicalAlias = A_ROOM_ALIAS)
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion("tot"),
            roomMembersState = RoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(
                RoomAliasSuggestion(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = "Element",
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            ),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEmpty()
    }

    @Test
    fun `processing Room suggestion will return a suggestion when matching on room name`() = runTest {
        val aRoomSummary = aRoomSummary(canonicalAlias = A_ROOM_ALIAS)
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion("lement"),
            roomMembersState = RoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(
                RoomAliasSuggestion(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = "Element",
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            ),
            currentUserId = A_USER_ID,
            canSendRoomMention = { true },
        )
        assertThat(result).isEqualTo(
            listOf(
                ResolvedSuggestion.Alias(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = "Element",
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            )
        )
    }

    @Test
    fun `processing Room suggestion will not return a suggestion when room has no name`() = runTest {
        val aRoomSummary = aRoomSummary(canonicalAlias = A_ROOM_ALIAS)
        val result = suggestionsProcessor.process(
            suggestion = aRoomSuggestion("lement"),
            roomMembersState = RoomMembersState.Ready(persistentListOf()),
            roomAliasSuggestions = listOf(
                RoomAliasSuggestion(
                    roomAlias = A_ROOM_ALIAS,
                    roomId = aRoomSummary.roomId,
                    roomName = null,
                    roomAvatarUrl = aRoomSummary.info.avatarUrl,
                )
            ),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember)),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember)),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember)),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember)),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember)),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember)),
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
            roomMembersState = RoomMembersState.Ready(persistentListOf(aRoomMember)),
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
