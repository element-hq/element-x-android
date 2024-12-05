/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.runtime.Immutable
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.textcomposer.model.TextEditorState

data class AttachmentsPreviewState(
    val attachment: Attachment,
    val sendActionState: SendActionState,
    val textEditorState: TextEditorState,
    val allowCaption: Boolean,
    val showCaptionCompatibilityWarning: Boolean,
    val eventSink: (AttachmentsPreviewEvents) -> Unit
)

@Immutable
sealed interface SendActionState {
    data object Idle : SendActionState

    @Immutable
    sealed interface Sending : SendActionState {
        data object InstantSending : Sending
        data object Processing : Sending
        data class Uploading(val progress: Float) : Sending
    }

    data class Failure(val error: Throwable) : SendActionState
    data object Done : SendActionState
}
