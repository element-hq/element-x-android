/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.imageeditor.AttachmentImageEditorState
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.textcomposer.model.TextEditorState
import kotlinx.collections.immutable.ImmutableList

data class AttachmentsPreviewState(
    val attachments: ImmutableList<Attachment>,
    val imageEditorState: AttachmentImageEditorState?,
    val canEditImage: Boolean,
    val isApplyingImageEdits: Boolean,
    val displayImageEditError: Boolean,
    val sendActionState: SendActionState,
    val textEditorState: TextEditorState,
    val mediaOptimizationSelectorState: MediaOptimizationSelectorState,
    val displayFileTooLargeError: Boolean,
    val currentIndex: Int,
    val eventSink: (AttachmentsPreviewEvent) -> Unit,
) {
    val isGallery: Boolean get() = attachments.size > 1
    val totalCount: Int get() = attachments.size
}

@Immutable
sealed interface SendActionState {
    data object Idle : SendActionState

    @Immutable
    sealed interface Sending : SendActionState {
        data class Processing(val displayProgress: Boolean) : Sending
        data class ReadyToUpload(val mediaInfos: List<MediaUploadInfo>) : Sending
        data class Uploading(val mediaInfos: List<MediaUploadInfo>) : Sending
    }

    data class Failure(val error: Throwable, val mediaInfos: List<MediaUploadInfo>) : SendActionState
    data object Done : SendActionState

    fun mediaUploadInfoList(): List<MediaUploadInfo>? = when (this) {
        is Sending.ReadyToUpload -> mediaInfos
        is Sending.Uploading -> mediaInfos
        is Failure -> mediaInfos
        else -> null
    }
}
