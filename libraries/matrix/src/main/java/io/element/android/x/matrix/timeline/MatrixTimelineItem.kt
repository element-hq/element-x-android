package io.element.android.x.matrix.timeline

import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.TimelineItem
import org.matrix.rustcomponents.sdk.TimelineKey

sealed interface MatrixTimelineItem {
    data class Event(val event: EventTimelineItem) : MatrixTimelineItem {
        val uniqueId: String
            get() = when (val eventKey = event.key()) {
                is TimelineKey.TransactionId -> eventKey.txnId
                is TimelineKey.EventId -> eventKey.eventId
            }
    }

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
