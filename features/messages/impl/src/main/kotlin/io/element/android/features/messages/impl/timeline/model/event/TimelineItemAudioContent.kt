/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.helper.formatFileExtensionAndSize
import kotlin.time.Duration

data class TimelineItemAudioContent(
    val body: String,
    val duration: Duration,
    val mediaSource: MediaSource,
    val mimeType: String,
    val formattedFileSize: String,
    val fileExtension: String,
) : TimelineItemEventContent {
    val fileExtensionAndSize =
        formatFileExtensionAndSize(
            fileExtension,
            formattedFileSize
        )
    override val type: String = "TimelineItemAudioContent"
}
