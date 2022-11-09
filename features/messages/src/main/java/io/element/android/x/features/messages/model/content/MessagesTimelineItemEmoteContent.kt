package io.element.android.x.features.messages.model.content

import org.matrix.rustcomponents.sdk.FormattedBody

data class MessagesTimelineItemEmoteContent(
    override val body: String,
    override val formattedBody: FormattedBody?,
) : MessagesTimelineItemTextBasedContent