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

interface TimelineEventFormatter {
    fun format(event: EventTimelineItem): CharSequence? {
        return format(
            content = event.content,
            isOutgoing = event.isOwn,
            sender = event.sender,
            senderDisambiguatedDisplayName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
        )
    }
    fun format(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): CharSequence?
}
