/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.fixtures.aMessageEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemDebugInfo
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemReadMarkerModel
import io.element.android.features.messages.impl.timeline.model.virtual.aTimelineItemDaySeparatorModel
import io.element.android.libraries.designsystem.components.avatar.anAvatarData
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.core.FakeSendHandle
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class TimelineItemsFactoryTest {
    private val anEvent = TimelineItem.Event(
        id = UniqueId("event"),
        eventId = AN_EVENT_ID,
        senderId = A_USER_ID,
        senderAvatar = anAvatarData(),
        senderProfile = ProfileDetails.Ready(displayName = "User", displayNameAmbiguous = false, avatarUrl = null),
        content = aMessageEvent().content,
        reactionsState = aTimelineItemReactions(count = 0),
        readReceiptState = TimelineItemReadReceipts(emptyList<ReadReceiptData>().toImmutableList()),
        localSendState = LocalEventSendState.Sent(AN_EVENT_ID),
        isEditable = false,
        canBeRepliedTo = false,
        inReplyTo = null,
        threadInfo = null,
        origin = null,
        timelineItemDebugInfoProvider = { aTimelineItemDebugInfo() },
        messageShieldProvider = { null },
        sendHandleProvider = { FakeSendHandle() },
        forwarder = null,
        forwarderProfile = null,
    )

    private fun aDaySeparator(date: String) = TimelineItem.Virtual(
        id = UniqueId("day_$date"),
        model = aTimelineItemDaySeparatorModel(date)
    )

    @Test
    fun `filterEmptyDaySeparators keeps day separator with events after it`() {
        val items = listOf(
            anEvent,
            aDaySeparator("Today"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(anEvent)
        assertThat(result[1]).isEqualTo(aDaySeparator("Today"))
    }

    @Test
    fun `filterEmptyDaySeparators removes day separator with no events after it`() {
        val items = listOf(
            aDaySeparator("Today"),
            aDaySeparator("Yesterday"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).isEmpty()
    }

    @Test
    fun `filterEmptyDaySeparators removes first day separator and keeps second when only second has events`() {
        val items = listOf(
            aDaySeparator("Today"),
            anEvent,
            aDaySeparator("Yesterday"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(anEvent)
        assertThat(result[1]).isEqualTo(aDaySeparator("Yesterday"))
    }

    @Test
    fun `filterEmptyDaySeparators handles multiple day separators in a row with no events`() {
        val items = listOf(
            aDaySeparator("Today"),
            aDaySeparator("Yesterday"),
            aDaySeparator("Last week"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).isEmpty()
    }

    @Test
    fun `filterEmptyDaySeparators keeps all items when no day separators`() {
        val items = listOf(
            anEvent,
            anEvent.copy(id = UniqueId("event2")),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
    }

    @Test
    fun `filterEmptyDaySeparators handles grouped events after day separator`() {
        val groupedEvents = TimelineItem.GroupedEvents(
            id = UniqueId("grouped"),
            events = listOf(anEvent).toImmutableList(),
            aggregatedReadReceipts = emptyList<ReadReceiptData>().toImmutableList(),
        )
        val items = listOf(
            groupedEvents,
            aDaySeparator("Today"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(2)
        assertThat(result[0]).isEqualTo(groupedEvents)
        assertThat(result[1]).isEqualTo(aDaySeparator("Today"))
    }

    @Test
    fun `filterEmptyDaySeparators removes day separator followed by non-event virtual item`() {
        val readMarker = TimelineItem.Virtual(
            id = UniqueId("readMarker"),
            model = TimelineItemReadMarkerModel
        )
        val items = listOf(
            aDaySeparator("Today"),
            readMarker,
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(readMarker)
    }

    @Test
    fun `filterEmptyDaySeparators keeps day separator when non-event virtual items are between separator and event`() {
        val readMarker = TimelineItem.Virtual(
            id = UniqueId("readMarker"),
            model = TimelineItemReadMarkerModel
        )
        val items = listOf(
            anEvent,
            readMarker,
            aDaySeparator("Today"),
        )
        val result = filterEmptyDaySeparators(items)
        assertThat(result).hasSize(3)
        assertThat(result[2]).isEqualTo(aDaySeparator("Today"))
    }
}
