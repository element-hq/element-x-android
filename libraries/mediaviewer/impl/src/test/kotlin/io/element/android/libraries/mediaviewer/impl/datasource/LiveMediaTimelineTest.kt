/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
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
        val createTimelineResult = lambdaRecorder<CreateTimelineParams, Result<Timeline>> {
            Result.success(FakeTimeline())
        }
        val room = FakeJoinedRoom(
            createTimelineResult = createTimelineResult,
        )
        val sut = createLiveMediaTimeline(
            room = room,
        )
        val timeline = sut.getTimeline()
        assertThat(timeline.isSuccess).isTrue()
        createTimelineResult.assertions().isCalledOnce().with(value(CreateTimelineParams.MediaOnly))
        val timeline2 = sut.getTimeline()
        assertThat(timeline2.isSuccess).isTrue()
        // No called another time
        createTimelineResult.assertions().isCalledOnce()
    }

    private fun createLiveMediaTimeline(
        room: JoinedRoom = FakeJoinedRoom(),
    ) = LiveMediaTimeline(
        room = room,
    )
}
