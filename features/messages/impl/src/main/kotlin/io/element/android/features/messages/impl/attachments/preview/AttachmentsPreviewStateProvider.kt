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
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.anApkMediaInfo
import kotlinx.collections.immutable.toImmutableList

open class AttachmentsPreviewStateProvider : PreviewParameterProvider<AttachmentsState> {
    val attachmentList =
        mutableListOf(
            Attachment.Media(
                localMedia = LocalMedia("file://path".toUri(), anApkMediaInfo()),
                compressIfPossible = true)
        ).toImmutableList()
    override val values: Sequence<AttachmentsState>
        get() = sequenceOf(
            AttachmentsState.None,
            AttachmentsState.Previewing(attachmentList),
            AttachmentsState.Sending.Processing(attachmentList),
            AttachmentsState.Sending.Uploading(25.0F)
        )
}

