/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.aTextEditorStateMarkdown

open class AttachmentsPreviewStateProvider : PreviewParameterProvider<AttachmentsPreviewState> {
    override val values: Sequence<AttachmentsPreviewState>
        get() = sequenceOf(
            anAttachmentsPreviewState(),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Processing),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Uploading(0.5f)),
            anAttachmentsPreviewState(sendActionState = SendActionState.Failure(RuntimeException("error"))),
            anAttachmentsPreviewState(allowCaption = false),
            anAttachmentsPreviewState(showCaptionCompatibilityWarning = true),
        )
}

fun anAttachmentsPreviewState(
    mediaInfo: MediaInfo = anImageMediaInfo(),
    textEditorState: TextEditorState = aTextEditorStateMarkdown(),
    sendActionState: SendActionState = SendActionState.Idle,
    allowCaption: Boolean = true,
    showCaptionCompatibilityWarning: Boolean = true,
) = AttachmentsPreviewState(
    attachment = Attachment.Media(
        localMedia = LocalMedia("file://path".toUri(), mediaInfo),
    ),
    sendActionState = sendActionState,
    textEditorState = textEditorState,
    allowCaption = allowCaption,
    showCaptionCompatibilityWarning = showCaptionCompatibilityWarning,
    eventSink = {}
)
