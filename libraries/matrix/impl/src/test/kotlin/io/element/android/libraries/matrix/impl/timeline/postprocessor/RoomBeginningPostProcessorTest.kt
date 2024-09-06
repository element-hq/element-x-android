/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import org.junit.Test

class RoomBeginningPostProcessorTest {
    @Test
    fun `processor removes room creation event and self-join event from DM timeline`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event(UniqueId("m.room.create"), anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event(UniqueId("m.room.member"), anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, null, MembershipChange.JOINED))),
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.LIVE)
        val processedItems = processor.process(timelineItems, isDm = true, hasMoreToLoadBackwards = false)
        assertThat(processedItems).isEmpty()
    }

    @Test
    fun `processor removes room creation event and self-join event from DM timeline even if they're not the first items`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event(
                UniqueId("m.room.member_other"),
                anEventTimelineItem(content = RoomMembershipContent(A_USER_ID_2, null, MembershipChange.JOINED))
            ),
            MatrixTimelineItem.Event(UniqueId("m.room.create"), anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event(UniqueId("m.room.message"), anEventTimelineItem(content = aMessageContent("hi"))),
            MatrixTimelineItem.Event(UniqueId("m.room.member"), anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, null, MembershipChange.JOINED))),
        )
        val expected = listOf(
            MatrixTimelineItem.Event(
                UniqueId("m.room.member_other"),
                anEventTimelineItem(content = RoomMembershipContent(A_USER_ID_2, null, MembershipChange.JOINED))
            ),
            MatrixTimelineItem.Event(UniqueId("m.room.message"), anEventTimelineItem(content = aMessageContent("hi"))),
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.LIVE)
        val processedItems = processor.process(timelineItems, isDm = true, hasMoreToLoadBackwards = false)
        assertThat(processedItems).isEqualTo(expected)
    }

    @Test
    fun `processor will add beginning of room item if it's not a DM`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event(UniqueId("m.room.create"), anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event(UniqueId("m.room.member"), anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, null, MembershipChange.JOINED))),
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.LIVE)
        val processedItems = processor.process(timelineItems, isDm = false, hasMoreToLoadBackwards = false)
        assertThat(processedItems).isEqualTo(
            listOf(processor.createRoomBeginningItem()) + timelineItems
        )
    }

    @Test
    fun `processor will not add beginning of room item if it's not a DM and EncryptedHistoryBanner item is found`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Virtual(UniqueId("EncryptedHistoryBanner"), VirtualTimelineItem.EncryptedHistoryBanner),
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.LIVE)
        val processedItems = processor.process(timelineItems, isDm = false, hasMoreToLoadBackwards = false)
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor won't remove items if it's not at the start of the timeline`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event(UniqueId("m.room.create"), anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event(UniqueId("m.room.member"), anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, null, MembershipChange.JOINED))),
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.LIVE)
        val processedItems = processor.process(timelineItems, isDm = true, hasMoreToLoadBackwards = true)
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor won't remove the first member join event if it can't find the room creation event`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event(UniqueId("m.room.member"), anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, null, MembershipChange.JOINED))),
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.LIVE)
        val processedItems = processor.process(timelineItems, isDm = true, hasMoreToLoadBackwards = true)
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor won't remove the first member join event if it's not from the room creator`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event(UniqueId("m.room.create"), anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event(
                UniqueId("m.room.member"),
                anEventTimelineItem(content = RoomMembershipContent(A_USER_ID_2, null, MembershipChange.JOINED))
            ),
        )
        val processor = RoomBeginningPostProcessor(Timeline.Mode.LIVE)
        val processedItems = processor.process(timelineItems, isDm = true, hasMoreToLoadBackwards = true)
        assertThat(processedItems).isEqualTo(timelineItems)
    }
}
