/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor

class PinnedMessagesListTimelineActionPostProcessor : TimelineItemActionPostProcessor {
    override fun process(actions: List<TimelineItemAction>): List<TimelineItemAction> {
        return buildList {
            add(TimelineItemAction.ViewInTimeline)
            actions.firstOrNull { it == TimelineItemAction.Unpin }?.let(::add)
            actions.firstOrNull { it == TimelineItemAction.Forward }?.let(::add)
            actions.firstOrNull { it == TimelineItemAction.ViewSource }?.let(::add)
        }
    }
}
