/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.dateformatter.api.toHumanReadableDuration
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.GalleryItemType
import io.element.android.libraries.matrix.api.timeline.item.event.GalleryMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
import io.element.android.libraries.matrix.api.timeline.item.event.LiveLocationContent
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.PollContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.getAvatarUrl
import io.element.android.libraries.matrix.api.timeline.item.event.getDisambiguatedDisplayName
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import timber.log.Timber

@Inject
class EventItemFactory(
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor,
    private val dateFormatter: DateFormatter,
) {
    fun create(
        currentTimelineItem: MatrixTimelineItem.Event,
    ): List<MediaItem.Event> {
        val event = currentTimelineItem.event
        val dateSent = dateFormatter.format(
            currentTimelineItem.event.timestamp,
            mode = DateFormatterMode.Day,
        )
        val dateSentFull = dateFormatter.format(
            timestamp = currentTimelineItem.event.timestamp,
            mode = DateFormatterMode.Full,
        )
        return when (val content = event.content) {
            CallNotifyContent,
            is FailedToParseMessageLikeContent,
            is FailedToParseStateContent,
            LegacyCallInviteContent,
            is PollContent,
            is ProfileChangeContent,
            RedactedContent,
            is RoomMembershipContent,
            is StateContent,
            is StickerContent,
            is UnableToDecryptContent,
            is LiveLocationContent,
            UnknownContent -> {
                Timber.w("Should not happen: ${content.javaClass.simpleName}")
                emptyList()
            }
            is MessageContent -> {
                when (val type = content.type) {
                    is EmoteMessageType,
                    is NoticeMessageType,
                    is OtherMessageType,
                    is LocationMessageType,
                    is TextMessageType -> {
                        Timber.w("Should not happen: ${content.type}")
                        emptyList()
                    }
                    is GalleryMessageType -> {
                        val baseId = currentTimelineItem.uniqueId.value
                        type.items.mapIndexedNotNull { index, galleryItem ->
                            val id = UniqueId("${baseId}_$index")
                            when (galleryItem) {
                                is GalleryItemType.Image -> {
                                    val c = galleryItem.content
                                    MediaItem.Image(
                                        id = id,
                                        eventId = currentTimelineItem.eventId,
                                        mediaInfo = createMediaInfo(c.filename, c.info?.size, c.caption, c.info?.mimetype.orEmpty(), c.filename, event, dateSent, dateSentFull),
                                        mediaSource = c.source,
                                        thumbnailSource = c.info?.thumbnailSource,
                                    )
                                }
                                is GalleryItemType.Video -> {
                                    val c = galleryItem.content
                                    MediaItem.Video(
                                        id = id,
                                        eventId = currentTimelineItem.eventId,
                                        mediaInfo = createMediaInfo(c.filename, c.info?.size, c.caption, c.info?.mimetype.orEmpty(), c.filename, event, dateSent, dateSentFull, duration = c.info?.duration?.inWholeMilliseconds?.toHumanReadableDuration()),
                                        mediaSource = c.source,
                                        thumbnailSource = c.info?.thumbnailSource,
                                    )
                                }
                                is GalleryItemType.Audio -> {
                                    val c = galleryItem.content
                                    MediaItem.Audio(
                                        id = id,
                                        eventId = currentTimelineItem.eventId,
                                        mediaInfo = createMediaInfo(c.filename, c.info?.size, c.caption, c.info?.mimetype.orEmpty(), c.filename, event, dateSent, dateSentFull),
                                        mediaSource = c.source,
                                    )
                                }
                                is GalleryItemType.File -> {
                                    val c = galleryItem.content
                                    MediaItem.File(
                                        id = id,
                                        eventId = currentTimelineItem.eventId,
                                        mediaInfo = createMediaInfo(c.filename, c.info?.size, c.caption, c.info?.mimetype.orEmpty(), c.filename, event, dateSent, dateSentFull),
                                        mediaSource = c.source,
                                    )
                                }
                                is GalleryItemType.Other -> null
                            }
                        }
                    }
                    is AudioMessageType -> listOf(MediaItem.Audio(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            fileSize = type.info?.size,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = dateSent,
                            dateSentFull = dateSentFull,
                            waveform = null,
                            duration = null,
                        ),
                        mediaSource = type.source,
                    ))
                    is FileMessageType -> listOf(MediaItem.File(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            fileSize = type.info?.size,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = dateSent,
                            dateSentFull = dateSentFull,
                            waveform = null,
                            duration = null,
                        ),
                        mediaSource = type.source,
                        // TODO We may want to add a thumbnailSource and set it to type.info?.thumbnailSource
                    ))
                    is ImageMessageType -> listOf(MediaItem.Image(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            fileSize = type.info?.size,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = dateSent,
                            dateSentFull = dateSentFull,
                            waveform = null,
                            duration = null,
                        ),
                        mediaSource = type.source,
                        thumbnailSource = type.info?.thumbnailSource,
                    ))
                    is StickerMessageType -> listOf(MediaItem.Image(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            fileSize = type.info?.size,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = dateSent,
                            dateSentFull = dateSentFull,
                            waveform = null,
                            duration = null,
                        ),
                        mediaSource = type.source,
                        thumbnailSource = type.info?.thumbnailSource,
                    ))
                    is VideoMessageType -> listOf(MediaItem.Video(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            fileSize = type.info?.size,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = dateSent,
                            dateSentFull = dateSentFull,
                            waveform = null,
                            duration = type.info?.duration?.inWholeMilliseconds?.toHumanReadableDuration(),
                        ),
                        mediaSource = type.source,
                        thumbnailSource = type.info?.thumbnailSource,
                    ))
                    is VoiceMessageType -> listOf(MediaItem.Voice(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            fileSize = type.info?.size,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = dateSent,
                            dateSentFull = dateSentFull,
                            waveform = type.details?.waveform.orEmpty(),
                            duration = type.info?.duration?.inWholeMilliseconds?.toHumanReadableDuration(),
                        ),
                        mediaSource = type.source,
                    ))
                }
            }
        }
    }

    private fun createMediaInfo(
        filename: String,
        fileSize: Long?,
        caption: String?,
        mimeType: String,
        fileExtension: String,
        event: io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem,
        dateSent: String,
        dateSentFull: String,
        waveform: List<Float>? = null,
        duration: String? = null,
    ) = MediaInfo(
        filename = filename,
        fileSize = fileSize,
        caption = caption,
        mimeType = mimeType,
        formattedFileSize = fileSize?.let { fileSizeFormatter.format(it) }.orEmpty(),
        fileExtension = fileExtensionExtractor.extractFromName(fileExtension),
        senderId = event.sender,
        senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
        senderAvatar = event.senderProfile.getAvatarUrl(),
        dateSent = dateSent,
        dateSentFull = dateSentFull,
        waveform = waveform,
        duration = duration,
    )
}
