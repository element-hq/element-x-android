/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

/**
 * This post processor is responsible for adding virtual items to indicate all the previous last forward item.
 */
class LastForwardIndicatorsPostProcessor(
    private val mode: Timeline.Mode,
) {
    private val lastForwardIdentifiers = LinkedHashSet<UniqueId>()

    fun process(
        items: List<MatrixTimelineItem>,
    ): List<MatrixTimelineItem> {
        // We don't need to add the last forward indicator if we are not in the FOCUSED_ON_EVENT mode
        if (mode !is Timeline.Mode.FocusedOnEvent) {
            return items
        } else {
            return buildList {
                val latestEventIdentifier = items.latestEventIdentifier()
                // Remove if it always exists (this should happen only when no new events are added)
                lastForwardIdentifiers.remove(latestEventIdentifier)

                items.forEach { item ->
                    add(item)

                    if (item is MatrixTimelineItem.Event) {
                        if (lastForwardIdentifiers.contains(item.uniqueId)) {
                            add(createLastForwardIndicator(item.uniqueId))
                        }
                    }
                }
                // This is important to always add this one at the end of the list so it's used to keep the scroll position.
                add(createLastForwardIndicator(latestEventIdentifier))
                lastForwardIdentifiers.add(latestEventIdentifier)
            }
        }
    }
}

private fun createLastForwardIndicator(identifier: UniqueId): MatrixTimelineItem {
    return MatrixTimelineItem.Virtual(
        uniqueId = UniqueId("last_forward_indicator_$identifier"),
        virtual = VirtualTimelineItem.LastForwardIndicator
    )
}

private fun List<MatrixTimelineItem>.latestEventIdentifier(): UniqueId {
    return findLast {
        it is MatrixTimelineItem.Event
    }?.let {
        (it as MatrixTimelineItem.Event).uniqueId
    } ?: UniqueId("fake_id")
}
