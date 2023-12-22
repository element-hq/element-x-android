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

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.media.MediaSource

open class AttachmentThumbnailInfoProvider : PreviewParameterProvider<AttachmentThumbnailInfo> {
    override val values: Sequence<AttachmentThumbnailInfo>
        get() = sequenceOf(
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Image),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Image, blurHash = A_BLUR_HASH),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Video),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Video, blurHash = A_BLUR_HASH),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Audio),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.File),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Location),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Voice),
            anAttachmentThumbnailInfo(type = AttachmentThumbnailType.Poll),
        )
}

fun anAttachmentThumbnailInfo(
    type: AttachmentThumbnailType,
    thumbnailSource: MediaSource? = null,
    textContent: String? = null,
    blurHash: String? = null,
) =
    AttachmentThumbnailInfo(
        type = type,
        thumbnailSource = thumbnailSource,
        textContent = textContent,
        blurHash = blurHash,
    )

const val A_BLUR_HASH = "TQF5:I_NtRE4kXt7Z#MwkCIARPjr"
