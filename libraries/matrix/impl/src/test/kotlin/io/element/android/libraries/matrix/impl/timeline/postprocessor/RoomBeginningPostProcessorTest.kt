/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.A_USER_ID
import org.junit.Test

class RoomBeginningPostProcessorTest {
    @Test
    fun `processor returns empty list when empty list is provided`() {
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(
            items = emptyList(),
            isDm = true,
            roomCreator = A_USER_ID,
            hasMoreToLoadBackwards = false,
        )
        assertThat(processedItems).isEmpty()
    }

    @Test
    fun `processor returns the provided list when it only contains a message`() {
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(
            items = listOf(messageEvent),
            isDm = true,
            roomCreator = A_USER_ID,
            hasMoreToLoadBackwards = false,
        )
        assertThat(processedItems).isEqualTo(listOf(messageEvent))
    }

    @Test
    fun `processor returns the provided list when it only contains a message and the roomCreator is not provided`() {
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(
            items = listOf(messageEvent),
            isDm = true,
            roomCreator = null,
            hasMoreToLoadBackwards = false,
        )
        assertThat(processedItems).isEqualTo(listOf(messageEvent))
    }

    @Test
    fun `processor removes room creation event and self-join event from DM timeline`() {
        val timelineItems = listOf(
            timelineStartEvent,
            roomCreateEvent,
            roomCreatorJoinEvent,
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(
            items = timelineItems,
            isDm = true,
            roomCreator = A_USER_ID,
            hasMoreToLoadBackwards = false,
        )
        assertThat(processedItems).containsExactly(timelineStartEvent)
    }

    @Test
    fun `processor does not remove anything with PINNED_EVENTS mode`() {
        val timelineItems = listOf(
            roomCreateEvent,
            roomCreatorJoinEvent,
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.PinnedEvents)
        val processedItems = processor.process(
            items = timelineItems,
            isDm = true,
            roomCreator = A_USER_ID,
            hasMoreToLoadBackwards = false,
        )
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor removes room creation event and self-join event from DM timeline even if they're not the first items`() {
        val timelineItems = listOf(
            otherMemberJoinEvent,
            roomCreateEvent,
            messageEvent,
            roomCreatorJoinEvent,
        )
        val expected = listOf(
            otherMemberJoinEvent,
            messageEvent,
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(timelineItems, isDm = true, roomCreator = A_USER_ID, hasMoreToLoadBackwards = false)
        assertThat(processedItems).isEqualTo(expected)
    }

    @Test
    fun `processor removes items event it's not at the start of the timeline`() {
        val timelineItems = listOf(
            roomCreateEvent,
            roomCreatorJoinEvent,
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(timelineItems, isDm = true, roomCreator = A_USER_ID, hasMoreToLoadBackwards = true)
        assertThat(processedItems).isEmpty()
    }

    @Test
    fun `processor removes the first member join event if it matches the roomCreator parameter`() {
        val timelineItems = listOf(
            roomCreatorJoinEvent,
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(timelineItems, isDm = true, roomCreator = A_USER_ID, hasMoreToLoadBackwards = true)
        assertThat(processedItems).isEmpty()
    }

    @Test
    fun `processor won't remove the first member join event if it's not from the room creator`() {
        val timelineItems = listOf(
            roomCreateEvent,
            otherMemberJoinEvent,
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.Live)
        val processedItems = processor.process(timelineItems, isDm = true, roomCreator = A_USER_ID, hasMoreToLoadBackwards = true)
        assertThat(processedItems).isEqualTo(listOf(otherMemberJoinEvent))
    }
}
