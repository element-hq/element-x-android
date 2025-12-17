/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
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
        val pushParser = createUnifiedPushParser()
        // Empty string
        assertThat(pushParser.parse("".toByteArray(), aClientSecret)).isNull()
        // Empty Json
        assertThat(pushParser.parse("{}".toByteArray(), aClientSecret)).isNull()
        // Bad Json
        assertThat(pushParser.parse("ABC".toByteArray(), aClientSecret)).isNull()
    }

    @Test
    fun `test UnifiedPush format`() {
        val pushParser = createUnifiedPushParser()
        assertThat(pushParser.parse(UNIFIED_PUSH_DATA.toByteArray(), aClientSecret)).isEqualTo(validData)
    }

    @Test
    fun `test empty roomId`() {
        val pushParser = createUnifiedPushParser()
        assertThrowsInDebug {
            pushParser.parse(UNIFIED_PUSH_DATA.replace(A_ROOM_ID.value, "").toByteArray(), aClientSecret)
        }
    }

    @Test
    fun `test invalid roomId`() {
        val pushParser = createUnifiedPushParser()
        assertThrowsInDebug {
            pushParser.parse(UNIFIED_PUSH_DATA.mutate(A_ROOM_ID.value, "aRoomId:domain"), aClientSecret)
        }
    }

    @Test
    fun `test empty eventId`() {
        val pushParser = createUnifiedPushParser()
        assertThrowsInDebug {
            pushParser.parse(UNIFIED_PUSH_DATA.mutate(AN_EVENT_ID.value, ""), aClientSecret)
        }
    }

    @Test
    fun `test invalid eventId`() {
        val pushParser = createUnifiedPushParser()
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

fun createUnifiedPushParser() = UnifiedPushParser(
    json = DefaultJsonProvider(),
)
