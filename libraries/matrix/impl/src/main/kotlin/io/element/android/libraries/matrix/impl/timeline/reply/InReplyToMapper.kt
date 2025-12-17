/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.reply

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.impl.timeline.item.event.TimelineEventContentMapper
import io.element.android.libraries.matrix.impl.timeline.item.event.map
import org.matrix.rustcomponents.sdk.EmbeddedEventDetails
import org.matrix.rustcomponents.sdk.InReplyToDetails

class InReplyToMapper(
    private val timelineEventContentMapper: TimelineEventContentMapper,
) {
    fun map(inReplyToDetails: InReplyToDetails): InReplyTo {
        val inReplyToId = EventId(inReplyToDetails.eventId())
        return when (val event = inReplyToDetails.event()) {
            is EmbeddedEventDetails.Ready -> {
                InReplyTo.Ready(
                    eventId = inReplyToId,
                    content = timelineEventContentMapper.map(event.content),
                    senderId = UserId(event.sender),
                    senderProfile = event.senderProfile.map(),
                )
            }
            is EmbeddedEventDetails.Error -> InReplyTo.Error(
                eventId = inReplyToId,
                message = event.message,
            )
            EmbeddedEventDetails.Pending -> InReplyTo.Pending(
                eventId = inReplyToId,
            )
            is EmbeddedEventDetails.Unavailable -> InReplyTo.NotLoaded(
                eventId = inReplyToId
            )
        }
    }
}
