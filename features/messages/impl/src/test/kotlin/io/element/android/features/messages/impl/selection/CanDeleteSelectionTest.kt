/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.libraries.matrix.api.core.EventId
import org.junit.Test

class CanDeleteSelectionTest {
    private val redactOwnOnly = UserEventPermissions.DEFAULT // canRedactOwn = true, canRedactOther = false

    @Test
    fun `empty selection is not deletable`() {
        val items = listOf(aTimelineItemEvent(eventId = EventId("\$1"), isMine = true))
        assertThat(canDeleteSelection(items, emptySet(), redactOwnOnly)).isFalse()
    }

    @Test
    fun `selection of own redactable messages is deletable`() {
        val items = listOf(
            aTimelineItemEvent(eventId = EventId("\$1"), isMine = true),
            aTimelineItemEvent(eventId = EventId("\$2"), isMine = true),
        )
        assertThat(canDeleteSelection(items, setOf(EventId("\$1"), EventId("\$2")), redactOwnOnly)).isTrue()
    }

    @Test
    fun `selection containing a non-redactable message is not deletable`() {
        val items = listOf(
            aTimelineItemEvent(eventId = EventId("\$1"), isMine = true),
            // Someone else's message, user lacks canRedactOther.
            aTimelineItemEvent(eventId = EventId("\$2"), isMine = false),
        )
        assertThat(canDeleteSelection(items, setOf(EventId("\$1"), EventId("\$2")), redactOwnOnly)).isFalse()
    }

    @Test
    fun `no redact rights at all means not deletable`() {
        val noRights = redactOwnOnly.copy(canRedactOwn = false, canRedactOther = false)
        val items = listOf(aTimelineItemEvent(eventId = EventId("\$1"), isMine = true))
        assertThat(canDeleteSelection(items, setOf(EventId("\$1")), noRights)).isFalse()
    }

    @Test
    fun `window-evicted selected id does not force-disable`() {
        // \$2 is selected but not present in the loaded timeline items - it must not disable Delete.
        val items = listOf(aTimelineItemEvent(eventId = EventId("\$1"), isMine = true))
        assertThat(canDeleteSelection(items, setOf(EventId("\$1"), EventId("\$2")), redactOwnOnly)).isTrue()
    }
}
