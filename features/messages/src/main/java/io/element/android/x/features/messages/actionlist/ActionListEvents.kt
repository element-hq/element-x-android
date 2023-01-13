package io.element.android.x.features.messages.actionlist

import io.element.android.x.features.messages.timeline.model.TimelineItem

sealed interface ActionListEvents {
    object Clear : ActionListEvents
    data class ComputeForMessage(val messageEvent: TimelineItem.MessageEvent) : ActionListEvents
}
