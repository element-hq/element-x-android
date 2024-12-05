/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.actionlist.model

class TimelineItemActionComparator : Comparator<TimelineItemAction> {
    // See order in https://www.figma.com/design/ux3tYoZV9WghC7hHT9Fhk0/Compound-iOS-Components?node-id=2946-2392
    private val orderedList = listOf(
        TimelineItemAction.EndPoll,
        TimelineItemAction.ViewInTimeline,
        TimelineItemAction.Reply,
        TimelineItemAction.ReplyInThread,
        TimelineItemAction.Forward,
        TimelineItemAction.Pin,
        TimelineItemAction.Unpin,
        TimelineItemAction.CopyLink,
        TimelineItemAction.Edit,
        TimelineItemAction.CopyText,
        TimelineItemAction.AddCaption,
        TimelineItemAction.EditCaption,
        TimelineItemAction.CopyCaption,
        TimelineItemAction.RemoveCaption,
        TimelineItemAction.ViewSource,
        TimelineItemAction.ReportContent,
        TimelineItemAction.Redact,
    )

    override fun compare(o1: TimelineItemAction, o2: TimelineItemAction): Int {
        val index1 = orderedList.indexOf(o1)
        val index2 = orderedList.indexOf(o2)
        return index1.compareTo(index2)
    }
}
