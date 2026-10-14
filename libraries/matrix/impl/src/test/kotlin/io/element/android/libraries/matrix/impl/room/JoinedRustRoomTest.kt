/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiThreadListService
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.Room
import kotlin.coroutines.cancellation.CancellationException

class JoinedRustRoomTest {
    @Test
    fun `sendStateEventRaw forwards arguments and maps the returned event id`() = runTest {
        val innerRoom = mockk<Room>()
        val eventType = "m.room.retention"
        val stateKey = "policy"
        val content = """{"max_lifetime":86400000}"""
        coEvery { innerRoom.sendStateEventRaw(eventType, stateKey, content) } returns AN_EVENT_ID.value
        val room = createJoinedRustRoom(innerRoom)

        val result = room.sendStateEventRaw(eventType, stateKey, content)

        assertThat(result.getOrThrow()).isEqualTo(AN_EVENT_ID)
        coVerify(exactly = 1) { innerRoom.sendStateEventRaw(eventType, stateKey, content) }
    }

    @Test
    fun `sendStateEventRaw returns SDK failures`() = runTest {
        val innerRoom = mockk<Room>()
        val failure = IllegalStateException("State event rejected")
        coEvery { innerRoom.sendStateEventRaw("m.room.retention", "", "{}") } throws failure
        val room = createJoinedRustRoom(innerRoom)

        val result = room.sendStateEventRaw("m.room.retention", "", "{}")

        assertThat(result.exceptionOrNull()).isSameInstanceAs(failure)
    }

    @Test(expected = CancellationException::class)
    fun `sendStateEventRaw propagates cancellation`() = runTest {
        val innerRoom = mockk<Room>()
        val cancellation = CancellationException("Cancelled")
        coEvery { innerRoom.sendStateEventRaw(any(), any(), any()) } throws cancellation
        val room = createJoinedRustRoom(innerRoom)

        room.sendStateEventRaw("m.room.retention", "", "{}")
    }

    private fun TestScope.createJoinedRustRoom(innerRoom: Room): JoinedRustRoom {
        every { innerRoom.threadListService() } returns FakeFfiThreadListService()
        val baseRoom = mockk<RustBaseRoom> {
            every { this@mockk.innerRoom } returns innerRoom
            every { roomId } returns A_ROOM_ID
            every { roomCoroutineScope } returns backgroundScope
            every { roomInfoFlow } returns MutableStateFlow(aRoomInfo())
        }
        return JoinedRustRoom(
            baseRoom = baseRoom,
            liveInnerTimeline = FakeFfiTimeline(),
            notificationSettingsService = mockk(),
            coroutineDispatchers = testCoroutineDispatchers(),
            systemClock = FakeSystemClock(),
            roomContentForwarder = mockk(),
            featureFlagService = mockk(),
        )
    }
}
