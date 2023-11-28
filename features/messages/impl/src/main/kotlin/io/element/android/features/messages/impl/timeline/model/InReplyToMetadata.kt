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

package io.element.android.features.messages.impl.timeline.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.ui.strings.CommonStrings

@Immutable
internal sealed interface InReplyToMetadata {

    val text: String?

    data class Thumbnail(
        val attachmentThumbnailInfo: AttachmentThumbnailInfo
    ) : InReplyToMetadata {
        override val text: String? = attachmentThumbnailInfo.textContent
    }

    data class Text(
        override val text: String
    ) : InReplyToMetadata
}

/**
 * Computes metadata for the in reply to message.
 *
 * Metadata can be either a thumbnail with a text OR just a text.
 */
@Composable
internal fun InReplyToDetails.metadata(): InReplyToMetadata? = when (eventContent) {
    is MessageContent -> when (val type = eventContent.type) {
        is ImageMessageType -> InReplyToMetadata.Thumbnail(
            AttachmentThumbnailInfo(
                thumbnailSource = type.info?.thumbnailSource ?: type.source,
                textContent = eventContent.body,
                type = AttachmentThumbnailType.Image,
                blurHash = type.info?.blurhash,
            )
        )
        is VideoMessageType -> InReplyToMetadata.Thumbnail(
            AttachmentThumbnailInfo(
                thumbnailSource = type.info?.thumbnailSource,
                textContent = eventContent.body,
                type = AttachmentThumbnailType.Video,
                blurHash = type.info?.blurhash,
            )
        )
        is FileMessageType -> InReplyToMetadata.Thumbnail(
            AttachmentThumbnailInfo(
                thumbnailSource = type.info?.thumbnailSource,
                textContent = eventContent.body,
                type = AttachmentThumbnailType.File,
            )
        )
        is LocationMessageType -> InReplyToMetadata.Thumbnail(
            AttachmentThumbnailInfo(
                textContent = stringResource(CommonStrings.common_shared_location),
                type = AttachmentThumbnailType.Location,
            )
        )
        is AudioMessageType -> InReplyToMetadata.Thumbnail(
            AttachmentThumbnailInfo(
                textContent = eventContent.body,
                type = AttachmentThumbnailType.Audio,
            )
        )
        is VoiceMessageType -> InReplyToMetadata.Thumbnail(
            AttachmentThumbnailInfo(
                textContent = stringResource(CommonStrings.common_voice_message),
                type = AttachmentThumbnailType.Voice,
            )
        )
        else -> InReplyToMetadata.Text(textContent ?: eventContent.body)
    }
    is PollContent -> InReplyToMetadata.Thumbnail(
        AttachmentThumbnailInfo(
            textContent = eventContent.question,
            type = AttachmentThumbnailType.Poll,
        )
    )
    else -> null
}
