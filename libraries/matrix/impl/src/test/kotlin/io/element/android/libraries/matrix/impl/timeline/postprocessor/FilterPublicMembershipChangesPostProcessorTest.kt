/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.join.JoinRule
import org.junit.Test

class FilterPublicMembershipChangesPostProcessorTest {
    @Test
    fun `processor removes join, leave, and profile events in unencrypted public rooms`() {
        val timelineItems = listOf(
            roomCreateEvent,
            roomCreatorJoinEvent,
            otherMemberJoinEvent,
            messageEvent,
            otherMemberLeaveEvent,
            profileChangeEvent,
        )
        val expected = listOf(
            roomCreateEvent,
            messageEvent,
        )
        val processor = FilterPublicMembershipChangesPostProcessor()
        val processedItems = processor.process(
            timelineItems,
            joinRule = JoinRule.Public,
            isEncrypted = false,
        )
        assertThat(processedItems).isEqualTo(expected)
    }

    @Test
    fun `processor keeps all events in encrypted public rooms`() {
        val timelineItems = listOf(
            roomCreateEvent,
            roomCreatorJoinEvent,
            otherMemberJoinEvent,
            messageEvent,
            otherMemberLeaveEvent,
            profileChangeEvent,
        )
        val processor = FilterPublicMembershipChangesPostProcessor()
        val processedItems = processor.process(
            timelineItems,
            joinRule = JoinRule.Public,
            isEncrypted = true,
        )
        assertThat(processedItems).isEqualTo(timelineItems)
    }

    @Test
    fun `processor keeps membership events in invite-only rooms`() {
        val timelineItems = listOf(
            roomCreateEvent,
            roomCreatorJoinEvent,
            otherMemberJoinEvent,
            messageEvent,
            otherMemberLeaveEvent,
            profileChangeEvent,
        )
        val processor = FilterPublicMembershipChangesPostProcessor()
        val processedItems = processor.process(
            timelineItems,
            joinRule = JoinRule.Invite,
            isEncrypted = null,
        )
        assertThat(processedItems).isEqualTo(timelineItems)
    }
}
