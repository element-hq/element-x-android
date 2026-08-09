/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.fixtures.aTimelineItemContentFactory
import io.element.android.features.messages.impl.messagesummary.FakeMessageSummaryFormatter
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemEventFactory
import io.element.android.features.messages.impl.timeline.factories.virtual.TimelineItemDaySeparatorFactory
import io.element.android.features.messages.impl.timeline.factories.virtual.TimelineItemVirtualFactory
import io.element.android.features.messages.impl.timeline.groups.TimelineItemGrouper
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.libraries.matrix.test.timeline.item.event.aRoomMembershipContent
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

private const val A_STATE_EVENT_BODY = "a state event"

class TimelineItemsFactoryTest {
    @Test
    fun `a state event which cannot be formatted is not rendered`() = runTest {
        val sut = aTimelineItemsFactory()
        sut.timelineItems.test {
            sut.replaceWith(
                timelineItems = listOf(
                    aMessageTimelineItem("0"),
                    aRoomMembershipTimelineItem("1", change = MembershipChange.JOINED),
                    aRoomMembershipTimelineItem("2", change = null),
                ),
                roomMembers = emptyList(),
                renderReadReceipts = false,
            )
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result.map { (it as TimelineItem.Event).id }).containsExactly(UniqueId("1"), UniqueId("0"))
            assertThat(result.stateEventBodies()).containsExactly(A_STATE_EVENT_BODY)
        }
    }

    @Test
    fun `a state event which cannot be formatted is not counted in a group of state events`() = runTest {
        val sut = aTimelineItemsFactory()
        sut.timelineItems.test {
            sut.replaceWith(
                timelineItems = listOf(
                    aMessageTimelineItem("0"),
                    aRoomMembershipTimelineItem("1", change = MembershipChange.JOINED),
                    aRoomMembershipTimelineItem("2", change = MembershipChange.LEFT),
                    aRoomMembershipTimelineItem("3", change = null),
                ),
                roomMembers = emptyList(),
                renderReadReceipts = false,
            )
            val result = awaitItem()
            assertThat(result).hasSize(2)
            val group = result.first() as TimelineItem.GroupedEvents
            assertThat(group.events.map { it.id }).containsExactly(UniqueId("1"), UniqueId("2"))
        }
    }

    private fun List<TimelineItem>.stateEventBodies(): List<String> {
        return filterIsInstance<TimelineItem.Event>()
            .map { it.content }
            .filterIsInstance<TimelineItemStateContent>()
            .map { it.body }
    }

    private fun aMessageTimelineItem(uniqueId: String) = MatrixTimelineItem.Event(
        uniqueId = UniqueId(uniqueId),
        event = anEventTimelineItem(content = aMessageContent()),
    )

    private fun aRoomMembershipTimelineItem(uniqueId: String, change: MembershipChange?) = MatrixTimelineItem.Event(
        uniqueId = UniqueId(uniqueId),
        event = anEventTimelineItem(content = aRoomMembershipContent(change = change)),
    )

    private fun TestScope.aTimelineItemsFactory(): TimelineItemsFactory {
        val matrixClient = FakeMatrixClient()
        return TimelineItemsFactory(
            config = TimelineItemsFactoryConfig(computeReadReceipts = false, computeReactions = false),
            eventItemFactoryCreator = object : TimelineItemEventFactory.Creator {
                override fun create(config: TimelineItemsFactoryConfig): TimelineItemEventFactory {
                    return TimelineItemEventFactory(
                        contentFactory = aTimelineItemContentFactory(
                            timelineEventFormatter = aFakeTimelineEventFormatter(),
                            matrixClient = matrixClient,
                        ),
                        matrixClient = matrixClient,
                        dateFormatter = FakeDateFormatter(),
                        permalinkParser = FakePermalinkParser(),
                        config = config,
                        summaryFormatter = FakeMessageSummaryFormatter(),
                    )
                }
            },
            dispatchers = testCoroutineDispatchers(),
            virtualItemFactory = TimelineItemVirtualFactory(
                daySeparatorFactory = TimelineItemDaySeparatorFactory(FakeDateFormatter()),
            ),
            timelineItemGrouper = TimelineItemGrouper(),
        )
    }

    private fun aFakeTimelineEventFormatter() = object : TimelineEventFormatter {
        override fun format(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): CharSequence? {
            return if (content is RoomMembershipContent && content.change == null) null else A_STATE_EVENT_BODY
        }
    }
}
