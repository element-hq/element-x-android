/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import io.element.android.features.messages.impl.UserEventPermissions
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.matrix.api.core.EventId

/**
 * Whether the bulk Delete action should be enabled for the current selection.
 * Selected events that are no longer in [timelineItems] are not taken into account, the per-event
 * filter in BulkRedactSelected handles those.
 */
fun canDeleteSelection(
    timelineItems: List<TimelineItem>,
    selectedIds: Set<EventId>,
    userEventPermissions: UserEventPermissions,
): Boolean {
    if (selectedIds.isEmpty()) return false
    return timelineItems
        .asSequence()
        .filterIsInstance<TimelineItem.Event>()
        .filter { it.eventId in selectedIds }
        .none { if (it.isMine) !userEventPermissions.canRedactOwn else !userEventPermissions.canRedactOther }
}
