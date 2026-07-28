/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.runtime.saveable.SaverScope
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Test

class TimelineSelectionStateTest {
    companion object {
        val CanSaveScope = SaverScope { true }
    }

    @Test
    fun `selection is active as soon as something is selected`() {
        assertThat(TimelineSelectionState.Empty.isActive).isFalse()
        assertThat(aTimelineSelectionState(count = 1).isActive).isTrue()
    }

    @Test
    fun `isAtCap is true when the maximum number of messages is selected`() {
        assertThat(aTimelineSelectionState(count = TimelineSelectionState.MAX_SELECTION - 1).isAtCap).isFalse()
        assertThat(aTimelineSelectionState(count = TimelineSelectionState.MAX_SELECTION).isAtCap).isTrue()
    }

    @Test
    fun `save and restore preserves the selected ids`() {
        val selectedIds: ImmutableSet<EventId> = persistentSetOf(EventId("\$event1"), EventId("\$event2"))

        val saved = with(CanSaveScope) {
            with(TimelineSelectionSaver) {
                save(selectedIds)
            }
        }
        val restored = saved?.let { TimelineSelectionSaver.restore(it) }

        assertThat(restored).isEqualTo(selectedIds)
    }

    @Test
    fun `restoring an empty payload yields an empty selection`() {
        val restored = TimelineSelectionSaver.restore(emptyList<String>())

        assertThat(restored).isEmpty()
    }
}
