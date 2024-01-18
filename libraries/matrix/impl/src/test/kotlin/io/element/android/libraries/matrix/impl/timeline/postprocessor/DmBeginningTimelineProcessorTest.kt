/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import org.junit.Test

class DmBeginningTimelineProcessorTest {
    @Test
    fun `processor removes room creation event and self-join event from DM timeline`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event("m.room.create", anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event("m.room.member", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, MembershipChange.JOINED))),
        )
        val processor = DmBeginningTimelineProcessor()
        val processedItems = processor.process(timelineItems, isDm = true, isAtStartOfTimeline = true)
        assertThat(processedItems).isEmpty()
    }

    @Test
    fun `processor removes room creation event and self-join event from DM timeline even if they're not the first items`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event("m.room.member_other", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID_2, MembershipChange.JOINED))),
            MatrixTimelineItem.Event("m.room.create", anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event("m.room.message", anEventTimelineItem(content = aMessageContent("hi"))),
            MatrixTimelineItem.Event("m.room.member", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, MembershipChange.JOINED))),
        )
        val expected = listOf(
            MatrixTimelineItem.Event("m.room.member_other", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID_2, MembershipChange.JOINED))),
            MatrixTimelineItem.Event("m.room.message", anEventTimelineItem(content = aMessageContent("hi"))),
        )
        val processor = DmBeginningTimelineProcessor()
        val processedItems = processor.process(timelineItems, isDm = true, isAtStartOfTimeline = true)
        assertThat(processedItems).isEqualTo(expected)
    }

    @Test
    fun `processor won't remove items if it's not a DM`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event("m.room.create", anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event("m.room.member", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, MembershipChange.JOINED))),
        )
        val processor = DmBeginningTimelineProcessor()
        val processedItems = processor.process(timelineItems, isDm = false, isAtStartOfTimeline = true)
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor won't remove items if it's not at the start of the timeline`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event("m.room.create", anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event("m.room.member", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, MembershipChange.JOINED))),
        )
        val processor = DmBeginningTimelineProcessor()
        val processedItems = processor.process(timelineItems, isDm = true, isAtStartOfTimeline = false)
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor won't remove the first member join event if it can't find the room creation event`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event("m.room.member", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID, MembershipChange.JOINED))),
        )
        val processor = DmBeginningTimelineProcessor()
        val processedItems = processor.process(timelineItems, isDm = true, isAtStartOfTimeline = false)
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor won't remove the first member join event if it's not from the room creator`() {
        val timelineItems = listOf(
            MatrixTimelineItem.Event("m.room.create", anEventTimelineItem(sender = A_USER_ID, content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event("m.room.member", anEventTimelineItem(content = RoomMembershipContent(A_USER_ID_2, MembershipChange.JOINED))),
        )
        val processor = DmBeginningTimelineProcessor()
        val processedItems = processor.process(timelineItems, isDm = true, isAtStartOfTimeline = false)
        assertThat(processedItems).isEqualTo(timelineItems)
    }
}
