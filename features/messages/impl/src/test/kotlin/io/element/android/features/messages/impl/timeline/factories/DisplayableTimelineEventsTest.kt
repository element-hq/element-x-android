/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import org.junit.Test

class DisplayableTimelineEventsTest {
    @Test
    fun `a custom state event is dropped`() {
        val items = listOf(aMessage("message"), aCustomStateEvent("custom"))
        assertThat(items.keepDisplayableTimelineEvents()).containsExactly(items[0])
    }

    @Test
    fun `a known state event is kept`() {
        val items = listOf(aStateEvent("topic", OtherState.RoomTopic("A topic")))
        assertThat(items.keepDisplayableTimelineEvents()).isEqualTo(items)
    }

    @Test
    fun `a day divider whose only event is a custom state event is dropped`() {
        val items = listOf(aDayDivider("day"), aCustomStateEvent("custom"))
        assertThat(items.keepDisplayableTimelineEvents()).isEmpty()
    }

    @Test
    fun `a day divider is kept when the day still has an event`() {
        val items = listOf(aDayDivider("day"), aCustomStateEvent("custom"), aMessage("message"))
        assertThat(items.keepDisplayableTimelineEvents()).containsExactly(items[0], items[2]).inOrder()
    }

    @Test
    fun `a day divider is kept when a read marker precedes the first event of the day`() {
        val items = listOf(aDayDivider("day"), aVirtual("marker", VirtualTimelineItem.ReadMarker), aMessage("message"))
        assertThat(items.keepDisplayableTimelineEvents()).isEqualTo(items)
    }

    @Test
    fun `a day divider is dropped when only a typing notification trails the day`() {
        val items = listOf(aDayDivider("day"), aVirtual("typing", VirtualTimelineItem.TypingNotification))
        assertThat(items.keepDisplayableTimelineEvents()).containsExactly(items[1])
    }

    private fun aMessage(id: String) = MatrixTimelineItem.Event(
        uniqueId = UniqueId(id),
        event = anEventTimelineItem(content = aMessageContent(body = id)),
    )

    private fun aCustomStateEvent(id: String) = aStateEvent(id, OtherState.Custom("com.example.custom"))

    private fun aStateEvent(id: String, state: OtherState) = MatrixTimelineItem.Event(
        uniqueId = UniqueId(id),
        event = anEventTimelineItem(content = StateContent(stateKey = "", content = state)),
    )

    private fun aDayDivider(id: String) = aVirtual(id, VirtualTimelineItem.DayDivider(0L))

    private fun aVirtual(id: String, virtual: VirtualTimelineItem) = MatrixTimelineItem.Virtual(
        uniqueId = UniqueId(id),
        virtual = virtual,
    )
}
