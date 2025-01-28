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
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemImage
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FocusedMediaTimelineTest {
    @Test
    fun `check the returned cache data`() {
        val media = aMediaItemImage()
        val sut = createFocusedMediaTimeline(
            initialMediaItem = media,
        )
        val cache = sut.cache
        assertThat(cache.imageAndVideoItems.size).isEqualTo(3)
        assertThat(cache.fileItems.size).isEqualTo(3)
        assertThat(cache.imageAndVideoItems[1]).isEqualTo(media)
        assertThat(cache.fileItems[1]).isEqualTo(media)
    }

    @Test
    fun `when event is not found, the cache is returned`() {
        val media = aMediaItemImage(
            eventId = AN_EVENT_ID,
        )
        val sut = createFocusedMediaTimeline(
            initialMediaItem = media,
        )
        val cache = sut.orCache(
            GroupedMediaItems(
                imageAndVideoItems = persistentListOf(),
                fileItems = persistentListOf(),
            )
        )
        assertThat(cache.imageAndVideoItems.size).isEqualTo(3)
        assertThat(cache.fileItems.size).isEqualTo(3)
    }

    @Test
    fun `when event is found, the data is returned`() {
        val media = aMediaItemImage(
            eventId = AN_EVENT_ID,
        )
        val sut = createFocusedMediaTimeline(
            initialMediaItem = media,
        )
        val cache = sut.orCache(
            GroupedMediaItems(
                imageAndVideoItems = persistentListOf(media),
                fileItems = persistentListOf(),
            )
        )
        assertThat(cache.imageAndVideoItems.size).isEqualTo(1)
        assertThat(cache.fileItems).isEmpty()
    }

    @Test
    fun `getTimeline returns the timeline provided by the room`() = runTest {
        val mediaTimelineResult = lambdaRecorder<EventId?, Result<Timeline>> {
            Result.success(FakeTimeline())
        }
        val room = FakeMatrixRoom(
            mediaTimelineResult = mediaTimelineResult,
        )
        val sut = createFocusedMediaTimeline(
            room = room,
            eventId = AN_EVENT_ID,
        )
        val timeline = sut.getTimeline()
        assertThat(timeline.isSuccess).isTrue()
        mediaTimelineResult.assertions().isCalledOnce().with(value(AN_EVENT_ID))
    }

    private fun createFocusedMediaTimeline(
        room: MatrixRoom = FakeMatrixRoom(),
        eventId: EventId = AN_EVENT_ID,
        initialMediaItem: MediaItem.Event = aMediaItemImage(),
    ) = FocusedMediaTimeline(
        room = room,
        eventId = eventId,
        initialMediaItem = initialMediaItem,
    )
}
