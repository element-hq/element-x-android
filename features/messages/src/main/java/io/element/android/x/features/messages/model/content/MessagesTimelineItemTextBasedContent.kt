package io.element.android.x.features.messages.model.content

import org.jsoup.nodes.Document

sealed interface MessagesTimelineItemTextBasedContent : MessagesTimelineItemContent {
    val body: String
    val htmlDocument: Document?
}
