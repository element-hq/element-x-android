package io.element.android.x.features.messages.actionlist

import io.element.android.x.features.messages.model.MessagesTimelineItemState

sealed interface ActionListEvents {
    object Clear : ActionListEvents
    data class ComputeForMessage(val messageEvent: MessagesTimelineItemState.MessageEvent) : ActionListEvents
}
