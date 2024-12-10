/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.dateformatter.api.toHumanReadableDuration
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.CallNotifyContent
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LegacyCallInviteContent
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
import timber.log.Timber
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

class EventItemFactory @Inject constructor(
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor,
) {
    private val timeFormatter = DateFormat.getDateInstance()

    fun create(
        currentTimelineItem: MatrixTimelineItem.Event,
    ): MediaItem.Event? {
        val event = currentTimelineItem.event
        val sentTime = timeFormatter.format(Date(currentTimelineItem.event.timestamp))
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
            UnknownContent -> {
                Timber.w("Should not happen: ${content.javaClass.simpleName}")
                null
            }
            is MessageContent -> {
                when (val type = content.type) {
                    is EmoteMessageType,
                    is NoticeMessageType,
                    is OtherMessageType,
                    is LocationMessageType,
                    is TextMessageType -> {
                        Timber.w("Should not happen: ${content.type}")
                        null
                    }
                    is AudioMessageType -> MediaItem.File(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = sentTime,
                        ),
                        mediaSource = type.source,
                    )
                    is FileMessageType -> MediaItem.File(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = sentTime,
                        ),
                        mediaSource = type.source,
                    )
                    is ImageMessageType -> MediaItem.Image(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = sentTime,
                        ),
                        mediaSource = type.source,
                        thumbnailSource = null,
                    )
                    is StickerMessageType -> MediaItem.Image(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = sentTime,
                        ),
                        mediaSource = type.source,
                        thumbnailSource = null,
                    )
                    is VideoMessageType -> MediaItem.Video(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = sentTime,
                        ),
                        mediaSource = type.source,
                        thumbnailSource = type.info?.thumbnailSource,
                        duration = type.info?.duration?.inWholeMilliseconds?.toHumanReadableDuration(),
                    )
                    is VoiceMessageType -> MediaItem.File(
                        id = currentTimelineItem.uniqueId,
                        eventId = currentTimelineItem.eventId,
                        mediaInfo = MediaInfo(
                            filename = type.filename,
                            caption = type.caption,
                            mimeType = type.info?.mimetype.orEmpty(),
                            formattedFileSize = type.info?.size?.let { fileSizeFormatter.format(it) }.orEmpty(),
                            fileExtension = fileExtensionExtractor.extractFromName(type.filename),
                            senderId = event.sender,
                            senderName = event.senderProfile.getDisambiguatedDisplayName(event.sender),
                            senderAvatar = event.senderProfile.getAvatarUrl(),
                            dateSent = sentTime,
                        ),
                        mediaSource = type.source,
                    )
                }
            }
        }
    }
}
