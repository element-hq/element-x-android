/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.data

import com.google.common.truth.Truth.assertThat
import io.element.android.features.poll.impl.aPollTimelineItems
import io.element.android.features.poll.impl.anOngoingPollContent
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PollRepositoryTest {
    private val pollContent = anOngoingPollContent()

    @Test
    fun `given a thread, getPoll closes the created timeline`() = runTest {
        val threadedTimeline = aThreadedTimeline()
        val room = aRoomCreating(threadedTimeline)
        val sut = createPollRepository(room, Timeline.Mode.Thread(A_THREAD_ID))

        val result = sut.getPoll(AN_EVENT_ID)

        assertThat(result.getOrNull()).isEqualTo(pollContent)
        assertThat(threadedTimeline.closeCounter).isEqualTo(1)
        assertThat((room.liveTimeline as FakeTimeline).closeCounter).isEqualTo(0)
    }

    @Test
    fun `given a thread, savePoll closes the created timeline`() = runTest {
        val threadedTimeline = aThreadedTimeline().apply {
            createPollLambda = { _, _, _, _ -> Result.success(Unit) }
        }
        val room = aRoomCreating(threadedTimeline)
        val sut = createPollRepository(room, Timeline.Mode.Thread(A_THREAD_ID))

        val result = sut.savePoll(
            existingPollId = null,
            question = "A question",
            answers = listOf("Yes", "No"),
            pollKind = PollKind.Disclosed,
            maxSelections = 1,
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(threadedTimeline.closeCounter).isEqualTo(1)
    }

    @Test
    fun `given a thread, savePoll of an existing poll closes the created timeline`() = runTest {
        val threadedTimeline = aThreadedTimeline().apply {
            editPollLambda = { _, _, _, _, _ -> Result.success(Unit) }
        }
        val room = aRoomCreating(threadedTimeline)
        val sut = createPollRepository(room, Timeline.Mode.Thread(A_THREAD_ID))

        val result = sut.savePoll(
            existingPollId = AN_EVENT_ID,
            question = "A question",
            answers = listOf("Yes", "No"),
            pollKind = PollKind.Disclosed,
            maxSelections = 1,
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(threadedTimeline.closeCounter).isEqualTo(1)
    }

    @Test
    fun `given a thread, deletePoll closes the created timeline`() = runTest {
        val threadedTimeline = aThreadedTimeline().apply {
            redactEventLambda = { _, _ -> Result.success(Unit) }
        }
        val room = aRoomCreating(threadedTimeline)
        val sut = createPollRepository(room, Timeline.Mode.Thread(A_THREAD_ID))

        val result = sut.deletePoll(AN_EVENT_ID)

        assertThat(result.isSuccess).isTrue()
        assertThat(threadedTimeline.closeCounter).isEqualTo(1)
    }

    @Test
    fun `given a thread, the created timeline is closed even if the operation fails`() = runTest {
        val error = RuntimeException("Failed to send the poll")
        val threadedTimeline = aThreadedTimeline().apply {
            createPollLambda = { _, _, _, _ -> Result.failure(error) }
        }
        val room = aRoomCreating(threadedTimeline)
        val sut = createPollRepository(room, Timeline.Mode.Thread(A_THREAD_ID))

        val result = sut.savePoll(
            existingPollId = null,
            question = "A question",
            answers = listOf("Yes", "No"),
            pollKind = PollKind.Disclosed,
            maxSelections = 1,
        )

        assertThat(result.exceptionOrNull()).isEqualTo(error)
        assertThat(threadedTimeline.closeCounter).isEqualTo(1)
    }

    @Test
    fun `given a thread, if the timeline cannot be created the error is returned`() = runTest {
        val error = RuntimeException("Failed to create the timeline")
        val room = FakeJoinedRoom(
            liveTimeline = aLiveTimeline(),
            createTimelineResult = { Result.failure(error) },
        )
        val sut = createPollRepository(room, Timeline.Mode.Thread(A_THREAD_ID))

        val result = sut.deletePoll(AN_EVENT_ID)

        assertThat(result.exceptionOrNull()).isEqualTo(error)
    }

    @Test
    fun `given a live timeline, savePoll of a new poll uses the live timeline and does not close it`() = runTest {
        val liveTimeline = aLiveTimeline().apply {
            createPollLambda = { _, _, _, _ -> Result.success(Unit) }
        }
        val room = FakeJoinedRoom(liveTimeline = liveTimeline)
        val sut = createPollRepository(room, Timeline.Mode.Live)

        val result = sut.savePoll(
            existingPollId = null,
            question = "A question",
            answers = listOf("Yes", "No"),
            pollKind = PollKind.Disclosed,
            maxSelections = 1,
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(liveTimeline.closeCounter).isEqualTo(0)
    }

    @Test
    fun `given a live timeline, getPoll closes the timeline focused on the poll`() = runTest {
        val focusedTimeline = aFocusedTimeline()
        val room = aRoomCreating(focusedTimeline, CreateTimelineParams.Focused(AN_EVENT_ID))
        val sut = createPollRepository(room, Timeline.Mode.Live)

        val result = sut.getPoll(AN_EVENT_ID)

        assertThat(result.getOrNull()).isEqualTo(pollContent)
        assertThat(focusedTimeline.closeCounter).isEqualTo(1)
        assertThat((room.liveTimeline as FakeTimeline).closeCounter).isEqualTo(0)
    }

    @Test
    fun `given a live timeline, savePoll of an existing poll closes the timeline focused on the poll`() = runTest {
        val focusedTimeline = aFocusedTimeline().apply {
            editPollLambda = { _, _, _, _, _ -> Result.success(Unit) }
        }
        val room = aRoomCreating(focusedTimeline, CreateTimelineParams.Focused(AN_EVENT_ID))
        val sut = createPollRepository(room, Timeline.Mode.Live)

        val result = sut.savePoll(
            existingPollId = AN_EVENT_ID,
            question = "A question",
            answers = listOf("Yes", "No"),
            pollKind = PollKind.Disclosed,
            maxSelections = 1,
        )

        assertThat(result.isSuccess).isTrue()
        assertThat(focusedTimeline.closeCounter).isEqualTo(1)
    }

    @Test
    fun `given a live timeline, deletePoll closes the timeline focused on the poll`() = runTest {
        val focusedTimeline = aFocusedTimeline().apply {
            redactEventLambda = { _, _ -> Result.success(Unit) }
        }
        val room = aRoomCreating(focusedTimeline, CreateTimelineParams.Focused(AN_EVENT_ID))
        val sut = createPollRepository(room, Timeline.Mode.Live)

        val result = sut.deletePoll(AN_EVENT_ID)

        assertThat(result.isSuccess).isTrue()
        assertThat(focusedTimeline.closeCounter).isEqualTo(1)
    }

    private fun aLiveTimeline() = FakeTimeline(
        timelineItems = aPollTimelineItems(mapOf(AN_EVENT_ID to pollContent)),
    )

    private fun aThreadedTimeline() = FakeTimeline(
        timelineItems = aPollTimelineItems(mapOf(AN_EVENT_ID to pollContent)),
        mode = Timeline.Mode.Thread(A_THREAD_ID),
    )

    private fun aFocusedTimeline() = FakeTimeline(
        timelineItems = aPollTimelineItems(mapOf(AN_EVENT_ID to pollContent)),
        mode = Timeline.Mode.FocusedOnEvent(AN_EVENT_ID),
    )

    private fun aRoomCreating(
        timeline: FakeTimeline,
        expectedParams: CreateTimelineParams = CreateTimelineParams.Threaded(A_THREAD_ID),
    ) = FakeJoinedRoom(
        liveTimeline = aLiveTimeline(),
        createTimelineResult = { params ->
            assertThat(params).isEqualTo(expectedParams)
            Result.success(timeline)
        },
    )

    private fun createPollRepository(
        room: FakeJoinedRoom,
        timelineMode: Timeline.Mode,
    ) = PollRepository(
        room = room,
        timelineMode = timelineMode,
    )
}
