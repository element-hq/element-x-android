/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.search.backfill

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.search.RoomSweepOutcome
import io.element.android.libraries.matrix.api.search.SearchBackfillCursor
import io.element.android.libraries.matrix.api.search.SearchBackfillStore
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * These tests pin the LOOP SHAPE and nothing more.
 *
 * They cannot show that a single message becomes searchable: the chain from `paginate()` through the
 * SDK event cache into tantivy has no app-level observation point, and no index API exists over FFI.
 * A fully green file here means the loop iterated correctly over fakes. It is not evidence the
 * feature works, and it must never be cited as such.
 */
class SearchBackfillRunnerTest {
    @Test
    fun `each room is paginated until it reports the start of the room`() = runTest {
        val timeline = fakeTimeline(reachStartAfter = 3)
        val runner = runner(rooms = listOf(A_ROOM), timelines = mapOf(A_ROOM to timeline))

        val cursor = runner.runOnce()

        assertThat(timeline.paginateCallCount).isEqualTo(3)
        assertThat(cursor.outcomes[A_ROOM.value]).isEqualTo(RoomSweepOutcome.REACHED_START)
        assertThat(cursor.isDrained).isTrue()
    }

    @Test
    fun `a room that cannot paginate is never asked to`() = runTest {
        // The regression test for the silent no-op: paginate() THROWS CannotPaginate when the status
        // says it cannot, so calling it unguarded would turn "nothing to fetch" into a fake failure.
        val timeline = fakeTimeline(canPaginate = false)
        val runner = runner(rooms = listOf(A_ROOM), timelines = mapOf(A_ROOM to timeline))

        val cursor = runner.runOnce()

        assertThat(timeline.paginateCallCount).isEqualTo(0)
        assertThat(cursor.outcomes[A_ROOM.value]).isEqualTo(RoomSweepOutcome.REACHED_START)
    }

    @Test
    fun `pagination stops at the per-room page cap`() = runTest {
        val timeline = fakeTimeline(reachStartAfter = Int.MAX_VALUE)
        val runner = runner(
            rooms = listOf(A_ROOM),
            timelines = mapOf(A_ROOM to timeline),
            budget = SearchBackfillBudget(maxPagesPerRoom = 4),
        )

        val cursor = runner.runOnce()

        assertThat(timeline.paginateCallCount).isEqualTo(4)
        assertThat(cursor.outcomes[A_ROOM.value]).isEqualTo(RoomSweepOutcome.PAGE_CAP)
    }

    @Test
    fun `a room is abandoned after repeated failures and the sweep continues`() = runTest {
        val failing = fakeTimeline(failEvery = true)
        val healthy = fakeTimeline(reachStartAfter = 1)
        val runner = runner(
            rooms = listOf(A_ROOM, B_ROOM),
            timelines = mapOf(A_ROOM to failing, B_ROOM to healthy),
            budget = SearchBackfillBudget(maxFailuresPerRoom = 2),
        )

        val cursor = runner.runOnce()

        assertThat(failing.paginateCallCount).isEqualTo(2)
        assertThat(cursor.outcomes[A_ROOM.value]).isEqualTo(RoomSweepOutcome.FAILED)
        // The important half: one bad room must not sink the sweep.
        assertThat(cursor.outcomes[B_ROOM.value]).isEqualTo(RoomSweepOutcome.REACHED_START)
    }

    @Test
    fun `a room that is no longer joined is skipped without paginating`() = runTest {
        val runner = runner(rooms = listOf(A_ROOM), timelines = emptyMap())

        val cursor = runner.runOnce()

        assertThat(cursor.outcomes[A_ROOM.value]).isEqualTo(RoomSweepOutcome.NOT_JOINED)
    }

    @Test
    fun `an empty queue asks for another execution instead of reporting completion`() = runTest {
        // A headless start right after the caches were cleared can see an empty room list. Reading
        // that as "all done" would park the sweep until the next app start with nothing indexed.
        val runner = runner(rooms = emptyList(), timelines = emptyMap())

        val cursor = runner.runOnce()

        assertThat(cursor.needsAnotherExecution).isTrue()
    }

    @Test
    fun `a drained queue with visited rooms does not ask for another execution`() = runTest {
        val runner = runner(
            rooms = listOf(A_ROOM),
            timelines = mapOf(A_ROOM to fakeTimeline(reachStartAfter = 1)),
        )

        val cursor = runner.runOnce()

        assertThat(cursor.needsAnotherExecution).isFalse()
    }

