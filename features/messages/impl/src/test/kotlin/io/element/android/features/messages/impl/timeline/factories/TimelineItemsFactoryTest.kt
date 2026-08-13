/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.fixtures.aTimelineItemsFactory
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimelineItemsFactoryTest {
    @Test
    fun `messages sent close together are grouped`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, ONE_MINUTE, 2 * ONE_MINUTE),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Middle,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    @Test
    fun `messages sent more than five minutes apart are not grouped`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, 6 * ONE_MINUTE, 12 * ONE_MINUTE),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.None,
            TimelineItemGroupPosition.None,
            TimelineItemGroupPosition.None,
        ).inOrder()
    }

    @Test
    fun `a gap of more than five minutes splits the group`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, ONE_MINUTE, 8 * ONE_MINUTE, 9 * ONE_MINUTE),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Last,
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    @Test
    fun `a gap of exactly five minutes still groups`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, FIVE_MINUTES),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    @Test
    fun `messages with the same timestamp are grouped`() = runTest {
        val positions = groupPositionsOf(
            timestamps = listOf(0L, 0L, 0L),
        )
        assertThat(positions).containsExactly(
            TimelineItemGroupPosition.First,
            TimelineItemGroupPosition.Middle,
            TimelineItemGroupPosition.Last,
        ).inOrder()
    }

    private suspend fun TestScope.groupPositionsOf(timestamps: List<Long>): List<TimelineItemGroupPosition> {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = timestamps.mapIndexed { index, timestamp ->
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-$index"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    timestamp = timestamp,
                    content = aMessageContent(body = "Message $index"),
                ),
            )
        }
        var positions: List<TimelineItemGroupPosition> = emptyList()
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
                roomMembers = emptyList(),
                renderReadReceipts = false,
            )
            positions = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .map { it.groupPosition }
                .reversed()
            cancelAndIgnoreRemainingEvents()
        }
        return positions
    }

    private companion object {
        const val ONE_MINUTE = 60 * 1000L
        const val FIVE_MINUTES = 5 * ONE_MINUTE
    }
}
