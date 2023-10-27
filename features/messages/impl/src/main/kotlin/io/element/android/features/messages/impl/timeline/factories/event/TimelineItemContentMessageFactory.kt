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

package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.features.location.api.Location
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEmoteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemNoticeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.util.FileExtensionExtractor
import io.element.android.features.messages.impl.timeline.util.toHtmlDocument
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Duration
import javax.inject.Inject

class TimelineItemContentMessageFactory @Inject constructor(
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor,
    private val featureFlagService: FeatureFlagService,
) {

    suspend fun create(content: MessageContent, senderDisplayName: String, eventId: EventId?): TimelineItemEventContent {
        return when (val messageType = content.type) {
            is EmoteMessageType -> TimelineItemEmoteContent(
                body = "* $senderDisplayName ${messageType.body}",
                htmlDocument = messageType.formatted?.toHtmlDocument(prefix = "* senderDisplayName"),
                isEdited = content.isEdited,
            )
            is ImageMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemImageContent(
                    body = messageType.body,
                    mediaSource = messageType.source,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    blurhash = messageType.info?.blurhash,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    aspectRatio = aspectRatio,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.body)
                )
            }
            is LocationMessageType -> {
                val location = Location.fromGeoUri(messageType.geoUri)
                if (location == null) {
                    TimelineItemTextContent(
                        body = messageType.body,
                        htmlDocument = null,
                        isEdited = content.isEdited,
                    )
                } else {
                    TimelineItemLocationContent(
                        body = messageType.body,
                        location = location,
                        description = messageType.description
                    )
                }
            }
            is VideoMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemVideoContent(
                    body = messageType.body,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    videoSource = messageType.source,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    duration = messageType.info?.duration?.toMillis() ?: 0L,
                    blurHash = messageType.info?.blurhash,
                    aspectRatio = aspectRatio,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.body)
                )
            }
            is AudioMessageType -> {
                TimelineItemAudioContent(
                    body = messageType.body,
                    mediaSource = messageType.source,
                    duration = messageType.info?.duration ?: Duration.ZERO,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.body),
                )
            }
            is VoiceMessageType -> {
                when (featureFlagService.isFeatureEnabled(FeatureFlags.VoiceMessages)) {
                    true -> {
                        TimelineItemVoiceContent(
                            eventId = eventId,
                            body = messageType.body,
                            mediaSource = messageType.source,
                            duration = messageType.info?.duration ?: Duration.ZERO,
                            mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                            waveform = messageType.details?.waveform?.toImmutableList() ?: persistentListOf(),
                        )
                    }
                    false -> {
                        TimelineItemAudioContent(
                            body = messageType.body,
                            mediaSource = messageType.source,
                            duration = messageType.info?.duration ?: Duration.ZERO,
                            mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                            formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                            fileExtension = fileExtensionExtractor.extractFromName(messageType.body),
                        )
                    }
                }
            }
            is FileMessageType -> {
                val fileExtension = fileExtensionExtractor.extractFromName(messageType.body)
                TimelineItemFileContent(
                    body = messageType.body,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    fileSource = messageType.source,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.fromFileExtension(fileExtension),
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtension
                )
            }
            is NoticeMessageType -> TimelineItemNoticeContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument(),
                isEdited = content.isEdited,
            )
            is TextMessageType -> TimelineItemTextContent(
                body = messageType.body,
                htmlDocument = messageType.formatted?.toHtmlDocument(),
                isEdited = content.isEdited,
            )
            is OtherMessageType -> TimelineItemTextContent(
                body = messageType.body,
                htmlDocument = null,
                isEdited = content.isEdited,
            )
            UnknownMessageType -> TimelineItemTextContent(
                // Display the body as a fallback, but should not happen anymore
                // (we have `OtherMessageType` now)
                body = content.body,
                htmlDocument = null,
                isEdited = content.isEdited,
            )
        }
    }

    private fun aspectRatioOf(width: Long?, height: Long?): Float? {
        val result = if (height != null && width != null) {
            width.toFloat() / height.toFloat()
        } else {
            null
        }

        return result?.takeIf { it.isFinite() }
    }
}
