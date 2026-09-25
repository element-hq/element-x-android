/*
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemRedactedContent
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.AN_EVENT_ID_3
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Test

class TimelineStateTest {
    @Test
    fun `selectedEventIdsInTimelineOrder - when selection is disabled, the list is empty`() {
        val state = aTimelineState(
            timelineItems = persistentListOf(aTimelineItemEvent(eventId = AN_EVENT_ID)),
            selectionState = SelectionState.Disabled,
        )
        assertThat(state.selectedEventIdsInTimelineOrder()).isEmpty()
    }

    @Test
    fun `selectedEventIdsInTimelineOrder - the events are sorted from the oldest to the most recent one`() {
        // The timeline items are rendered from the most recent to the oldest event.
        val state = aTimelineState(
            timelineItems = persistentListOf(
                aTimelineItemEvent(eventId = AN_EVENT_ID_3),
                aTimelineItemEvent(eventId = AN_EVENT_ID_2),
                aTimelineItemEvent(eventId = AN_EVENT_ID),
            ),
            // The events have been tapped in a random order.
            selectionState = SelectionState.Active(SelectionAction.Forward, persistentSetOf(AN_EVENT_ID_2, AN_EVENT_ID, AN_EVENT_ID_3)),
        )
        assertThat(state.selectedEventIdsInTimelineOrder()).containsExactly(AN_EVENT_ID, AN_EVENT_ID_2, AN_EVENT_ID_3).inOrder()
    }

    @Test
    fun `selectedEventIdsInTimelineOrder - the events which are not in the timeline are ignored`() {
        val state = aTimelineState(
            timelineItems = persistentListOf(
                aTimelineItemEvent(eventId = AN_EVENT_ID_2),
                aTimelineItemEvent(eventId = AN_EVENT_ID),
            ),
            selectionState = SelectionState.Active(SelectionAction.Forward, persistentSetOf(AN_EVENT_ID_2, AN_EVENT_ID_3)),
        )
        assertThat(state.selectedEventIdsInTimelineOrder()).containsExactly(AN_EVENT_ID_2)
    }

    @Test
    fun `canSelect - returns false when selection mode is disabled`() {
        val event = aTimelineItemEvent(eventId = AN_EVENT_ID)
        val state = aTimelineState(
            timelineItems = persistentListOf(event),
            selectionState = SelectionState.Disabled,
        )
        assertThat(state.canSelect(event)).isFalse()
    }

    @Test
    fun `canSelect - follows the action predicate when selection mode is active`() {
        val forwardable = aTimelineItemEvent(eventId = AN_EVENT_ID)
        val notForwardable = aTimelineItemEvent(eventId = AN_EVENT_ID_2, content = aTimelineItemRedactedContent())
        val state = aTimelineState(
            timelineItems = persistentListOf(forwardable, notForwardable),
            selectionState = SelectionState.Active(SelectionAction.Forward, persistentSetOf(AN_EVENT_ID)),
        )
        assertThat(state.canSelect(forwardable)).isTrue()
        assertThat(state.canSelect(notForwardable)).isFalse()
    }
}
