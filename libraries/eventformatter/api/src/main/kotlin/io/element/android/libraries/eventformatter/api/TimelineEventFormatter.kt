/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.api

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName

/**
 * Turns a state or membership event into the human readable sentence shown in the timeline, such as "Alice joined the room".
 */
interface TimelineEventFormatter {
    /**
     * Convenience overload that reads the sender and ownership from the timeline item itself.
     *
     * @param event the timeline item to describe.
     * @return the sentence to display, or `null` when this kind of event is not rendered as text.
     */
    fun format(event: EventTimelineItem): CharSequence? {
        return format(
            content = event.content,
            isOutgoing = event.isOwn,
            sender = event.sender,
            senderDisambiguatedDisplayName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
        )
    }

    /**
     * @param content the event content to describe.
     * @param isOutgoing whether the event was sent by the current user, which selects the first-person wording.
     * @param sender the user who sent the event.
     * @param senderDisambiguatedDisplayName the sender's display name, already suffixed with their id when it is ambiguous in the room.
     * @return the sentence to display, or `null` when this kind of event is not rendered as text.
     */
    fun format(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): CharSequence?
}
