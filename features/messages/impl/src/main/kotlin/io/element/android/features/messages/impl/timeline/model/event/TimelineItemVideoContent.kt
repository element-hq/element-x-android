/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource
import kotlin.time.Duration

data class TimelineItemVideoContent(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: CharSequence?,
    override val isEdited: Boolean,
    val duration: Duration,
    override val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val aspectRatio: Float?,
    val blurHash: String?,
    val height: Int?,
    val width: Int?,
    val thumbnailWidth: Int?,
    val thumbnailHeight: Int?,
    override val mimeType: String,
    override val formattedFileSize: String,
    override val fileExtension: String,
) : TimelineItemEventContentWithAttachment {
    override val type: String = "TimelineItemImageContent"

    val showCaption = caption != null
}
