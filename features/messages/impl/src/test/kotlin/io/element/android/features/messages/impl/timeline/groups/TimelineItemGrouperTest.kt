/*
 * Copyright (c) 2023 New Vector Ltd
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
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class TimelineItemGrouperTest {
    private val sut = TimelineItemGrouper()

    private val aGroupableItem = TimelineItem.Event(
        id = "0",
        senderId = A_USER_ID,
        senderAvatar = anAvatarData(),
        senderDisplayName = "",
        content = TimelineItemStateEventContent(body = "a state event"),
        reactionsState = aTimelineItemReactions(count = 0),
        readReceiptState = TimelineItemReadReceipts(emptyList<ReadReceiptData>().toImmutableList()),
        localSendState = LocalEventSendState.Sent(AN_EVENT_ID),
        isEditable = false,
        inReplyTo = null,
        isThreaded = false,
        debugInfo = aTimelineItemDebugInfo(),
        origin = null
    )
    private val aNonGroupableItem = aMessageEvent()
    private val aNonGroupableItemNoEvent = TimelineItem.Virtual("virtual", aTimelineItemDaySeparatorModel("Today"))

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
                aGroupableItem.copy(id = "1"),
                aGroupableItem.copy(id = "0"),
            ),
        )
        assertThat(result).isEqualTo(
            listOf(
                TimelineItem.GroupedEvents(
                    id = computeGroupIdWith(aGroupableItem),
                    events = listOf(
                        aGroupableItem.copy("0"),
                        aGroupableItem.copy(id = "1"),
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
            aGroupableItem.copy(id = "1"),
            aGroupableItem.copy(id = "2")
        )
        val expectedGroupId = sut.group(groupableItems).first().identifier()
        groupableItems.add(0, aGroupableItem.copy("3"))
        groupableItems.add(2, aGroupableItem.copy("4"))
        groupableItems.add(aGroupableItem.copy("5"))
        val actualGroupId = sut.group(groupableItems).first().identifier()
        // Then
        assertThat(actualGroupId).isEqualTo(expectedGroupId)
    }
}
