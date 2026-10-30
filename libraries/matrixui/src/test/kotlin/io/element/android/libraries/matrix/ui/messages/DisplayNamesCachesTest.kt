/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.user.DisplayedStatus
import io.element.android.libraries.matrix.api.user.UserStatus
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.room.aRoomSummary
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DisplayNamesCachesTest {
    @Test
    fun `RoomMemberProfilesCache - displayNames contains the disambiguated names of members with a display name`() = runTest {
        val cache = RoomMemberProfilesCache()
        assertThat(cache.displayNames.value).isEmpty()
        val alice = UserId("@alice:server.org")
        val bob = UserId("@bob:server.org")
        val carol = UserId("@carol:server.org")
        cache.replace(
            listOf(
                aRoomMember(userId = alice, displayName = "Alice"),
                aRoomMember(userId = bob, displayName = "Bob", isNameAmbiguous = true),
                aRoomMember(userId = carol, displayName = null),
            )
        )
        assertThat(cache.displayNames.value).containsExactly(
            alice,
            "Alice",
            bob,
            "Bob (@bob:server.org)",
        )
    }

    @Test
    fun `RoomMemberProfilesCache - displayedStatusEmojis contains the status emojis of members with a displayed status`() = runTest {
        val cache = RoomMemberProfilesCache()
        assertThat(cache.displayedStatusEmojis.value).isEmpty()
        val alice = UserId("@alice:server.org")
        val bob = UserId("@bob:server.org")
        val carol = UserId("@carol:server.org")
        cache.replace(
            listOf(
                aRoomMember(userId = alice, displayedStatus = DisplayedStatus.InCall(callJoinedTs = null)),
                aRoomMember(userId = bob, displayedStatus = DisplayedStatus.UserSet(UserStatus(emoji = "🌴", text = "On holidays"))),
                aRoomMember(userId = carol, displayedStatus = null),
            )
        )
        assertThat(cache.displayedStatusEmojis.value).containsExactly(
            alice,
            "🎧",
            bob,
            "🌴",
        )
    }

    @Test
    fun `RoomNamesCache - displayNames contains the names of rooms by id and canonical alias`() = runTest {
        val cache = RoomNamesCache()
        assertThat(cache.displayNames.value).isEmpty()
        val roomId = RoomId("!room:server.org")
        val alias = RoomAlias("#room:server.org")
        val unnamedRoomId = RoomId("!unnamed:server.org")
        cache.replace(
            listOf(
                aRoomSummary(roomId = roomId, name = "A room", canonicalAlias = alias),
                aRoomSummary(roomId = unnamedRoomId, name = null),
            )
        )
        assertThat(cache.displayNames.value).containsExactly(
            roomId.toRoomIdOrAlias(),
            "A room",
            alias.toRoomIdOrAlias(),
            "A room",
        )
        assertThat(cache.getDisplayName(unnamedRoomId.toRoomIdOrAlias())).isNull()
    }
}
