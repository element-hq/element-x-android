/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned.list

import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.actionlist.model.TimelineItemActionPostProcessor

class PinnedMessagesListTimelineActionPostProcessor : TimelineItemActionPostProcessor {
    override fun process(actions: List<TimelineItemAction>): List<TimelineItemAction> {
        return buildList {
            add(TimelineItemAction.ViewInTimeline)
            actions.firstOrNull { it is TimelineItemAction.Unpin }?.let(::add)
            actions.firstOrNull { it is TimelineItemAction.Forward }?.let(::add)
            actions.firstOrNull { it is TimelineItemAction.ViewSource }?.let(::add)
            actions.firstOrNull { it is TimelineItemAction.Redact }?.let(::add)
        }
    }
}
