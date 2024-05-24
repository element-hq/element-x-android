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

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.tests.testutils.assertThrowsInDebug
import org.junit.Test

class UnifiedPushParserTest {
    private val aClientSecret = "a-client-secret"
    private val validData = PushData(
        eventId = AN_EVENT_ID,
        roomId = A_ROOM_ID,
        unread = 1,
        clientSecret = aClientSecret
    )

    @Test
    fun `test edge cases UnifiedPush`() {
        val pushParser = UnifiedPushParser()
        // Empty string
        assertThat(pushParser.parse("".toByteArray(), aClientSecret)).isNull()
        // Empty Json
        assertThat(pushParser.parse("{}".toByteArray(), aClientSecret)).isNull()
        // Bad Json
        assertThat(pushParser.parse("ABC".toByteArray(), aClientSecret)).isNull()
    }

    @Test
    fun `test UnifiedPush format`() {
        val pushParser = UnifiedPushParser()
        assertThat(pushParser.parse(UNIFIED_PUSH_DATA.toByteArray(), aClientSecret)).isEqualTo(validData)
    }

    @Test
    fun `test empty roomId`() {
        val pushParser = UnifiedPushParser()
        assertThrowsInDebug {
            pushParser.parse(UNIFIED_PUSH_DATA.replace(A_ROOM_ID.value, "").toByteArray(), aClientSecret)
        }
    }

    @Test
    fun `test invalid roomId`() {
        val pushParser = UnifiedPushParser()
        assertThrowsInDebug {
            pushParser.parse(UNIFIED_PUSH_DATA.mutate(A_ROOM_ID.value, "aRoomId:domain"), aClientSecret)
        }
    }

    @Test
    fun `test empty eventId`() {
        val pushParser = UnifiedPushParser()
        assertThrowsInDebug {
            pushParser.parse(UNIFIED_PUSH_DATA.mutate(AN_EVENT_ID.value, ""), aClientSecret)
        }
    }

    @Test
    fun `test invalid eventId`() {
        val pushParser = UnifiedPushParser()
        assertThrowsInDebug {
            pushParser.parse(UNIFIED_PUSH_DATA.mutate(AN_EVENT_ID.value, "anEventId"), aClientSecret)
        }
    }

    companion object {
        val UNIFIED_PUSH_DATA =
            "{\"notification\":{\"event_id\":\"$AN_EVENT_ID\",\"room_id\":\"$A_ROOM_ID\",\"counts\":{\"unread\":1},\"prio\":\"high\"}}"
    }
}

private fun String.mutate(oldValue: String, newValue: String): ByteArray {
    return replace(oldValue, newValue).toByteArray()
}
