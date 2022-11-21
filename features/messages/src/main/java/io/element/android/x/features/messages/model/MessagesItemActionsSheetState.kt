package io.element.android.x.features.messages.model

import androidx.compose.runtime.Stable

@Stable
data class MessagesItemActionsSheetState(
    val targetItem: MessagesTimelineItemState.MessageEvent,
    val actions: List<MessagesItemAction>
)
