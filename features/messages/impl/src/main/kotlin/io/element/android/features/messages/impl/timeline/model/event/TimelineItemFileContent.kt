/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.mediaviewer.api.helper.formatFileExtensionAndSize

data class TimelineItemFileContent(
    override val filename: String,
    override val caption: String?,
    override val formattedCaption: FormattedBody?,
    val fileSource: MediaSource,
    val thumbnailSource: MediaSource?,
    val formattedFileSize: String,
    val fileExtension: String,
    val mimeType: String,
) : TimelineItemEventContentWithAttachment {
    override val type: String = "TimelineItemFileContent"

    val fileExtensionAndSize = formatFileExtensionAndSize(fileExtension, formattedFileSize)
}
