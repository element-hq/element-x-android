package io.element.android.x.features.messages

import io.element.android.x.features.messages.actionlist.model.TimelineItemAction
import io.element.android.x.features.messages.timeline.model.TimelineItem

sealed interface MessagesEvents {
    data class HandleAction(val action: TimelineItemAction, val messageEvent: TimelineItem.MessageEvent) : MessagesEvents
}
