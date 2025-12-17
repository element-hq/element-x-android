/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import dev.zacsweers.metro.Inject
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRoomMembershipContent
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent

@Inject
class TimelineItemContentRoomMembershipFactory(
    private val timelineEventFormatter: TimelineEventFormatter,
) {
    fun create(eventContent: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): TimelineItemEventContent {
        val text = timelineEventFormatter.format(eventContent, isOutgoing, sender, senderDisambiguatedDisplayName)
        return TimelineItemRoomMembershipContent(text.orEmpty().toString())
    }
}
