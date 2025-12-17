/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import org.junit.Test

class MatrixPatternsTest {
    private val longLocalPart = "a".repeat(255 - ":server.com".length - 1)

    @Test
    fun `findPatterns - returns raw user ids`() {
        val text = "A @user:server.com and @user2:server.com"
        val patterns = MatrixPatterns.findPatterns(text, aPermalinkParser())
        assertThat(patterns).containsExactly(
            MatrixPatternResult(MatrixPatternType.USER_ID, "@user:server.com", 2, 18),
            MatrixPatternResult(MatrixPatternType.USER_ID, "@user2:server.com", 23, 40)
        )
    }

    @Test
    fun `findPatterns - returns raw room ids`() {
        val text = "A !room:server.com and !room2:server.com"
        val patterns = MatrixPatterns.findPatterns(text, aPermalinkParser())
        assertThat(patterns).containsExactly(
            MatrixPatternResult(MatrixPatternType.ROOM_ID, "!room:server.com", 2, 18),
            MatrixPatternResult(MatrixPatternType.ROOM_ID, "!room2:server.com", 23, 40)
        )
    }

    @Test
    fun `findPatterns - returns raw room aliases`() {
        val text = "A #room:server.com and #room2:server.com"
        val patterns = MatrixPatterns.findPatterns(text, aPermalinkParser())
        assertThat(patterns).containsExactly(
            MatrixPatternResult(MatrixPatternType.ROOM_ALIAS, "#room:server.com", 2, 18),
            MatrixPatternResult(MatrixPatternType.ROOM_ALIAS, "#room2:server.com", 23, 40)
        )
    }

    @Test
    fun `findPatterns - returns raw event ids`() {
        val text = "A \$event:server.com and \$event2:server.com"
        val patterns = MatrixPatterns.findPatterns(text, aPermalinkParser())
        assertThat(patterns).containsExactly(
            MatrixPatternResult(MatrixPatternType.EVENT_ID, "\$event:server.com", 2, 19),
            MatrixPatternResult(MatrixPatternType.EVENT_ID, "\$event2:server.com", 24, 42)
        )
    }

    @Test
    fun `findPatterns - returns @room mention`() {
        val text = "A @room mention"
        val patterns = MatrixPatterns.findPatterns(text, aPermalinkParser())
        assertThat(patterns).containsExactly(MatrixPatternResult(MatrixPatternType.AT_ROOM, "@room", 2, 7))
    }

    @Test
    fun `findPatterns - returns user ids in permalinks`() {
        val text = "A [User](https://matrix.to/#/@user:server.com)"
        val permalinkParser = aPermalinkParser { _ ->
            PermalinkData.UserLink(UserId("@user:server.com"))
        }
        val patterns = MatrixPatterns.findPatterns(text, permalinkParser)
        assertThat(patterns).containsExactly(MatrixPatternResult(MatrixPatternType.USER_ID, "@user:server.com", 2, 46))
    }

    @Test
    fun `findPatterns - returns room aliases in permalinks`() {
        val text = "A [Room](https://matrix.to/#/#room:server.com)"
        val permalinkParser = aPermalinkParser { _ ->
            PermalinkData.RoomLink(RoomIdOrAlias.Alias(RoomAlias("#room:server.com")))
        }
        val patterns = MatrixPatterns.findPatterns(text, permalinkParser)
        assertThat(patterns).containsExactly(MatrixPatternResult(MatrixPatternType.ROOM_ALIAS, "#room:server.com", 2, 46))
    }

