package io.element.android.x.features.messages

import io.element.android.x.features.messages.actionlist.TimelineItemAction
import io.element.android.x.features.messages.model.MessagesTimelineItemState

sealed interface MessagesEvents {
    data class HandleAction(val action: TimelineItemAction, val messageEvent: MessagesTimelineItemState.MessageEvent) : MessagesEvents
}
