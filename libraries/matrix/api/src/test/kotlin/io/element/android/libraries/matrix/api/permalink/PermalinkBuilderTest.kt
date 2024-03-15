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

package io.element.android.libraries.matrix.api.permalink

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.tests.testutils.assertThrowsInDebug
import io.element.android.libraries.androidutils.metadata.isInDebug
import org.junit.Test

class PermalinkBuilderTest {
    fun `building a permalink for an invalid user id throws when verifying the id`() {
        assertThrowsInDebug {
            val userId = UserId("some invalid user id")
            PermalinkBuilder.permalinkForUser(userId)
        }
    }

    fun `building a permalink for an invalid room id throws when verifying the id`() {
        assertThrowsInDebug {
            val roomId = RoomId("some invalid room id")
            PermalinkBuilder.permalinkForRoomId(roomId)
        }
    }

    @Test
    fun `building a permalink for an invalid user id returns failure when not verifying the id`() {
        isInDebug.set(false)
        val userId = UserId("some invalid user id")
        assertThat(PermalinkBuilder.permalinkForUser(userId).isFailure).isTrue()
    }

    @Test
    fun `building a permalink for an invalid room id returns failure when not verifying the id`() {
        isInDebug.set(false)
        val roomId = RoomId("some invalid room id")
        assertThat(PermalinkBuilder.permalinkForRoomId(roomId).isFailure).isTrue()
    }

    @Test
    fun `building a permalink for an invalid room alias returns failure`() {
        val roomAlias = "an invalid room alias"
        assertThat(PermalinkBuilder.permalinkForRoomAlias(roomAlias).isFailure).isTrue()
    }

    @Test
    fun `building a permalink for a valid user id returns a matrix-to url`() {
        val userId = UserId("@user:matrix.org")
        assertThat(PermalinkBuilder.permalinkForUser(userId).getOrNull()).isEqualTo("https://matrix.to/#/@user:matrix.org")
    }

    @Test
    fun `building a permalink for a valid room id returns a matrix-to url`() {
        val roomId = RoomId("!aBCdEFG1234:matrix.org")
        assertThat(PermalinkBuilder.permalinkForRoomId(roomId).getOrNull()).isEqualTo("https://matrix.to/#/!aBCdEFG1234:matrix.org")
    }

    @Test
    fun `building a permalink for a valid room alias returns a matrix-to url`() {
        val roomAlias = "#room:matrix.org"
        assertThat(PermalinkBuilder.permalinkForRoomAlias(roomAlias).getOrNull()).isEqualTo("https://matrix.to/#/#room:matrix.org")
    }
}