    @Test
    fun `test isRoomId`() {
        assertThat(MatrixPatterns.isRoomId(null)).isFalse()
        assertThat(MatrixPatterns.isRoomId("")).isFalse()
        assertThat(MatrixPatterns.isRoomId("not a room id")).isFalse()
        assertThat(MatrixPatterns.isRoomId(" !room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomId("!room:server.com ")).isFalse()
        assertThat(MatrixPatterns.isRoomId("@room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomId("#room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomId("\$room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomId("!${longLocalPart}a:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomId("!9BozuV4TBw6rfRW@rMEgZ5v-jNk1D6FA8Hd1OsWqT9k")).isFalse()

        assertThat(MatrixPatterns.isRoomId("!9BozuV4TBw6rfRW3rMEgZ5v-jNk1D6FA8Hd1OsWqT9k")).isTrue()
        assertThat(MatrixPatterns.isRoomId("!room:server.com")).isTrue()
        assertThat(MatrixPatterns.isRoomId("!$longLocalPart:server.com")).isTrue()
        assertThat(MatrixPatterns.isRoomId("!#test/room\nversion <u>11</u>, with @🐈️:maunium.net")).isTrue()
    }

    @Test
    fun `test isRoomAlias`() {
        assertThat(MatrixPatterns.isRoomAlias(null)).isFalse()
        assertThat(MatrixPatterns.isRoomAlias("")).isFalse()
        assertThat(MatrixPatterns.isRoomAlias("not a room alias")).isFalse()
        assertThat(MatrixPatterns.isRoomAlias(" #room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomAlias("#room:server.com ")).isFalse()
        assertThat(MatrixPatterns.isRoomAlias("@room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomAlias("!room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomAlias("\$room:server.com")).isFalse()
        assertThat(MatrixPatterns.isRoomAlias("#${longLocalPart}a:server.com")).isFalse()

        assertThat(MatrixPatterns.isRoomAlias("#room:server.com")).isTrue()
        assertThat(MatrixPatterns.isRoomAlias("#nico's-stickers:neko.dev")).isTrue()
        assertThat(MatrixPatterns.isRoomAlias("#$longLocalPart:server.com")).isTrue()
    }

    @Test
    fun `test isEventId`() {
        assertThat(MatrixPatterns.isEventId(null)).isFalse()
        assertThat(MatrixPatterns.isEventId("")).isFalse()
        assertThat(MatrixPatterns.isEventId("not an event id")).isFalse()
        assertThat(MatrixPatterns.isEventId(" \$event:server.com")).isFalse()
        assertThat(MatrixPatterns.isEventId("\$event:server.com ")).isFalse()
        assertThat(MatrixPatterns.isEventId("@event:server.com")).isFalse()
        assertThat(MatrixPatterns.isEventId("!event:server.com")).isFalse()
        assertThat(MatrixPatterns.isEventId("#event:server.com")).isFalse()
        assertThat(MatrixPatterns.isEventId("$${longLocalPart}a:server.com")).isFalse()
        assertThat(MatrixPatterns.isEventId("\$" + "a".repeat(255))).isFalse()

        assertThat(MatrixPatterns.isEventId("\$event:server.com")).isTrue()
        assertThat(MatrixPatterns.isEventId("$$longLocalPart:server.com")).isTrue()
        assertThat(MatrixPatterns.isEventId("\$9BozuV4TBw6rfRW3rMEgZ5v-jNk1D6FA8Hd1OsWqT9k")).isTrue()
        assertThat(MatrixPatterns.isEventId("\$" + "a".repeat(254))).isTrue()
    }

    @Test
    fun `test isUserId`() {
        assertThat(MatrixPatterns.isUserId(null)).isFalse()
        assertThat(MatrixPatterns.isUserId("")).isFalse()
        assertThat(MatrixPatterns.isUserId("not a user id")).isFalse()
        assertThat(MatrixPatterns.isUserId(" @user:server.com")).isFalse()
        assertThat(MatrixPatterns.isUserId("@user:server.com ")).isFalse()
        assertThat(MatrixPatterns.isUserId("!user:server.com")).isFalse()
        assertThat(MatrixPatterns.isUserId("#user:server.com")).isFalse()
        assertThat(MatrixPatterns.isUserId("\$user:server.com")).isFalse()
        assertThat(MatrixPatterns.isUserId("@${longLocalPart}a:server.com")).isFalse()

        assertThat(MatrixPatterns.isUserId("@user:server.com")).isTrue()
        assertThat(MatrixPatterns.isUserId("@:server.com")).isTrue()
        assertThat(MatrixPatterns.isUserId("@$longLocalPart:server.com")).isTrue()
    }

    private fun aPermalinkParser(block: (String) -> PermalinkData = { PermalinkData.FallbackLink(Uri.EMPTY) }) = object : PermalinkParser {
        override fun parse(uriString: String): PermalinkData {
            return block(uriString)
        }
    }
}
