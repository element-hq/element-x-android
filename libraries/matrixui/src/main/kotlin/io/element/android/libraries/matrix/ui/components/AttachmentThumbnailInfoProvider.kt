/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
