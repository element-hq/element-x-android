/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemProfileChangeContent
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import javax.inject.Inject

class TimelineItemContentProfileChangeFactory @Inject constructor(
    private val timelineEventFormatter: TimelineEventFormatter,
) {
    fun create(eventTimelineItem: EventTimelineItem): TimelineItemEventContent {
        val text = timelineEventFormatter.format(eventTimelineItem)
        return TimelineItemProfileChangeContent(text.orEmpty().toString())
    }
}
