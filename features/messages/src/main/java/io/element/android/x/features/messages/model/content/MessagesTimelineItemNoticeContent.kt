package io.element.android.x.features.messages.model.content

import org.jsoup.nodes.Document

data class MessagesTimelineItemNoticeContent(
    override val body: String,
    override val htmlDocument: Document?
) : MessagesTimelineItemTextBasedContent
