/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource

data class TimelineItemStickerContent(
    val body: String,
    val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val formattedFileSize: String,
    val fileExtension: String,
    val mimeType: String,
    val blurhash: String?,
    val width: Int?,
    val height: Int?,
    val aspectRatio: Float?
) : TimelineItemEventContent {
    override val type: String = "TimelineItemStickerContent"

    /* Stickers are supposed to be small images so
       we allow using the mediaSource (unless the url is empty) */
    val preferredMediaSource = if (mediaSource.url.isEmpty()) thumbnailSource else mediaSource
}
