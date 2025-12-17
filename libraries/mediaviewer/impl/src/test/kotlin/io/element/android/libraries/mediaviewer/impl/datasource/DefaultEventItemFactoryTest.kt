/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.matrix.api.media.AudioDetails
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.VideoInfo
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
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_UNIQUE_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.aPollContent
import io.element.android.libraries.matrix.test.timeline.aProfileChangeMessageContent
import io.element.android.libraries.matrix.test.timeline.aStickerContent
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import io.element.android.libraries.matrix.test.timeline.item.event.aRoomMembershipContent
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class DefaultEventItemFactoryTest {
    @Test
    fun `create check all null cases`() {
        val factory = createEventItemFactory()
        val contents = listOf(
            CallNotifyContent,
            FailedToParseMessageLikeContent("", ""),
            FailedToParseStateContent("", "", ""),
            LegacyCallInviteContent,
            aPollContent(),
            aProfileChangeMessageContent(),
            RedactedContent,
            aRoomMembershipContent(
                userId = A_USER_ID,
            ),
            StateContent("", OtherState.RoomCreate),
            aStickerContent(
                info = ImageInfo(
                    width = null,
                    height = null,
                    mimetype = null,
                    size = null,
                    thumbnailInfo = null,
                    thumbnailSource = null,
                    blurhash = null,
                ),
                mediaSource = MediaSource("")
            ),
            UnableToDecryptContent(UnableToDecryptContent.Data.Unknown),
            UnknownContent,
        )
        contents.forEach {
            val result = factory.create(
                MatrixTimelineItem.Event(
                    uniqueId = A_UNIQUE_ID,
                    event = anEventTimelineItem(
                        content = it
                    )
                )
            )
            assertThat(result).isNull()
        }
    }

    @Test
    fun `create MessageContent check all null cases`() {
        val factory = createEventItemFactory()
        val messageTypes = listOf(
            EmoteMessageType("", null),
            NoticeMessageType("", null),
            OtherMessageType("", ""),
            LocationMessageType("", "", null),
            TextMessageType("", null)
        )
        messageTypes.forEach {
            val result = factory.create(
                MatrixTimelineItem.Event(
                    uniqueId = A_UNIQUE_ID,
                    event = anEventTimelineItem(
                        content = aMessageContent(
                            messageType = it
                        )
                    )
                )
            )
            assertThat(result).isNull()
        }
    }

    @Test
    fun `create for FileMessageType`() {
        val factory = createEventItemFactory()
        val result = factory.create(
            MatrixTimelineItem.Event(
                uniqueId = A_UNIQUE_ID,
                event = anEventTimelineItem(
                    content = aMessageContent(
                        messageType = FileMessageType(
                            filename = "filename.apk",
                            caption = "caption",
                            formattedCaption = null,
                            source = MediaSource(""),
                            info = FileInfo(
                                mimetype = MimeTypes.Apk,
                                size = 123L,
                                thumbnailInfo = null,
                                thumbnailSource = null,
                            )
                        )
                    )
                )
            )
        )
        assertThat(result).isEqualTo(
            MediaItem.File(
                id = A_UNIQUE_ID,
                eventId = AN_EVENT_ID,
                mediaInfo = MediaInfo(
                    mimeType = MimeTypes.Apk,
                    filename = "filename.apk",
                    fileSize = 123L,
                    caption = "caption",
                    formattedFileSize = "123 Bytes",
                    fileExtension = "apk",
                    senderId = A_USER_ID,
                    senderName = "alice",
                    senderAvatar = null,
                    dateSent = "0 Day false",
                    dateSentFull = "0 Full false",
                    waveform = null,
                    duration = null,
                ),
                mediaSource = MediaSource(""),
            )
        )
    }

    @Test
    fun `create for ImageMessageType`() {
        val factory = createEventItemFactory()
        val result = factory.create(
            MatrixTimelineItem.Event(
                uniqueId = A_UNIQUE_ID,
                event = anEventTimelineItem(
                    content = aMessageContent(
                        messageType = ImageMessageType(
                            filename = "filename.jpg",
                            caption = "caption",
                            formattedCaption = null,
                            source = MediaSource(""),
                            info = ImageInfo(
                                mimetype = MimeTypes.Jpeg,
                                size = 123L,
                                thumbnailInfo = null,
                                thumbnailSource = null,
                                height = 1L,
                                width = 2L,
                                blurhash = null,
                            )
                        )
                    )
                )
            )
        )
        assertThat(result).isEqualTo(
            MediaItem.Image(
                id = A_UNIQUE_ID,
                eventId = AN_EVENT_ID,
                mediaInfo = MediaInfo(
                    mimeType = MimeTypes.Jpeg,
                    filename = "filename.jpg",
                    fileSize = 123L,
                    caption = "caption",
                    formattedFileSize = "123 Bytes",
                    fileExtension = "jpg",
                    senderId = A_USER_ID,
                    senderName = "alice",
                    senderAvatar = null,
                    dateSent = "0 Day false",
                    dateSentFull = "0 Full false",
                    waveform = null,
                    duration = null,
                ),
                mediaSource = MediaSource(""),
                thumbnailSource = null,
            )
        )
    }

    @Test
    fun `create for AudioMessageType`() {
        val factory = createEventItemFactory()
        val result = factory.create(
            MatrixTimelineItem.Event(
                uniqueId = A_UNIQUE_ID,
                event = anEventTimelineItem(
                    content = aMessageContent(
                        messageType = AudioMessageType(
                            filename = "filename.mp3",
                            caption = "caption",
                            formattedCaption = null,
                            source = MediaSource(""),
                            info = AudioInfo(
                                mimetype = MimeTypes.Mp3,
                                size = 123L,
                                duration = 456.seconds,
                            )
                        )
                    )
                )
            )
        )
        assertThat(result).isEqualTo(
            MediaItem.Audio(
                id = A_UNIQUE_ID,
                eventId = AN_EVENT_ID,
                mediaInfo = MediaInfo(
                    mimeType = MimeTypes.Mp3,
                    filename = "filename.mp3",
                    fileSize = 123L,
                    caption = "caption",
                    formattedFileSize = "123 Bytes",
                    fileExtension = "mp3",
                    senderId = A_USER_ID,
                    senderName = "alice",
                    senderAvatar = null,
                    dateSent = "0 Day false",
                    dateSentFull = "0 Full false",
                    waveform = null,
                    duration = null,
                ),
                mediaSource = MediaSource(""),
            )
        )
    }

    @Test
    fun `create for VideoMessageType`() {
        val factory = createEventItemFactory()
        val result = factory.create(
            MatrixTimelineItem.Event(
                uniqueId = A_UNIQUE_ID,
                event = anEventTimelineItem(
                    content = aMessageContent(
                        messageType = VideoMessageType(
                            filename = "filename.mp4",
                            caption = "caption",
                            formattedCaption = null,
                            source = MediaSource(""),
                            info = VideoInfo(
                                mimetype = MimeTypes.Mp4,
                                size = 123L,
                                thumbnailInfo = null,
                                duration = 123.seconds,
                                height = 1L,
                                width = 2L,
                                thumbnailSource = null,
                                blurhash = null
                            )
                        )
                    )
                )
            )
        )
        assertThat(result).isEqualTo(
            MediaItem.Video(
                id = A_UNIQUE_ID,
                eventId = AN_EVENT_ID,
                mediaInfo = MediaInfo(
                    mimeType = MimeTypes.Mp4,
                    filename = "filename.mp4",
                    fileSize = 123L,
                    caption = "caption",
                    formattedFileSize = "123 Bytes",
                    fileExtension = "mp4",
                    senderId = A_USER_ID,
                    senderName = "alice",
                    senderAvatar = null,
                    dateSent = "0 Day false",
                    dateSentFull = "0 Full false",
                    waveform = null,
                    duration = "2:03",
                ),
                mediaSource = MediaSource(""),
                thumbnailSource = null,
            )
        )
    }

    @Test
    fun `create for VoiceMessageType`() {
        val factory = createEventItemFactory()
        val result = factory.create(
            MatrixTimelineItem.Event(
                uniqueId = A_UNIQUE_ID,
                event = anEventTimelineItem(
                    content = aMessageContent(
                        messageType = VoiceMessageType(
                            filename = "filename.ogg",
                            caption = "caption",
                            formattedCaption = null,
                            source = MediaSource(""),
                            info = AudioInfo(
                                mimetype = MimeTypes.Ogg,
                                size = 123L,
                                duration = 456.seconds,
                            ),
                            details = AudioDetails(
                                duration = 456.seconds,
                                waveform = persistentListOf(1f, 2f),
                            )
                        )
                    )
                )
            )
        )
        assertThat(result).isEqualTo(
            MediaItem.Voice(
                id = A_UNIQUE_ID,
                eventId = AN_EVENT_ID,
                mediaInfo = MediaInfo(
                    mimeType = MimeTypes.Ogg,
                    filename = "filename.ogg",
                    fileSize = 123L,
                    caption = "caption",
                    formattedFileSize = "123 Bytes",
                    fileExtension = "ogg",
                    senderId = A_USER_ID,
                    senderName = "alice",
                    senderAvatar = null,
                    dateSent = "0 Day false",
                    dateSentFull = "0 Full false",
                    waveform = listOf(1f, 2f).toImmutableList(),
                    duration = "7:36",
                ),
                mediaSource = MediaSource(""),
            )
        )
    }

    @Test
    fun `create for StickerMessageType`() {
        val factory = createEventItemFactory()
        val result = factory.create(
            MatrixTimelineItem.Event(
                uniqueId = A_UNIQUE_ID,
                event = anEventTimelineItem(
                    content = aMessageContent(
                        messageType = StickerMessageType(
                            filename = "filename.gif",
                            caption = "caption",
                            formattedCaption = null,
                            source = MediaSource(""),
                            info = ImageInfo(
                                mimetype = MimeTypes.Gif,
                                size = 123L,
                                thumbnailInfo = null,
                                thumbnailSource = null,
                                height = 1L,
                                width = 2L,
                                blurhash = null,
                            )
                        )
                    )
                )
            )
        )
        assertThat(result).isEqualTo(
            MediaItem.Image(
                id = A_UNIQUE_ID,
                eventId = AN_EVENT_ID,
                mediaInfo = MediaInfo(
                    mimeType = MimeTypes.Gif,
                    filename = "filename.gif",
                    fileSize = 123L,
                    caption = "caption",
                    formattedFileSize = "123 Bytes",
                    fileExtension = "gif",
                    senderId = A_USER_ID,
                    senderName = "alice",
                    senderAvatar = null,
                    dateSent = "0 Day false",
                    dateSentFull = "0 Full false",
                    waveform = null,
                    duration = null,
                ),
                mediaSource = MediaSource(""),
                thumbnailSource = null,
            )
        )
    }
}

private fun createEventItemFactory() = EventItemFactory(
    fileSizeFormatter = FakeFileSizeFormatter(),
    fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
    dateFormatter = FakeDateFormatter(),
)
