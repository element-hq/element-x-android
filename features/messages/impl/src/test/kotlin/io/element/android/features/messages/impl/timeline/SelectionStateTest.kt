/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemRedactedContent
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.AN_EVENT_ID_3
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import org.junit.Test

class SelectionStateTest {
    @Test
    fun `eventIdsToDeselect - selection mode being disabled has nothing to drop`() {
        val items = listOf(aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemRedactedContent()))
        assertThat(SelectionState.Disabled.eventIdsToDeselect(items)).isEmpty()
    }

    @Test
    fun `eventIdsToDeselect - an empty selection has nothing to drop`() {
        val items = listOf(aTimelineItemEvent(eventId = AN_EVENT_ID, content = aTimelineItemRedactedContent()))
        assertThat(anActiveSelection().eventIdsToDeselect(items)).isEmpty()
    }

    @Test
    fun `eventIdsToDeselect - a selected event the action still applies to is kept`() {
        val items = listOf(aTimelineItemEvent(eventId = AN_EVENT_ID))
        assertThat(anActiveSelection(listOf(AN_EVENT_ID)).eventIdsToDeselect(items)).isEmpty()
    }

    @Test
    fun `eventIdsToDeselect - a selected event which has been deleted is dropped`() {
        val items = listOf(
            aTimelineItemEvent(eventId = AN_EVENT_ID_2, content = aTimelineItemRedactedContent()),
            aTimelineItemEvent(eventId = AN_EVENT_ID),
        )
        assertThat(anActiveSelection(listOf(AN_EVENT_ID, AN_EVENT_ID_2)).eventIdsToDeselect(items)).containsExactly(AN_EVENT_ID_2)
    }

    @Test
    fun `eventIdsToDeselect - a selected event which is not in the timeline is ignored`() {
        val items = listOf(aTimelineItemEvent(eventId = AN_EVENT_ID))
        // AN_EVENT_ID_3 may simply not be loaded anymore, so it is not dropped here.
        assertThat(anActiveSelection(listOf(AN_EVENT_ID, AN_EVENT_ID_3)).eventIdsToDeselect(items)).isEmpty()
    }

    @Test
    fun `eventIdsToDeselect - a deleted selected event folded into a group is dropped`() {
        val items = listOf(
            TimelineItem.GroupedEvents(
                id = UniqueId("group"),
                events = persistentListOf(
                    aTimelineItemEvent(eventId = AN_EVENT_ID_2, content = aTimelineItemRedactedContent()),
                    aTimelineItemEvent(eventId = AN_EVENT_ID_3, content = aTimelineItemRedactedContent()),
                ),
                aggregatedReadReceipts = persistentListOf(),
            ),
            aTimelineItemEvent(eventId = AN_EVENT_ID),
        )
        assertThat(anActiveSelection(listOf(AN_EVENT_ID, AN_EVENT_ID_2)).eventIdsToDeselect(items)).containsExactly(AN_EVENT_ID_2)
    }

    @Test
    fun `deselect - removes the given events and keeps selection mode active`() {
        val selection = anActiveSelection(listOf(AN_EVENT_ID, AN_EVENT_ID_2))
        assertThat(selection.deselect(setOf(AN_EVENT_ID))).isEqualTo(anActiveSelection(listOf(AN_EVENT_ID_2)))
        assertThat(selection.deselect(setOf(AN_EVENT_ID, AN_EVENT_ID_2))).isEqualTo(anActiveSelection())
    }

    @Test
    fun `deselect - does nothing when selection mode is disabled`() {
        assertThat(SelectionState.Disabled.deselect(setOf(AN_EVENT_ID))).isEqualTo(SelectionState.Disabled)
    }

    private fun anActiveSelection(eventIds: List<EventId> = emptyList()) = SelectionState.Active(
        action = SelectionAction.Forward,
        selectedEventIds = eventIds.toImmutableSet(),
    )
}
