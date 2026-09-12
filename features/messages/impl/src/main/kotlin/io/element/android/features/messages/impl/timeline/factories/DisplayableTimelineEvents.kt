/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories

import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem

internal fun List<MatrixTimelineItem>.keepDisplayableTimelineEvents(): List<MatrixTimelineItem> {
    val items = filter { it.isDisplayableInTimeline() }
    return items.filterIndexed { index, item ->
        !item.isDayDivider() || items.hasEventBeforeNextDayDivider(index)
    }
}

private fun MatrixTimelineItem.isDisplayableInTimeline(): Boolean {
    return this !is MatrixTimelineItem.Event || event.content.isDisplayableInTimeline()
}

private fun EventContent.isDisplayableInTimeline(): Boolean {
    return this !is StateContent || content !is OtherState.Custom
}

private fun MatrixTimelineItem.isDayDivider(): Boolean {
    return this is MatrixTimelineItem.Virtual && virtual is VirtualTimelineItem.DayDivider
}

private fun List<MatrixTimelineItem>.hasEventBeforeNextDayDivider(dayDividerIndex: Int): Boolean {
    for (index in dayDividerIndex + 1 until size) {
        val item = this[index]
        if (item.isDayDivider()) return false
        if (item is MatrixTimelineItem.Event) return true
    }
    return false
}
