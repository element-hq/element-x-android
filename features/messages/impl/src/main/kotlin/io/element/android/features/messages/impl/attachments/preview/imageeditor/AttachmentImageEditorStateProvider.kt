/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.preview.imageeditor

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.net.toUri
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia

open class AttachmentImageEditorStateProvider : PreviewParameterProvider<AttachmentImageEditorState> {
    private val caterpillarCrop = NormalizedCropRect(
        left = 0.3f,
        top = 0.3f,
        right = 0.8f,
        bottom = 0.75f,
    )

    override val values: Sequence<AttachmentImageEditorState>
        get() = sequenceOf(
            anAttachmentImageEditorState(
                edits = AttachmentImageEdits(
                    // Cheat a bit so that the crop match the sample image size (1024 * 682)
                    cropRect = 0.17f.let { correction ->
                        NormalizedCropRect(
                            left = 0f,
                            top = correction,
                            right = 1f,
                            bottom = 1 - correction,
                        )
                    },
                ),
            ),
            anAttachmentImageEditorState(
                edits = AttachmentImageEdits(
                    cropRect = caterpillarCrop,
                ),
            ),
            anAttachmentImageEditorState(
                edits = AttachmentImageEdits(
                    cropRect = caterpillarCrop,
                ),
                previewDebug = true,
            ),
            anAttachmentImageEditorState(
                edits = AttachmentImageEdits(
                    cropRect = caterpillarCrop,
                ).rotateAntiClockwise(),
            ),
            // Small crop
            anAttachmentImageEditorState(
                edits = AttachmentImageEdits(
                    cropRect = NormalizedCropRect(
                        left = 0.3f,
                        top = 0.6f,
                        right = 0.4f,
                        bottom = 0.7f,
                    ),
                ),
                previewDebug = true,
            ),
            // Big crop
            anAttachmentImageEditorState(
                edits = AttachmentImageEdits(
                    cropRect = NormalizedCropRect(
                        left = 0.05f,
                        top = 0.05f,
                        right = 0.95f,
                        bottom = 0.95f,
                    ),
                ),
                previewDebug = true,
            ),
        )
}

internal fun anAttachmentImageEditorState(
    localMedia: LocalMedia = LocalMedia(
        uri = "file://preview-image".toUri(),
        info = anImageMediaInfo(),
    ),
    edits: AttachmentImageEdits = AttachmentImageEdits(),
    previewDebug: Boolean = false,
) = AttachmentImageEditorState(
    localMedia = localMedia,
    edits = edits,
    previewDebug = previewDebug,
)
