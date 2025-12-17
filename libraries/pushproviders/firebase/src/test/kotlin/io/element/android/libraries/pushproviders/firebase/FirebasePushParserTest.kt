/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.tests.testutils.assertThrowsInDebug
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
        assertThrowsInDebug { pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", "")) }
    }

    @Test
    fun `test invalid roomId`() {
        val pushParser = FirebasePushParser()
        assertThrowsInDebug { pushParser.parse(FIREBASE_PUSH_DATA.mutate("room_id", "aRoomId:domain")) }
    }

    @Test
    fun `test empty eventId`() {
        val pushParser = FirebasePushParser()
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", null))).isNull()
        assertThrowsInDebug { pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", "")) }
    }

    @Test
    fun `test empty client secret`() {
        val pushParser = FirebasePushParser()
        assertThat(pushParser.parse(FIREBASE_PUSH_DATA.mutate("cs", null))).isNull()
    }

    @Test
    fun `test invalid eventId`() {
        val pushParser = FirebasePushParser()
        assertThrowsInDebug { pushParser.parse(FIREBASE_PUSH_DATA.mutate("event_id", "anEventId")) }
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
