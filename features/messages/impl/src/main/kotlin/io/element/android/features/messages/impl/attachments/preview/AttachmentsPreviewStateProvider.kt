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

