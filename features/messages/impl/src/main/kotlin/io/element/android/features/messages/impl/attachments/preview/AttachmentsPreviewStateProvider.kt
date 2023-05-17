/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.attachments.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.media.local.LocalMedia
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.core.mimetype.MimeTypes

open class AttachmentsPreviewStateProvider : PreviewParameterProvider<AttachmentsPreviewState> {
    override val values: Sequence<AttachmentsPreviewState>
        get() = sequenceOf(
            anAttachmentsPreviewState(),
            anAttachmentsPreviewState(sendActionState = Async.Loading()),
            anAttachmentsPreviewState(sendActionState = Async.Failure(RuntimeException())),
            // Add other states here
        )
}

fun anAttachmentsPreviewState(sendActionState: Async<Unit> = Async.Uninitialized) = AttachmentsPreviewState(
    attachment = Attachment.Media(
        localMedia = LocalMedia("".toUri(), mimeType = MimeTypes.OctetStream),
    ),
    sendActionState = sendActionState,
    eventSink = {}
)
