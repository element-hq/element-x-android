package io.element.android.x.features.messages.model.content

import org.matrix.rustcomponents.sdk.FormattedBody

sealed interface MessagesTimelineItemTextBasedContent : MessagesTimelineItemContent {
    val body: String
    val formattedBody: FormattedBody?
}