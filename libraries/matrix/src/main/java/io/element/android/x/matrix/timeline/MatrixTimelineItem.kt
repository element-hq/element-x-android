package io.element.android.x.matrix.timeline

import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.TimelineItem

sealed interface MatrixTimelineItem {
    data class Event(val event: EventTimelineItem) : MatrixTimelineItem
    object Virtual : MatrixTimelineItem
    object Other : MatrixTimelineItem
}

fun TimelineItem.asMatrixTimelineItem(): MatrixTimelineItem {
    val asEvent = asEvent()
    if (asEvent != null) {
        return MatrixTimelineItem.Event(asEvent)
    }
    val asVirtual = asVirtual()
    if (asVirtual != null) {
        return MatrixTimelineItem.Virtual
    }
    return MatrixTimelineItem.Other
}
