package io.element.android.x.features.messages.model

import androidx.compose.runtime.Stable

@Stable
data class MessagesItemReactionState(
    val reactions: List<AggregatedReaction>
)

@Stable
data class AggregatedReaction(
    val key: String,
    val count: String,
    val isHighlighted: Boolean = false
)