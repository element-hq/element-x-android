/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.collections.immutable.ImmutableList

data class TimelineItemAttachmentsContent(
    val body: String,
    val caption: String?,
    val formattedCaption: CharSequence?,
    override val isEdited: Boolean,
    val attachments: ImmutableList<AttachmentItem>,
) : TimelineItemEventContent, TimelineItemEventMutableContent {
    override val type: String = "TimelineItemAttachmentsContent"

    val showCaption = caption != null

    val hasPreviews = attachments.any { it.thumbnailSource != null }
}

data class AttachmentItem(
    val filename: String,
    val mimeType: String,
    val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val fileSize: Long?,
    val formattedFileSize: String,
    val fileExtension: String,
)
