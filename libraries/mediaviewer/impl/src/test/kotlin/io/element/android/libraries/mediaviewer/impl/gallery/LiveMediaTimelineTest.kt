/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LiveMediaTimelineTest {
    @Test
    fun `LiveMediaTimeline cache is always null`() = runTest {
        val sut = createLiveMediaTimeline()
        assertThat<GroupedMediaItems?>(sut.cache).isNull()
    }

    @Test
    fun `getTimeline returns the timeline provided by the room, then from cache`() = runTest {
        val mediaTimelineResult = lambdaRecorder<EventId?, Result<Timeline>> {
            Result.success(FakeTimeline())
        }
        val room = FakeMatrixRoom(
            mediaTimelineResult = mediaTimelineResult,
        )
        val sut = createLiveMediaTimeline(
            room = room,
        )
        val timeline = sut.getTimeline()
        assertThat(timeline.isSuccess).isTrue()
        mediaTimelineResult.assertions().isCalledOnce().with(value(null))
        val timeline2 = sut.getTimeline()
        assertThat(timeline2.isSuccess).isTrue()
        // No called another time
        mediaTimelineResult.assertions().isCalledOnce()
    }

    private fun createLiveMediaTimeline(
        room: MatrixRoom = FakeMatrixRoom(),
    ) = LiveMediaTimeline(
        room = room,
    )
}
