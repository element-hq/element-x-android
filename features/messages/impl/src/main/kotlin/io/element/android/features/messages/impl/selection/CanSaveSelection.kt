/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId

/**
 * The files to save for the current selection, oldest first, or an empty list if any selected event
 * carries none. Selected events which are no longer loaded make the selection unsavable, since their
 * media cannot be resolved.
 */
fun savableSelection(
    timelineItems: List<TimelineItem>,
    selectedIds: Set<EventId>,
): List<SavableMedia> {
    if (selectedIds.isEmpty()) return emptyList()
    val selectedEvents = timelineItems
        .filterIsInstance<TimelineItem.Event>()
        .filter { it.eventId in selectedIds }
        .sortedBy { it.sentTimeMillis }
    if (selectedEvents.mapNotNull { it.eventId }.toSet() != selectedIds) return emptyList()
    val media = selectedEvents.map { it.content.savableMedia() }
    return if (media.any { it.isEmpty() }) emptyList() else media.flatten()
}

/** Whether the bulk Save action should be offered for the current selection. */
fun canSaveSelection(
    timelineItems: List<TimelineItem>,
    selectedIds: Set<EventId>,
): Boolean = savableSelection(timelineItems, selectedIds).isNotEmpty()
