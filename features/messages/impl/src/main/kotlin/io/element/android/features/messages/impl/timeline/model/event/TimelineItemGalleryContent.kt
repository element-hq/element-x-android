/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import kotlinx.collections.immutable.ImmutableList
import kotlin.time.Duration

data class TimelineItemGalleryContent(
    val body: String,
    val caption: String?,
    val formattedCaption: CharSequence?,
    override val isEdited: Boolean,
    val items: ImmutableList<GalleryItem>,
) : TimelineItemEventContent, TimelineItemEventMutableContent {
    override val type: String = "TimelineItemGalleryContent"

    val showCaption = caption != null
}

data class GalleryItem(
    val filename: String,
    val mimeType: String,
    val mediaSource: MediaSource,
    val type: Type,
    val thumbnailSource: MediaSource?,
    val width: Int?,
    val height: Int?,
    val thumbnailWidth: Int?,
    val thumbnailHeight: Int?,
    val blurhash: String?,
    val duration: Duration = Duration.ZERO,
) {
    enum class Type {
        Image,
        Video,
        Audio,
        File;

        fun isVisualMedia() = this in setOf(Image, Video)
    }

    val thumbnailMediaRequestData: MediaRequestData by lazy {
        MediaRequestData(
            source = thumbnailSource ?: mediaSource,
            kind = MediaRequestData.Kind.Thumbnail(
                width = thumbnailWidth?.toLong() ?: io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_WIDTH,
                height = thumbnailHeight?.toLong() ?: io.element.android.libraries.matrix.ui.media.MAX_THUMBNAIL_HEIGHT,
            ),
        )
    }

    val aspectRatio: Float? by lazy {
        if (width != null && height != null && height > 0) {
            width.toFloat() / height.toFloat()
        } else {
            null
        }
    }
}
