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

package io.element.android.libraries.push.impl.firebase

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.push.impl.push.PushData
import org.junit.Test

class FirebasePushParserTest {
    private val validData = PushData(
        eventId = AN_EVENT_ID,
        roomId = A_ROOM_ID,
        unread = 1,
        clientSecret = "a-secret"
    )

    private val emptyData = PushData(
        eventId = null,
        roomId = null,
        unread = null,
        clientSecret = null
    )

    @Test
    fun `test edge cases Firebase`() {
        val pushParser = FirebasePushParser()
        // Empty Json
        assertThat(pushParser.parse(emptyMap())).isEqualTo(emptyData)
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
        val expected = validData.copy(roomId = null)
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", null))).isEqualTo(expected)
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", ""))).isEqualTo(expected)
    }

    @Test
    fun `test invalid roomId`() {
        val pushParser = FirebasePushParser()
        val expected = validData.copy(roomId = null)
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", "aRoomId:domain"))).isEqualTo(expected)
    }

    @Test
    fun `test empty eventId`() {
        val pushParser = FirebasePushParser()
        val expected = validData.copy(eventId = null)
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", null))).isEqualTo(expected)
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", ""))).isEqualTo(expected)
    }

    @Test
    fun `test invalid eventId`() {
        val pushParser = FirebasePushParser()
        val expected = validData.copy(eventId = null)
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", "anEventId"))).isEqualTo(expected)
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
