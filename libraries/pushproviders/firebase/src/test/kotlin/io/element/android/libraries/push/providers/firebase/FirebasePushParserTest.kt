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

package io.element.android.libraries.push.providers.firebase

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.push.providers.api.PushData
import org.junit.Assert.assertThrows
import org.junit.Test

class FirebasePushParserTest {
    private val validData = PushData(
        eventId = AN_EVENT_ID,
        roomId = A_ROOM_ID,
        unread = 1,
        clientSecret = "a-secret"
    )

    @Test
    fun `test edge cases Firebase`() {
        val pushParser = FirebasePushParser()
        // Empty Json
        assertThat(pushParser.parse(emptyMap())).isNull()
        // Bad Json
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("unread", "str"))).isEqualTo(validData.copy(unread = null))
        // Extra data
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("extra", "5"))).isEqualTo(validData)
    }

    @Test
    fun `test Firebase format`() {
        val pushParser = FirebasePushParser()
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA)).isEqualTo(validData)
    }

    @Test
    fun `test empty roomId`() {
        val pushParser = FirebasePushParser()
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", null))).isNull()
        assertThrows(IllegalStateException::class.java) { pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", "")) }
    }

    @Test
    fun `test invalid roomId`() {
        val pushParser = FirebasePushParser()
        assertThrows(IllegalStateException::class.java) { pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", "aRoomId:domain")) }
    }

    @Test
    fun `test empty eventId`() {
        val pushParser = FirebasePushParser()
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", null))).isNull()
        assertThrows(IllegalStateException::class.java) { pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", "")) }
    }

    @Test
    fun `test invalid eventId`() {
        val pushParser = FirebasePushParser()
        assertThrows(IllegalStateException::class.java) { pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", "anEventId")) }
    }

    companion object {
        private val FIREBASE_PUSH_DATA = mapOf(
            "event_id" to AN_EVENT_ID.value,
            "room_id" to A_ROOM_ID.value,
            "unread" to "1",
            "prio" to "high",
            "cs" to "a-secret",
        )
    }
}

private fun Map<String, String?>.mutate(key: String, value: String?): Map<String, String?> {
    return toMutableMap().apply { put(key, value) }
}
