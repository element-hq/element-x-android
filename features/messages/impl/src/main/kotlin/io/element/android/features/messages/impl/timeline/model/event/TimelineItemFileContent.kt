/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.helper.formatFileExtensionAndSize

data class TimelineItemFileContent(
    override val filename: String,
    override val fileSize: Long?,
    override val caption: String?,
    override val formattedCaption: CharSequence?,
    override val isEdited: Boolean,
    override val mediaSource: MediaSource,
    val thumbnailSource: MediaSource?,
    override val formattedFileSize: String,
    override val fileExtension: String,
    override val mimeType: String,
) : TimelineItemEventContentWithAttachment {
    override val type: String = "TimelineItemFileContent"

    val fileExtensionAndSize = formatFileExtensionAndSize(fileExtension, formattedFileSize)
}
