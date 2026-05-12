/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.TextEditorState

class MediaPreviewStateProvider : PreviewParameterProvider<MediaPreviewState> {
    override val values: Sequence<MediaPreviewState>
        get() = sequenceOf(
            aMediaPreviewState(),
        )
}

fun aMediaPreviewState(
    localMedia: LocalMedia = LocalMedia(
        uri = android.net.Uri.EMPTY,
        info = anImageMediaInfo(),
    ),
    sendActionState: SendActionState = SendActionState.Idle,
    showOptimizationOptions: Boolean = false,
    isImageOptimizationEnabled: Boolean = true,
    selectedVideoPreset: VideoCompressionPreset? = VideoCompressionPreset.STANDARD,
    showVideoQualityDialog: Boolean = false,
    displayFileTooLargeError: Boolean = false,
    videoSizeEstimations: List<VideoUploadEstimation> = emptyList(),
    maxUploadSize: Long = 100L * 1024 * 1024,
) = MediaPreviewState(
    localMedia = localMedia,
    textEditorState = TextEditorState.Markdown(
        MarkdownTextEditorState("", initialFocus = false),
        isRoomEncrypted = null,
    ),
    sendActionState = sendActionState,
    showOptimizationOptions = showOptimizationOptions,
    isImageOptimizationEnabled = isImageOptimizationEnabled,
    selectedVideoPreset = selectedVideoPreset,
    showVideoQualityDialog = showVideoQualityDialog,
    displayFileTooLargeError = displayFileTooLargeError,
    videoSizeEstimations = videoSizeEstimations,
    maxUploadSize = maxUploadSize,
    eventSink = {},
)
