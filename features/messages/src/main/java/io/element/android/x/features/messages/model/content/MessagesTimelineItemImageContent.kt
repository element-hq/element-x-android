package io.element.android.x.features.messages.model.content

import io.element.android.x.matrix.media.MediaResolver

data class MessagesTimelineItemImageContent(
    val body: String,
    val imageMeta: MediaResolver.Meta,
    val blurhash: String?,
    val aspectRatio: Float
) : MessagesTimelineItemContent