    @Test
    fun `the room is always released, including when pagination fails`() = runTest {
        // 200 rooms of un-closed Rust handles would be a slow leak with no symptom until it hurts.
        val baseRoom = FakeBaseRoom()
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM, FakeJoinedRoom(baseRoom = baseRoom, liveTimeline = fakeTimeline(failEvery = true)))
        }
        val runner = SearchBackfillRunner(
            client = client,
            store = InMemoryStore(),
            roomsProvider = { listOf(A_ROOM) },
            budget = SearchBackfillBudget(maxFailuresPerRoom = 1),
            currentTimeMillis = { 0L },
        )

        runner.runOnce()

        baseRoom.assertDestroyed()
    }

    @Test
    fun `the execution page budget stops the sweep early and is recorded`() = runTest {
        val timelines = (1..5).associate { index ->
            RoomId("!room$index:server") to fakeTimeline(reachStartAfter = Int.MAX_VALUE)
        }
        val runner = runner(
            rooms = timelines.keys.toList(),
            timelines = timelines,
            budget = SearchBackfillBudget(maxPagesPerRoom = 10, maxPagesPerExecution = 15),
        )

        val cursor = runner.runOnce()

        assertThat(cursor.pagesIssued).isAtMost(15)
        assertThat(cursor.stoppedByBudget).isTrue()
        assertThat(cursor.isDrained).isFalse()
    }

    @Test
    fun `a stored cursor resumes where it stopped and does not revisit earlier rooms`() = runTest {
        val first = fakeTimeline(reachStartAfter = 1)
        val second = fakeTimeline(reachStartAfter = 1)
        val store = InMemoryStore(
            SearchBackfillCursor(
                generation = 1,
                queue = listOf(A_ROOM.value, B_ROOM.value),
                index = 1,
            )
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM, FakeJoinedRoom(baseRoom = FakeBaseRoom(), liveTimeline = first))
            givenGetRoomResult(B_ROOM, FakeJoinedRoom(baseRoom = FakeBaseRoom(), liveTimeline = second))
        }
        val runner = SearchBackfillRunner(
            client = client,
            store = store,
            roomsProvider = { error("must not rebuild the queue while one is in progress") },
            currentTimeMillis = { 0L },
        )

        val cursor = runner.runOnce()

        assertThat(first.paginateCallCount).isEqualTo(0)
        assertThat(second.paginateCallCount).isEqualTo(1)
        assertThat(cursor.isDrained).isTrue()
    }

    @Test
    fun `a drained cursor starts a new generation`() = runTest {
        val store = InMemoryStore(
            SearchBackfillCursor(generation = 4, queue = listOf(A_ROOM.value), index = 1)
        )
        val runner = SearchBackfillRunner(
            client = FakeMatrixClient(),
            store = store,
            roomsProvider = { listOf(B_ROOM) },
            currentTimeMillis = { 0L },
        )

        val cursor = runner.runOnce()

        assertThat(cursor.generation).isEqualTo(5)
        assertThat(cursor.queue).isEqualTo(listOf(B_ROOM.value))
    }

    @Test
    fun `progress is persisted after every room`() = runTest {
        val store = InMemoryStore()
        val timelines = mapOf(
            A_ROOM to fakeTimeline(reachStartAfter = 1),
            B_ROOM to fakeTimeline(reachStartAfter = 1),
        )
        val runner = runner(rooms = listOf(A_ROOM, B_ROOM), timelines = timelines, store = store)

        runner.runOnce()

        // Two rooms plus the final write: process death costs at most one room of progress.
        assertThat(store.writes).isAtLeast(3)
        assertThat(store.getCursor()?.isDrained).isTrue()
    }

    @Test
    fun `the execution deadline stops the sweep between rooms`() = runTest {
        // Every other test freezes the clock, so without this the time budgets are never exercised
        // at all and a wrong comparison would pass silently.
        val timelines = (1..5).associate { index ->
            RoomId("!room$index:server") to fakeTimeline(reachStartAfter = 1)
        }
        // Advances 40s per reading, so the 1-minute deadline trips after the first room.
        var now = 0L
        val runner = SearchBackfillRunner(
            client = FakeMatrixClient().apply {
                timelines.forEach { (roomId, timeline) ->
                    givenGetRoomResult(roomId, FakeJoinedRoom(baseRoom = FakeBaseRoom(), liveTimeline = timeline))
                }
            },
            store = InMemoryStore(),
            roomsProvider = { timelines.keys.toList() },
            budget = SearchBackfillBudget(executionDeadline = 1.minutes),
            currentTimeMillis = { now.also { now += 40_000 } },
        )

        val cursor = runner.runOnce()

        assertThat(cursor.stoppedByBudget).isTrue()
        assertThat(cursor.isDrained).isFalse()
        // Later rooms must be left untouched for the next execution to resume into.
        assertThat(timelines.values.count { it.paginateCallCount > 0 }).isLessThan(timelines.size)
    }

    @Test
    fun `the per-room time limit stops that room and moves on`() = runTest {
        val slow = fakeTimeline(reachStartAfter = Int.MAX_VALUE)
        var now = 0L
        val runner = SearchBackfillRunner(
            client = FakeMatrixClient().apply {
                givenGetRoomResult(A_ROOM, FakeJoinedRoom(baseRoom = FakeBaseRoom(), liveTimeline = slow))
            },
            store = InMemoryStore(),
            roomsProvider = { listOf(A_ROOM) },
            budget = SearchBackfillBudget(maxPagesPerRoom = 100, maxRoomDuration = 10.seconds),
            currentTimeMillis = { now.also { now += 4_000 } },
        )

        val cursor = runner.runOnce()

        assertThat(cursor.outcomes[A_ROOM.value]).isEqualTo(RoomSweepOutcome.PAGE_CAP)
        // Time, not the page cap of 100, is what stopped it.
        assertThat(slow.paginateCallCount).isLessThan(100)
    }

    @Test
    fun `an empty room list finishes without touching any room`() = runTest {
        val runner = runner(rooms = emptyList(), timelines = emptyMap())

        val cursor = runner.runOnce()

        assertThat(cursor.queue).isEmpty()
        assertThat(cursor.finishedAt).isNotNull()
    }
}

