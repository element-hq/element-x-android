package io.element.android.x.features.messages.model.content

import org.matrix.rustcomponents.sdk.FormattedBody

data class MessagesTimelineItemNoticeContent(
    override val body: String,
    override val formattedBody: FormattedBody?,
) : MessagesTimelineItemTextBasedContent