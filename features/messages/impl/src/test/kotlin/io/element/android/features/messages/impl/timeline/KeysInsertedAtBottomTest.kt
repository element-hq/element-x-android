/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemDaySeparatorModel
import io.element.android.features.messages.impl.timeline.model.virtual.TimelineItemTypingNotificationModel
import io.element.android.libraries.matrix.api.core.UniqueId
import org.junit.Test

class KeysInsertedAtBottomTest {
    private val typing = TimelineItem.Virtual(UniqueId("typing"), TimelineItemTypingNotificationModel)
    private val older = aTimelineItemEvent().copy(id = UniqueId("older"))
    private val newest = aTimelineItemEvent().copy(id = UniqueId("newest"))
    private val new = aTimelineItemEvent().copy(id = UniqueId("new"))

    @Test
    fun `initial items are not inserted items`() {
        assertThat(keysInsertedAtBottom(previous = null, current = listOf(typing, newest, older))).isEmpty()
    }

    @Test
    fun `items added below the previously newest event are inserted items`() {
        val daySeparator = TimelineItem.Virtual(UniqueId("day"), TimelineItemDaySeparatorModel("Today"))
        val keys = keysInsertedAtBottom(
            previous = listOf(typing, newest, older),
            current = listOf(typing, new, daySeparator, newest, older),
        )
        assertThat(keys).containsExactly(new.identifier(), daySeparator.identifier())
    }

    @Test
    fun `paginated items are not inserted items`() {
        val keys = keysInsertedAtBottom(
            previous = listOf(typing, newest),
            current = listOf(typing, newest, older),
        )
        assertThat(keys).isEmpty()
    }

    @Test
    fun `items replacing the whole timeline are not inserted items`() {
        val keys = keysInsertedAtBottom(
            previous = listOf(typing, older),
            current = listOf(typing, new, newest),
        )
        assertThat(keys).isEmpty()
    }

    @Test
    fun `updated items are not inserted items`() {
        val keys = keysInsertedAtBottom(
            previous = listOf(typing, newest, older),
            current = listOf(typing, newest.copy(isMine = true), older),
        )
        assertThat(keys).isEmpty()
    }
}
