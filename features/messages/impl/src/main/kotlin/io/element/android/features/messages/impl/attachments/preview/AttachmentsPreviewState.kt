/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.textcomposer.model.TextEditorState

data class AttachmentsPreviewState(
    val attachment: Attachment,
    val sendActionState: SendActionState,
    val textEditorState: TextEditorState,
    val eventSink: (AttachmentsPreviewEvents) -> Unit
) {
    val allowCaption: Boolean = (attachment as? Attachment.Media)?.localMedia?.info?.mimeType?.let {
        it.isMimeTypeImage() || it.isMimeTypeVideo()
    }.orFalse()
}

@Immutable
sealed interface SendActionState {
    data object Idle : SendActionState

    @Immutable
    sealed interface Sending : SendActionState {
        data object Processing : Sending
        data class Uploading(val progress: Float) : Sending
    }

    data class Failure(val error: Throwable) : SendActionState
}