private val A_ROOM = RoomId("!a:server")
private val B_ROOM = RoomId("!b:server")

private fun runner(
    rooms: List<RoomId>,
    timelines: Map<RoomId, CountingTimeline>,
    budget: SearchBackfillBudget = SearchBackfillBudget(),
    store: SearchBackfillStore = InMemoryStore(),
): SearchBackfillRunner {
    val client = FakeMatrixClient().apply {
        timelines.forEach { (roomId, timeline) ->
            givenGetRoomResult(roomId, FakeJoinedRoom(baseRoom = FakeBaseRoom(), liveTimeline = timeline))
        }
    }
    return SearchBackfillRunner(
        client = client,
        store = store,
        roomsProvider = { rooms },
        budget = budget,
        // Fixed clock: these tests assert page counts, never elapsed time.
        currentTimeMillis = { 0L },
    )
}

private fun fakeTimeline(
    reachStartAfter: Int = 1,
    canPaginate: Boolean = true,
    failEvery: Boolean = false,
): CountingTimeline = CountingTimeline(reachStartAfter, canPaginate, failEvery)

/**
 * Wraps [FakeTimeline] to count pagination calls, which is the only thing these tests can observe.
 */
private class CountingTimeline(
    private val reachStartAfter: Int,
    canPaginate: Boolean,
    private val failEvery: Boolean,
) : Timeline by FakeTimeline() {
    var paginateCallCount = 0
        private set

    override val backwardPaginationStatus = MutableStateFlow(
        Timeline.PaginationStatus(isPaginating = false, hasMoreToLoad = canPaginate)
    )

    override suspend fun paginate(direction: Timeline.PaginationDirection): Result<Boolean> {
        paginateCallCount++
        if (failEvery) return Result.failure(IllegalStateException("network"))
        return Result.success(paginateCallCount >= reachStartAfter)
    }
}

private class InMemoryStore(
    private var cursor: SearchBackfillCursor? = null,
) : SearchBackfillStore {
    var writes = 0
        private set

    private val flow = MutableStateFlow(cursor)

    override fun cursorFlow(): Flow<SearchBackfillCursor?> = flow

    override suspend fun getCursor(): SearchBackfillCursor? = cursor

    override suspend fun setCursor(cursor: SearchBackfillCursor) {
        writes++
        this.cursor = cursor
        flow.value = cursor
    }

    override suspend fun clear() {
        cursor = null
        flow.value = null
    }
}
