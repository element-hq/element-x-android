/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import io.element.android.features.messages.impl.attachments.video.VideoUploadEstimation
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.aTextEditorStateMarkdown
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.io.File

open class AttachmentsPreviewStateProvider : PreviewParameterProvider<AttachmentsPreviewState> {
    override val values: Sequence<AttachmentsPreviewState>
        get() = sequenceOf(
            anAttachmentsPreviewState(),
            anAttachmentsPreviewState(
                sendActionState = SendActionState.Sending.Processing(displayProgress = false),
                textEditorState = aTextEditorStateMarkdown(
                    initialText = "This is a caption!"
                )
            ),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Processing(displayProgress = true)),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.ReadyToUpload(aMediaUploadInfo())),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Uploading(aMediaUploadInfo())),
            anAttachmentsPreviewState(sendActionState = SendActionState.Failure(RuntimeException("error"), aMediaUploadInfo())),
            anAttachmentsPreviewState(displayFileTooLargeError = true),
            anAttachmentsPreviewState(
                mediaInfo = aVideoMediaInfo(),
                mediaOptimizationSelectorState = aMediaOptimisationSelectorState(
                    selectedVideoPreset = VideoCompressionPreset.STANDARD,
                    videoSizeEstimations = aVideoSizeEstimationList(),
                )
            ),
            anAttachmentsPreviewState(
                mediaInfo = aVideoMediaInfo(),
                mediaOptimizationSelectorState = aMediaOptimisationSelectorState(
                    videoSizeEstimations = aVideoSizeEstimationList(),
                    displayVideoPresetSelectorDialog = true,
                )
            ),
        )
}

fun anAttachmentsPreviewState(
    mediaInfo: MediaInfo = anImageMediaInfo(),
    textEditorState: TextEditorState = aTextEditorStateMarkdown(),
    sendActionState: SendActionState = SendActionState.Idle,
    mediaOptimizationSelectorState: MediaOptimizationSelectorState = aMediaOptimisationSelectorState(),
    displayFileTooLargeError: Boolean = false,
) = AttachmentsPreviewState(
    attachment = Attachment.Media(
        localMedia = LocalMedia("file://path".toUri(), mediaInfo),
    ),
    sendActionState = sendActionState,
    textEditorState = textEditorState,
    mediaOptimizationSelectorState = mediaOptimizationSelectorState,
    displayFileTooLargeError = displayFileTooLargeError,
    eventSink = {}
)

fun aMediaUploadInfo(
    filePath: String = "file://path",
    thumbnailFilePath: String? = null,
) = MediaUploadInfo.Image(
    file = File(filePath),
    imageInfo = ImageInfo(
        height = 100,
        width = 100,
        mimetype = MimeTypes.Jpeg,
        size = 1000,
        thumbnailInfo = null,
        thumbnailSource = null,
        blurhash = null,
    ),
    thumbnailFile = thumbnailFilePath?.let { File(it) },
)

fun aMediaOptimisationSelectorState(
    maxUploadSize: Long = 100,
    videoSizeEstimations: AsyncData<ImmutableList<VideoUploadEstimation>> = AsyncData.Success(persistentListOf()),
    isImageOptimizationEnabled: Boolean = true,
    selectedVideoPreset: VideoCompressionPreset = VideoCompressionPreset.STANDARD,
    displayMediaSelectorViews: Boolean = true,
    displayVideoPresetSelectorDialog: Boolean = false,
) = MediaOptimizationSelectorState(
    maxUploadSize = AsyncData.Success(maxUploadSize),
    videoSizeEstimations = videoSizeEstimations,
    isImageOptimizationEnabled = isImageOptimizationEnabled,
    selectedVideoPreset = selectedVideoPreset,
    displayMediaSelectorViews = displayMediaSelectorViews,
    displayVideoPresetSelectorDialog = displayVideoPresetSelectorDialog,
    eventSink = {},
)

internal fun aVideoSizeEstimationList(): AsyncData<ImmutableList<VideoUploadEstimation>> = AsyncData.Success(
    persistentListOf(
        VideoUploadEstimation(
            preset = VideoCompressionPreset.HIGH,
            sizeInBytes = 8_200_000L,
            canUpload = false,
        ),
        VideoUploadEstimation(
            preset = VideoCompressionPreset.STANDARD,
            sizeInBytes = 4_200_000L,
            canUpload = true,
        ),
    )
)
