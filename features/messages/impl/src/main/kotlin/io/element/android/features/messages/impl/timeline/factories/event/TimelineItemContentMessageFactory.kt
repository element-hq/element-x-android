/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.features.messages.impl.utils.TextPillificationHelper
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
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
    private val permalinkParser: PermalinkParser,
    private val textPillificationHelper: TextPillificationHelper,
) {
    suspend fun create(
        content: MessageContent,
        senderDisambiguatedDisplayName: String,
        eventId: EventId?,
    ): TimelineItemEventContent {
        return when (val messageType = content.type) {
            is EmoteMessageType -> {
                val emoteBody = "* $senderDisambiguatedDisplayName ${messageType.body.trimEnd()}"
                TimelineItemEmoteContent(
                    body = emoteBody,
                    htmlDocument = messageType.formatted?.toHtmlDocument(
                        permalinkParser = permalinkParser,
                        prefix = "* $senderDisambiguatedDisplayName",
                    ),
                    formattedBody = parseHtml(messageType.formatted, prefix = "* $senderDisambiguatedDisplayName") ?: emoteBody.withLinks(),
                    isEdited = content.isEdited,
                )
            }
            is ImageMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemImageContent(
                    filename = messageType.filename,
                    caption = messageType.caption?.trimEnd(),
                    formattedCaption = messageType.formattedCaption,
                    mediaSource = messageType.source,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    blurhash = messageType.info?.blurhash,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    aspectRatio = aspectRatio,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.filename)
                )
            }
            is StickerMessageType -> {
                val aspectRatio = aspectRatioOf(messageType.info?.width, messageType.info?.height)
                TimelineItemStickerContent(
                    filename = messageType.filename,
                    caption = messageType.caption?.trimEnd(),
                    formattedCaption = messageType.formattedCaption,
                    mediaSource = messageType.source,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    blurhash = messageType.info?.blurhash,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    aspectRatio = aspectRatio,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.filename)
                )
            }
            is LocationMessageType -> {
                val location = Location.fromGeoUri(messageType.geoUri)
                if (location == null) {
                    val body = messageType.body.trimEnd()
                    TimelineItemTextContent(
                        body = body,
                        pillifiedBody = textPillificationHelper.pillify(body),
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
                    filename = messageType.filename,
                    caption = messageType.caption?.trimEnd(),
                    formattedCaption = messageType.formattedCaption,
                    thumbnailSource = messageType.info?.thumbnailSource,
                    videoSource = messageType.source,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    width = messageType.info?.width?.toInt(),
                    height = messageType.info?.height?.toInt(),
                    duration = messageType.info?.duration ?: Duration.ZERO,
                    blurHash = messageType.info?.blurhash,
                    aspectRatio = aspectRatio,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.filename),
                )
            }
            is AudioMessageType -> {
                TimelineItemAudioContent(
                    filename = messageType.filename,
                    caption = messageType.caption?.trimEnd(),
                    formattedCaption = messageType.formattedCaption,
                    mediaSource = messageType.source,
                    duration = messageType.info?.duration ?: Duration.ZERO,
                    mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                    formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                    fileExtension = fileExtensionExtractor.extractFromName(messageType.filename),
                )
            }
            is VoiceMessageType -> {
                when (featureFlagService.isFeatureEnabled(FeatureFlags.VoiceMessages)) {
                    true -> {
                        TimelineItemVoiceContent(
                            eventId = eventId,
                            filename = messageType.filename,
                            caption = messageType.caption?.trimEnd(),
                            formattedCaption = messageType.formattedCaption,
                            mediaSource = messageType.source,
                            duration = messageType.info?.duration ?: Duration.ZERO,
                            mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                            waveform = messageType.details?.waveform?.toImmutableList() ?: persistentListOf(),
                        )
                    }
                    false -> {
                        TimelineItemAudioContent(
                            filename = messageType.filename,
                            caption = messageType.caption?.trimEnd(),
                            formattedCaption = messageType.formattedCaption,
                            mediaSource = messageType.source,
                            duration = messageType.info?.duration ?: Duration.ZERO,
                            mimeType = messageType.info?.mimetype ?: MimeTypes.OctetStream,
                            formattedFileSize = fileSizeFormatter.format(messageType.info?.size ?: 0),
                            fileExtension = fileExtensionExtractor.extractFromName(messageType.filename),
                        )
                    }
                }
            }
            is FileMessageType -> {
                val fileExtension = fileExtensionExtractor.extractFromName(messageType.filename)
                TimelineItemFileContent(
                    filename = messageType.filename,
                    caption = messageType.caption?.trimEnd(),
                    formattedCaption = messageType.formattedCaption,
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
                    htmlDocument = messageType.formatted?.toHtmlDocument(permalinkParser = permalinkParser),
                    formattedBody = parseHtml(messageType.formatted) ?: body.withLinks(),
                    isEdited = content.isEdited,
                )
            }
            is TextMessageType -> {
                val body = messageType.body.trimEnd()
                TimelineItemTextContent(
                    body = body,
                    pillifiedBody = textPillificationHelper.pillify(body),
                    htmlDocument = messageType.formatted?.toHtmlDocument(permalinkParser = permalinkParser),
                    formattedBody = parseHtml(messageType.formatted) ?: body.withLinks(),
                    isEdited = content.isEdited,
                )
            }
            is OtherMessageType -> {
                val body = messageType.body.trimEnd()
                TimelineItemTextContent(
                    body = body,
                    pillifiedBody = textPillificationHelper.pillify(body),
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
        val spannable = this.toSpannable()
        // Get all URL spans, as they will be removed by LinkifyCompat.addLinks
        val oldURLSpans = spannable.getSpans<URLSpan>(0, length).associateWith {
            val start = spannable.getSpanStart(it)
            val end = spannable.getSpanEnd(it)
            Pair(start, end)
        }
        // Find and set as URLSpans any links present in the text
        LinkifyCompat.addLinks(spannable, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES)
        // Restore old spans, remove new ones if there is a conflict
        for ((urlSpan, location) in oldURLSpans) {
            val (start, end) = location
            val addedSpans = spannable.getSpans<URLSpan>(start, end).orEmpty()
            if (addedSpans.isNotEmpty()) {
                for (span in addedSpans) {
                    spannable.removeSpan(span)
                }
            }
            spannable.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannable
    }
}

@Suppress("USELESS_ELVIS")
private fun String.withLinks(): CharSequence? {
    // Note: toSpannable() can return null when running unit tests
    val spannable = toSpannable() ?: return null
    val addedLinks = LinkifyCompat.addLinks(spannable, Linkify.WEB_URLS or Linkify.PHONE_NUMBERS or Linkify.EMAIL_ADDRESSES)
    return spannable.takeIf { addedLinks }
}
