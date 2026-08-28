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
import io.element.android.features.messages.impl.timeline.model.TimelineItemThreadInfo
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.EventThreadInfo
import io.element.android.libraries.matrix.api.timeline.item.ThreadSummary
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.room.aRoomMember
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.aRedactedContent
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

    @Test
    fun `redacted message keeps its thread info`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    timestamp = 0L,
                    content = aRedactedContent(
                        threadInfo = EventThreadInfo.ThreadResponse(A_THREAD_ID),
                    ),
                ),
            )
        )
        var threadInfo: TimelineItemThreadInfo? = null
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
                roomMembers = emptyList(),
                renderReadReceipts = false,
            )
            threadInfo = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .firstOrNull()
                ?.threadInfo
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(threadInfo).isEqualTo(TimelineItemThreadInfo.ThreadResponse(A_THREAD_ID))
    }

    @Test
    fun `a key verification request is not rendered`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    content = aMessageContent(
                        body = "Alice is requesting to verify your key, but your client does not support in-chat key verification.",
                        messageType = OtherMessageType(
                            msgType = "m.key.verification.request",
                            body = "Alice is requesting to verify your key, but your client does not support in-chat key verification.",
                        ),
                    ),
                ),
            ),
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-1"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    content = aMessageContent(body = "A regular message"),
                ),
            ),
        )
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
                roomMembers = emptyList(),
                renderReadReceipts = false,
            )
            val bodies = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .map { (it.content as TimelineItemTextContent).body }
            assertThat(bodies).containsExactly("A regular message")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a deleted message keeps the summary of the thread it heads`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    content = aRedactedContent(
                        threadInfo = EventThreadInfo.ThreadRoot(
                            summary = ThreadSummary(latestEvent = AsyncData.Uninitialized, numberOfReplies = 3),
                        ),
                    ),
                ),
            ),
        )
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
                roomMembers = emptyList(),
                renderReadReceipts = false,
            )
            val threadInfo = awaitItem()
                .filterIsInstance<TimelineItem.Event>()
                .single()
                .threadInfo
            assertThat(threadInfo).isInstanceOf(TimelineItemThreadInfo.ThreadRoot::class.java)
            assertThat((threadInfo as TimelineItemThreadInfo.ThreadRoot).summary.numberOfReplies).isEqualTo(3)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a sender the timeline knows nothing about is named from the room member list`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    senderProfile = ProfileDetails.Unavailable,
                    content = aMessageContent(body = "A regular message"),
                ),
            ),
        )
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
                roomMembers = listOf(aRoomMember(userId = A_USER_ID, displayName = "Alice")),
                renderReadReceipts = false,
            )
            val event = awaitItem().filterIsInstance<TimelineItem.Event>().single()
            assertThat(event.senderProfile).isEqualTo(
                ProfileDetails.Ready(
                    displayName = "Alice",
                    displayNameAmbiguous = false,
                    avatarUrl = null,
                    displayedStatus = null,
                )
            )
            assertThat(event.senderAvatar.name).isEqualTo("Alice")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a sender the room member list knows nothing about keeps the profile the timeline gave`() = runTest {
        val factory = aTimelineItemsFactory(
            config = TimelineItemsFactoryConfig(
                computeReadReceipts = false,
                computeReactions = false,
            )
        )
        val items = listOf(
            MatrixTimelineItem.Event(
                uniqueId = UniqueId("event-0"),
                event = anEventTimelineItem(
                    sender = A_USER_ID,
                    senderProfile = ProfileDetails.Unavailable,
                    content = aMessageContent(body = "A regular message"),
                ),
            ),
        )
        factory.timelineItems.test {
            factory.replaceWith(
                timelineItems = items,
                roomMembers = listOf(aRoomMember(userId = A_USER_ID_2, displayName = "Bob")),
                renderReadReceipts = false,
            )
            val event = awaitItem().filterIsInstance<TimelineItem.Event>().single()
            assertThat(event.senderProfile).isEqualTo(ProfileDetails.Unavailable)
            cancelAndIgnoreRemainingEvents()
        }
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
