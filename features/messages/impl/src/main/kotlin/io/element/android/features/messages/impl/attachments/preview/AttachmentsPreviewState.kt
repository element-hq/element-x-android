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
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.textcomposer.model.TextEditorState

data class AttachmentsPreviewState(
    val attachment: Attachment,
    val sendActionState: SendActionState,
    val textEditorState: TextEditorState,
    val mediaOptimizationSelectorState: MediaOptimizationSelectorState,
    val displayFileTooLargeError: Boolean,
    val eventSink: (AttachmentsPreviewEvents) -> Unit
)

@Immutable
sealed interface SendActionState {
    data object Idle : SendActionState

    @Immutable
    sealed interface Sending : SendActionState {
        data class Processing(val displayProgress: Boolean) : Sending
        data class ReadyToUpload(val mediaInfo: MediaUploadInfo) : Sending
        data class Uploading(val mediaUploadInfo: MediaUploadInfo) : Sending
    }

    data class Failure(val error: Throwable, val mediaUploadInfo: MediaUploadInfo?) : SendActionState
    data object Done : SendActionState

    fun mediaUploadInfo(): MediaUploadInfo? = when (this) {
        is Sending.ReadyToUpload -> mediaInfo
        is Sending.Uploading -> mediaUploadInfo
        is Failure -> mediaUploadInfo
        else -> null
    }
}
