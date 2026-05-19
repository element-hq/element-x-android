/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoom
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiRoomListService
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import io.element.android.libraries.matrix.test.A_ROOM_ID_4
import io.element.android.libraries.matrix.test.room.aRoomSummary
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.matrix.rustcomponents.sdk.LatestEventValue
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate

class RoomSummaryListProcessorTest {
    private val summaries = MutableStateFlow<List<RoomSummary>>(emptyList())

    @Test
    fun `Append adds new entries at the end of the list`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()

        processor.postUpdate(listOf(RoomListEntriesUpdate.Append(listOf(aRustRoom(A_ROOM_ID_2), aRustRoom(A_ROOM_ID_3), aRustRoom(A_ROOM_ID_4)))))

        assertThat(summaries.value.count()).isEqualTo(4)
        assertThat(summaries.value.subList(1, 4).map { it.roomId }).isEqualTo(listOf(A_ROOM_ID_2, A_ROOM_ID_3, A_ROOM_ID_4))
    }

    @Test
    fun `PushBack adds a new entry at the end of the list`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        processor.postUpdate(listOf(RoomListEntriesUpdate.PushBack(aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value.last().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `PushFront inserts a new entry at the start of the list`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        processor.postUpdate(listOf(RoomListEntriesUpdate.PushFront(aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value.first().roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Set replaces an entry at some index`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Set(index.toUInt(), aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Insert inserts a new entry at the provided index`() = runTest {
        summaries.value = listOf(aRoomSummary())
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Insert(index.toUInt(), aRustRoom(A_ROOM_ID_2))))

        assertThat(summaries.value.count()).isEqualTo(2)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Remove removes an entry at some index`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Remove(index.toUInt())))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `PopBack removes an entry at the end of the list`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.PopBack))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    @Test
    fun `PopFront removes an entry at the start of the list`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.PopFront))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_2)
    }

    @Test
    fun `Clear removes all the entries`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()

        processor.postUpdate(listOf(RoomListEntriesUpdate.Clear))

        assertThat(summaries.value).isEmpty()
    }

    @Test
    fun `Truncate removes all entries after the provided length`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Truncate(1u)))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID)
    }

    @Test
    fun `Reset removes all entries and add the provided ones`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(A_ROOM_ID_2)
        )
        val processor = createProcessor()
        val index = 0

        processor.postUpdate(listOf(RoomListEntriesUpdate.Reset(listOf(aRustRoom(A_ROOM_ID_3)))))

        assertThat(summaries.value.count()).isEqualTo(1)
        assertThat(summaries.value[index].roomId).isEqualTo(A_ROOM_ID_3)
    }

    /**
     * Tracking issue #4182 / #5031: rooms duplicated in the room list.
     *
     * If duplicates are present in the upstream summaries flow, the dedupe safety net in
     * [RoomSummaryListProcessor.updateRoomSummaries] must remove them and report the incident via
     * [analyticsService.trackError]. Uses an empty update to drive the dedupe path without
     * passing a Rust Room through the destroy-on-use path.
     */
    @Test
    fun `pre-existing duplicates in summaries are deduped on next update and trackError fires`() = runTest {
        summaries.value = listOf(
            aRoomSummary(roomId = A_ROOM_ID),
            aRoomSummary(roomId = A_ROOM_ID), // simulated SDK-side leak
            aRoomSummary(roomId = A_ROOM_ID_2),
        )
        val analyticsService = FakeAnalyticsService()
        val processor = createProcessor(analyticsService = analyticsService)

        processor.postUpdate(emptyList())

        assertThat(summaries.value.map { it.roomId }).containsExactly(A_ROOM_ID, A_ROOM_ID_2).inOrder()
        assertThat(analyticsService.trackedErrors).hasSize(1)
    }

    /**
     * Tracking issue #4182 / #5031.
     *
     * Insert is the most likely Rust-SDK trigger for a duplicate-room report: it blindly inserts
     * a new entry at an index without checking whether the roomId already exists. Before the
     * describe-capture fix, the dedupe branch in [updateRoomSummaries] would call `Room.id()`
     * on an already-destroyed Room (because [applyUpdate] consumes each value via
     * `entry.use { ... }`) and crash before [trackError] could be invoked. This test guards the
     * fix: the Insert is processed, the list is emitted deduplicated, and the tracked error
     * message carries the human-readable description of the offending update.
     */
    @Test
    fun `Insert that triggers dedupe is reported via trackError without crashing`() = runTest {
        summaries.value = listOf(aRoomSummary(roomId = A_ROOM_ID))
        val analyticsService = FakeAnalyticsService()
        val processor = createProcessor(analyticsService = analyticsService)

        processor.postUpdate(listOf(RoomListEntriesUpdate.Insert(0u, aRustRoom(A_ROOM_ID))))

        assertThat(summaries.value.map { it.roomId }).containsExactly(A_ROOM_ID)
        assertThat(analyticsService.trackedErrors).hasSize(1)
        val message = analyticsService.trackedErrors.single().message.orEmpty()
        assertThat(message).contains("Found duplicates")
        assertThat(message).contains("Insert at #0")
    }

    private fun aRustRoom(roomId: RoomId = A_ROOM_ID) = FakeFfiRoom(
        roomId = roomId,
        latestEventLambda = { LatestEventValue.None }
    )

    private fun TestScope.createProcessor(
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ) = RoomSummaryListProcessor(
        summaries,
        FakeFfiRoomListService(),
        coroutineContext = StandardTestDispatcher(testScheduler),
        roomSummaryFactory = RoomSummaryFactory(),
        analyticsService = analyticsService,
    )
}
