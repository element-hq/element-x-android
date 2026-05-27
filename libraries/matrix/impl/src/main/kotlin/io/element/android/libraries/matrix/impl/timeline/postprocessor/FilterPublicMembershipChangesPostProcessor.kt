/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.MembershipChange
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent

/**
 * Post-processor to filter out public membership changes for non-encrypted, publicly joinable rooms.
 */
class FilterPublicMembershipChangesPostProcessor {
    /**
     * Filters out public membership changes from [items] if the room is publicly joinable and not encrypted.
     */
    fun process(
        items: List<MatrixTimelineItem>,
        joinRule: JoinRule?,
        isEncrypted: Boolean?,
    ): List<MatrixTimelineItem> {
        return if (joinRule !is JoinRule.Invite && isEncrypted == false) {
            filterMembershipEvents(items)
        } else {
            items
        }
    }

    private fun filterMembershipEvents(items: List<MatrixTimelineItem>): List<MatrixTimelineItem> = items.filter { item ->
        val eventContent = (item as? MatrixTimelineItem.Event)?.event?.content ?: return@filter true
        when (eventContent) {
            is RoomMembershipContent -> eventContent.change != null && eventContent.change !in listOf(MembershipChange.JOINED, MembershipChange.LEFT)
            is ProfileChangeContent -> false
            else -> true
        }
    }
}
