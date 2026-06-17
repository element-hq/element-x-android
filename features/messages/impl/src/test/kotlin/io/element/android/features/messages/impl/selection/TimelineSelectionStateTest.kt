/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import androidx.compose.runtime.saveable.SaverScope
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Test

class TimelineSelectionStateTest {
    companion object {
        val CanSaveScope = SaverScope { true }
    }

    @Test
    fun `save and restore preserves the active selection`() {
        val state = TimelineSelectionState(
            isActive = true,
            selectedIds = persistentSetOf(EventId("\$event1"), EventId("\$event2")),
            maxSelection = TimelineSelectionState.MAX_SELECTION,
        )

        val saved = with(CanSaveScope) {
            with(TimelineSelectionState.Saver) {
                save(state)
            }
        }
        val restored = saved?.let { TimelineSelectionState.Saver.restore(it) }

        assertThat(restored).isEqualTo(state)
    }

    @Test
    fun `restoring a truncated payload yields an empty selection instead of crashing`() {
        val restored = TimelineSelectionState.Saver.restore(listOf("true"))

        assertThat(restored).isEqualTo(TimelineSelectionState.Empty)
    }
}
