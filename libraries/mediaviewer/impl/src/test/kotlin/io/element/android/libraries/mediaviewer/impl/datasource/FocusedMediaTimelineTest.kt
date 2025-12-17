/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
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
        val createTimelineResult = lambdaRecorder<CreateTimelineParams, Result<Timeline>> {
            Result.success(FakeTimeline())
        }
        val room = FakeJoinedRoom(
            createTimelineResult = createTimelineResult,
        )
        val sut = createFocusedMediaTimeline(
            room = room,
            eventId = AN_EVENT_ID,
        )
        val timeline = sut.getTimeline()
        assertThat(timeline.isSuccess).isTrue()
        createTimelineResult.assertions().isCalledOnce().with(value(CreateTimelineParams.MediaOnlyFocused(AN_EVENT_ID)))
    }

    @Test
    fun `getTimeline returns the timeline provided by the room for pinned Events`() = runTest {
        val createTimelineResult = lambdaRecorder<CreateTimelineParams, Result<Timeline>> {
            Result.success(FakeTimeline())
        }
        val room = FakeJoinedRoom(
            createTimelineResult = createTimelineResult,
        )
        val sut = createFocusedMediaTimeline(
            room = room,
            eventId = AN_EVENT_ID,
            onlyPinnedEvent = true,
        )
        val timeline = sut.getTimeline()
        assertThat(timeline.isSuccess).isTrue()
        createTimelineResult.assertions().isCalledOnce().with(value(CreateTimelineParams.PinnedOnly))
    }

    private fun createFocusedMediaTimeline(
        room: JoinedRoom = FakeJoinedRoom(),
        eventId: EventId = AN_EVENT_ID,
        initialMediaItem: MediaItem.Event = aMediaItemImage(),
        onlyPinnedEvent: Boolean = false,
    ) = FocusedMediaTimeline(
        room = room,
        eventId = eventId,
        initialMediaItem = initialMediaItem,
        onlyPinnedEvents = onlyPinnedEvent,
    )
}
