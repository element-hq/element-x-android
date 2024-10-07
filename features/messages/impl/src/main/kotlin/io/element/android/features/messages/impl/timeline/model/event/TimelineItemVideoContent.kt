/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import kotlin.time.Duration

data class TimelineItemVideoContent(
    val body: String,
    val formatted: FormattedBody?,
    val filename: String?,
    val duration: Duration,
    val videoSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val aspectRatio: Float?,
    val blurHash: String?,
    val height: Int?,
    val width: Int?,
    val mimeType: String,
    val formattedFileSize: String,
    val fileExtension: String,
) : TimelineItemEventContent {
    override val type: String = "TimelineItemImageContent"

    val showCaption = filename != null && filename != body
    val caption = if (showCaption) body else ""
}
