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
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import org.junit.Test

class LastForwardIndicatorsPostProcessorTest {
    @Test
    fun `LastForwardIndicatorsPostProcessor does not alter the items with mode not FOCUSED_ON_EVENT`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.LIVE)
        val result = sut.process(listOf(messageEvent), true)
        assertThat(result).containsExactly(messageEvent)
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor does not alter the items with mode FOCUSED_ON_EVENT but timeline not initialized`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FOCUSED_ON_EVENT)
        val result = sut.process(listOf(messageEvent), false)
        assertThat(result).containsExactly(messageEvent)
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FOCUSED_ON_EVENT)
        val result = sut.process(listOf(messageEvent), true)
        assertThat(result).containsExactly(
            messageEvent,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items on empty list`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FOCUSED_ON_EVENT)
        val result = sut.process(listOf(), true)
        assertThat(result).containsExactly(
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_fake_id"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items but does not alter the list if called a second time`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FOCUSED_ON_EVENT)
        // Process a first time
        sut.process(listOf(messageEvent), true)
        // Process a second time with the same Event
        val result = sut.process(listOf(messageEvent), true)
        assertThat(result).containsExactly(
            messageEvent,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }

    @Test
    fun `LastForwardIndicatorsPostProcessor add virtual items each time it is called with new Events`() {
        val sut = LastForwardIndicatorsPostProcessor(Timeline.Mode.FOCUSED_ON_EVENT)
        // Process a first time
        sut.process(listOf(dayEvent, messageEvent), true)
        // Process a second time with the same Event
        val result = sut.process(listOf(dayEvent, messageEvent, messageEvent2), true)
        assertThat(result).containsExactly(
            dayEvent,
            messageEvent,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            ),
            messageEvent2,
            MatrixTimelineItem.Virtual(
                uniqueId = UniqueId("last_forward_indicator_${messageEvent2.uniqueId}"),
                virtual = VirtualTimelineItem.LastForwardIndicator
            )
        )
    }
}
