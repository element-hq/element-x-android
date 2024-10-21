/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.groups

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemDebugInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateEventContent
import io.element.android.features.messages.impl.timeline.model.virtual.aTimelineItemDaySeparatorModel
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.ui.messages.reply.aProfileTimelineDetailsReady
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class TimelineItemGrouperTest {
    private val sut = TimelineItemGrouper()

    private val aGroupableItem = TimelineItem.Event(
        id = UniqueId("0"),
        senderId = A_USER_ID,
        senderAvatar = anAvatarData(),
        senderProfile = aProfileTimelineDetailsReady(displayName = ""),
        content = TimelineItemStateEventContent(body = "a state event"),
        reactionsState = aTimelineItemReactions(count = 0),
        readReceiptState = TimelineItemReadReceipts(emptyList<ReadReceiptData>().toImmutableList()),
        localSendState = LocalEventSendState.Sent(AN_EVENT_ID),
        isEditable = false,
        canBeRepliedTo = false,
        inReplyTo = null,
        isThreaded = false,
        origin = null,
        timelineItemDebugInfoProvider = { aTimelineItemDebugInfo() },
        messageShieldProvider = { null },
    )
    private val aNonGroupableItem = aMessageEvent()
    private val aNonGroupableItemNoEvent = TimelineItem.Virtual(UniqueId("virtual"), aTimelineItemDaySeparatorModel("Today"))

    @Test
    fun `test empty`() {
        val result = sut.group(emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun `test non groupables`() {
        val result = sut.group(
            listOf(
                aNonGroupableItem,
                aNonGroupableItem,
            ),
        )
        assertThat(result).isEqualTo(
            listOf(
                aNonGroupableItem,
                aNonGroupableItem,
            )
        )
    }

    @Test
    fun `test groupables and ensure reordering`() {
        val result = sut.group(
            listOf(
                aGroupableItem.copy(id = UniqueId("1")),
                aGroupableItem.copy(id = UniqueId("0")),
            ),
        )
        assertThat(result).isEqualTo(
            listOf(
                TimelineItem.GroupedEvents(
                    id = computeGroupIdWith(aGroupableItem),
                    events = listOf(
                        aGroupableItem.copy(id = UniqueId("0")),
                        aGroupableItem.copy(id = UniqueId("1")),
                    ).toImmutableList(),
                    aggregatedReadReceipts = emptyList<ReadReceiptData>().toImmutableList(),
                ),
            )
        )
    }

    @Test
    fun `test 1 groupable, not group must be created`() {
        val listsToTest = listOf(
            listOf(aGroupableItem),
            listOf(aGroupableItem, aNonGroupableItem),
            listOf(aGroupableItem, aNonGroupableItemNoEvent),
            listOf(aNonGroupableItem, aGroupableItem),
            listOf(aNonGroupableItemNoEvent, aGroupableItem),
            listOf(aNonGroupableItem, aGroupableItem, aNonGroupableItem),
            listOf(aNonGroupableItemNoEvent, aGroupableItem, aNonGroupableItemNoEvent),
            listOf(aGroupableItem, aNonGroupableItem, aGroupableItem),
            listOf(aGroupableItem, aNonGroupableItemNoEvent, aGroupableItem),
            listOf(aNonGroupableItem),
            listOf(aNonGroupableItemNoEvent),
        )
        listsToTest.forEach { listToTest ->
            val result = sut.group(listToTest)
            assertThat(result).isEqualTo(listToTest)
        }
    }

    @Test
    fun `test 3 blocks`() {
        val result = sut.group(
            listOf(
                aGroupableItem,
                aGroupableItem,
                aNonGroupableItem,
                aGroupableItem,
                aGroupableItem,
                aGroupableItem,
            ),
        )
        assertThat(result).isEqualTo(
            listOf(
                TimelineItem.GroupedEvents(
                    id = computeGroupIdWith(aGroupableItem),
                    events = listOf(
                        aGroupableItem,
                        aGroupableItem,
                    ).toImmutableList(),
                    aggregatedReadReceipts = emptyList<ReadReceiptData>().toImmutableList(),
                ),
                aNonGroupableItem,
                TimelineItem.GroupedEvents(
                    id = computeGroupIdWith(aGroupableItem),
                    events = listOf(
                        aGroupableItem,
                        aGroupableItem,
                        aGroupableItem,
                    ).toImmutableList(),
                    aggregatedReadReceipts = emptyList<ReadReceiptData>().toImmutableList(),
                )
            )
        )
    }

    @Test
    fun `when calling multiple time the method group over a growing list of groupable items, then groupId is stable`() {
        // When
        val groupableItems = mutableListOf(
            aGroupableItem.copy(id = UniqueId("1")),
            aGroupableItem.copy(id = UniqueId("2"))
        )
        val expectedGroupId = sut.group(groupableItems).first().identifier()
        groupableItems.add(0, aGroupableItem.copy(UniqueId("3")))
        groupableItems.add(2, aGroupableItem.copy(UniqueId("4")))
        groupableItems.add(aGroupableItem.copy(UniqueId("5")))
        val actualGroupId = sut.group(groupableItems).first().identifier()
        // Then
        assertThat(actualGroupId).isEqualTo(expectedGroupId)
    }
}
