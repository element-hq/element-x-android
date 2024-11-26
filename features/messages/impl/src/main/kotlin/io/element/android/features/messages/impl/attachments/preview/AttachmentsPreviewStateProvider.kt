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
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.local.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.local.anAudioMediaInfo
import io.element.android.libraries.mediaviewer.api.local.anImageMediaInfo
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.aTextEditorStateMarkdown

open class AttachmentsPreviewStateProvider : PreviewParameterProvider<AttachmentsPreviewState> {
    override val values: Sequence<AttachmentsPreviewState>
        get() = sequenceOf(
            anAttachmentsPreviewState(),
            anAttachmentsPreviewState(mediaInfo = aVideoMediaInfo()),
            anAttachmentsPreviewState(mediaInfo = anAudioMediaInfo()),
            anAttachmentsPreviewState(mediaInfo = anApkMediaInfo()),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Processing),
            anAttachmentsPreviewState(sendActionState = SendActionState.Sending.Uploading(0.5f)),
            anAttachmentsPreviewState(sendActionState = SendActionState.Failure(RuntimeException("error"))),
        )
}

fun anAttachmentsPreviewState(
    mediaInfo: MediaInfo = anImageMediaInfo(),
    textEditorState: TextEditorState = aTextEditorStateMarkdown(),
    sendActionState: SendActionState = SendActionState.Idle,
) = AttachmentsPreviewState(
    attachment = Attachment.Media(
        localMedia = LocalMedia("file://path".toUri(), mediaInfo),
    ),
    sendActionState = sendActionState,
    textEditorState = textEditorState,
    eventSink = {}
)
