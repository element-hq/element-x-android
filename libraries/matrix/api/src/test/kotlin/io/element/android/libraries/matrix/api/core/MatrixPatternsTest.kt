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

package io.element.android.libraries.matrix.api.core

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import org.junit.Test

class MatrixPatternsTest {
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

    private fun aPermalinkParser(block: (String) -> PermalinkData = { PermalinkData.FallbackLink(Uri.EMPTY) }) = object : PermalinkParser {
        override fun parse(uriString: String): PermalinkData {
            return block(uriString)
        }
    }
}
