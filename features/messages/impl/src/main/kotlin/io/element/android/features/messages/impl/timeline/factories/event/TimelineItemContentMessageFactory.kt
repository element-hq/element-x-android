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

import android.text.Spannable
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.core.text.buildSpannedString
import androidx.core.text.getSpans
import androidx.core.text.toSpannable
import androidx.core.text.util.LinkifyCompat
import io.element.android.features.location.api.Location
import io.element.android.features.messages.api.timeline.HtmlConverterProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEmoteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemNoticeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.ui.messages.toHtmlDocument
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import kotlin.time.Duration

class TimelineItemContentMessageFactory @Inject constructor(
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor,
    private val featureFlagService: FeatureFlagService,
    private val htmlConverterProvider: HtmlConverterProvider,
) {
    suspend fun create(content: MessageContent, senderDisplayName: String, eventId: EventId?): TimelineItemEventContent {
        return when (val messageType = content.type) {
            is EmoteMessageType -> {
                val emoteBody = "* $senderDisplayName ${messageType.body.trimEnd()}"
                TimelineItemEmoteContent(
                    body = emoteBody,
                    htmlDocument = messageType.formatted?.toHtmlDocument(prefix = "* $senderDisplayName"),
                    formattedBody = parseHtml(messageType.formatted, prefix = "* $senderDisplayName") ?: emoteBody.withLinks(),
                    isEdited = content.isEdited,
                )
            }
            is ImageMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemImageContent(
                    body = messageType.body.trimEnd(),
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
            is StickerMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemStickerContent(
                    body = messageType.body.trimEnd(),
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
                    val body = messageType.body.trimEnd()
                    TimelineItemTextContent(
                        body = body,
                        htmlDocument = null,
                        plainText = body,
                        formattedBody = null,
                        isEdited = content.isEdited,
                    )
                } else {
                    TimelineItemLocationContent(
                        body = messageType.body.trimEnd(),
                        location = location,
                        description = messageType.description
                    )
                }
            }
            is VideoMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemVideoContent(
                    body = messageType.body.trimEnd(),
                    thumbnailSource = messageType.info?.thumbnailSource,
                    videoSource = messageType.source,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    duration = messageType.info?.duration ?: Duration.ZERO,
                    blurHash = messageType.info?.blurhash,
                    aspectRatio = aspectRatio,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.body)
                )
            }
            is AudioMessageType -> {
                TimelineItemAudioContent(
                    body = messageType.body.trimEnd(),
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
                            body = messageType.body.trimEnd(),
                            mediaSource = messageType.source,
                            duration = messageType.info?.duration ?: Duration.ZERO,
                            mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                            waveform = messageType.details?.waveform?.toImmutableList() ?: persistentListOf(),
                        )
                    }
                    false -> {
                        TimelineItemAudioContent(
                            body = messageType.body.trimEnd(),
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
                    body = messageType.body.trimEnd(),
                    thumbnailSource = messageType.info?.thumbnailSource,
                    fileSource = messageType.source,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.fromFileExtension(fileExtension),
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtension
                )
            }
            is NoticeMessageType -> {
                val body = messageType.body.trimEnd()
                TimelineItemNoticeContent(
                    body = body,
                    htmlDocument = messageType.formatted?.toHtmlDocument(),
                    formattedBody = parseHtml(messageType.formatted) ?: body.withLinks(),
                    isEdited = content.isEdited,
                )
            }
            is TextMessageType -> {
                val body = messageType.body.trimEnd()
                TimelineItemTextContent(
                    body = body,
                    htmlDocument = messageType.formatted?.toHtmlDocument(),
                    formattedBody = parseHtml(messageType.formatted) ?: body.withLinks(),
                    isEdited = content.isEdited,
                )
            }
            is OtherMessageType -> {
                val body = messageType.body.trimEnd()
                TimelineItemTextContent(
                    body = body,
                    htmlDocument = null,
                    formattedBody = body.withLinks(),
                    isEdited = content.isEdited,
                )
            }
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

    private fun parseHtml(formattedBody: FormattedBody?, prefix: String? = null): CharSequence? {
        if (formattedBody == null || formattedBody.format != MessageFormat.HTML) return null
        val result = htmlConverterProvider.provide()
            .fromHtmlToSpans(formattedBody.body.trimEnd())
            .withFixedURLSpans()
        return if (prefix != null) {
            buildSpannedString {
                append(prefix)
                append(" ")
                append(result)
            }
        } else {
            result
        }
    }

    private fun CharSequence.withFixedURLSpans(): CharSequence {
        if (this !is Spannable) return this
        // Get all URL spans, as they will be removed by LinkifyCompat.addLinks
        val oldURLSpans = getSpans<URLSpan>(0, length).associateWith {
            val start = getSpanStart(it)
            val end = getSpanEnd(it)
            Pair(start, end)
        }
        // Find and set as URLSpans any links present in the text
        LinkifyCompat.addLinks(this, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES)
        // Restore old spans if they don't conflict with the new ones
        for ((urlSpan, location) in oldURLSpans) {
            val (start, end) = location
            if (getSpans<URLSpan>(start, end).isEmpty()) {
                setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return this
    }
}

@Suppress("USELESS_ELVIS")
private fun String.withLinks(): CharSequence? {
    // Note: toSpannable() can return null when running unit tests
    val spannable = toSpannable() ?: return null
    val addedLinks = LinkifyCompat.addLinks(spannable, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES)
    return spannable.takeIf { addedLinks }
}
