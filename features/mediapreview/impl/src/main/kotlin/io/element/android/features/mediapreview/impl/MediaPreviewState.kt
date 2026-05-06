/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import androidx.compose.runtime.Immutable
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.model.TextEditorState

data class MediaPreviewState(
    val localMedia: LocalMedia,
    val textEditorState: TextEditorState,
    val sendActionState: SendActionState,
    val showOptimizationOptions: Boolean,
    val isImageOptimizationEnabled: Boolean,
    val selectedVideoPreset: VideoCompressionPreset?,
    val showVideoQualityDialog: Boolean,
    val displayFileTooLargeError: Boolean,
    val videoSizeEstimations: List<VideoUploadEstimation>,
    val maxUploadSize: Long,
    val eventSink: (MediaPreviewEvents) -> Unit,
)

@Immutable
sealed interface SendActionState {
    data object Idle : SendActionState

    @Immutable
    sealed interface Sending : SendActionState {
        data class Processing(val displayProgress: Boolean) : Sending
        data class ReadyToUpload(val mediaInfo: MediaUploadInfo) : Sending
        data class Uploading(val mediaInfo: MediaUploadInfo) : Sending
    }

    data class Failure(val error: Throwable, val mediaInfo: MediaUploadInfo?) : SendActionState
    data object Done : SendActionState

    fun mediaInfo(): MediaUploadInfo? = when (this) {
        is Sending.ReadyToUpload -> mediaInfo
        is Sending.Uploading -> mediaInfo
        is Failure -> mediaInfo
        else -> null
    }
}

data class VideoUploadEstimation(
    val preset: VideoCompressionPreset,
    val sizeInBytes: Long,
    val canUpload: Boolean,
)
