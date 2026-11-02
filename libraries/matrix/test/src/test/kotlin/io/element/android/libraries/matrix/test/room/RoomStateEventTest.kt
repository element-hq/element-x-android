/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomStateEventTest {
    @Test(expected = AssertionError::class)
    fun `sendStateEventRaw fails when no result is configured`() = runTest {
        val room = FakeJoinedRoom()

        room.sendStateEventRaw("m.room.retention", "", "{}")
    }

    @Test
    fun `sendStateEventRaw forwards arguments and returns the configured event id`() = runTest {
        val calls = mutableListOf<List<String>>()
        val room = FakeJoinedRoom(
            sendStateEventRawResult = { eventType, stateKey, content ->
                calls.add(listOf(eventType, stateKey, content))
                Result.success(AN_EVENT_ID)
            }
        )
        val content = """{"max_lifetime":86400000}"""

        val result = room.sendStateEventRaw("m.room.retention", "policy", content)

        assertThat(result.getOrThrow()).isEqualTo(AN_EVENT_ID)
        assertThat(calls).containsExactly(listOf("m.room.retention", "policy", content))
    }

    @Test
    fun `sendStateEventRaw returns the configured failure`() = runTest {
        val failure = IllegalStateException("State event rejected")
        val room = FakeJoinedRoom(sendStateEventRawResult = { _, _, _ -> Result.failure(failure) })

        val result = room.sendStateEventRaw("m.room.retention", "", "{}")

        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
    }
}
