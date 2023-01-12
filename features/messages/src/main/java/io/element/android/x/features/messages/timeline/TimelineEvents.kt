package io.element.android.x.features.messages.timeline

import io.element.android.x.matrix.core.EventId

sealed interface TimelineEvents {
    object LoadMore : TimelineEvents
    data class SetHighlightedEvent(val eventId: EventId?): TimelineEvents
}
