/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.factories.event

import android.net.Uri
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.URLSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.text.toSpannable
import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.api.Location
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEmoteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemNoticeContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.utils.FakeTextPillificationHelper
import io.element.android.features.messages.test.timeline.FakeHtmlConverterProvider
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.media.AudioDetails
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.matrix.test.timeline.aStickerContent
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractorWithoutValidation
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class TimelineItemContentMessageFactoryTest {
    @Test
    fun `test create OtherMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = OtherMessageType(msgType = "a_type", body = "body")),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemTextContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            isEdited = false,
            formattedBody = null,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create LocationMessageType not null`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = LocationMessageType("body", "geo:1,2", "description")),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemLocationContent(
            body = "body",
            location = Location(lat = 1.0, lon = 2.0, accuracy = 0.0F),
            description = "description",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create LocationMessageType null`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = LocationMessageType("body", "", null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemTextContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            isEdited = false,
            formattedBody = null,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create TextMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = TextMessageType("body", null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemTextContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            isEdited = false,
            formattedBody = null,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create TextMessageType with simple link`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = TextMessageType("https://www.example.org", null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        ) as TimelineItemTextContent
        val expected = TimelineItemTextContent(
            body = "https://www.example.org",
            htmlDocument = null,
            plainText = "https://www.example.org",
            isEdited = false,
            formattedBody = buildSpannedString {
                inSpans(URLSpan("https://www.example.org")) {
                    append("https://www.example.org")
                }
            }
        )
        assertThat(result.body).isEqualTo(expected.body)
        assertThat(result.htmlDocument).isEqualTo(expected.htmlDocument)
        assertThat(result.plainText).isEqualTo(expected.plainText)
        assertThat(result.isEdited).isEqualTo(expected.isEdited)
        assertThat(result.formattedBody).isInstanceOf(Spanned::class.java)
        val spanned = result.formattedBody as Spanned
        assertThat(spanned.toString()).isEqualTo("https://www.example.org")
        val urlSpans = spanned.getSpans(0, spanned.length, URLSpan::class.java)
        assertThat(urlSpans).hasLength(1)
        assertThat(urlSpans[0].url).isEqualTo("https://www.example.org")
    }

    @Test
    fun `test create TextMessageType with HTML formatted body`() = runTest {
        val expected = buildSpannedString {
            append("link to ")
            inSpans(URLSpan("https://matrix.org")) {
                append("https://matrix.org")
            }
            append(" ")
            inSpans(URLSpan("https://matrix.org")) {
                append("and manually added link")
            }
        }.toSpannable()
        val sut = createTimelineItemContentMessageFactory(
            htmlConverterTransform = { expected }
        )
        val result = sut.create(
            content = createMessageContent(
                type = TextMessageType(
                    body = "body",
                    formatted = FormattedBody(MessageFormat.HTML, expected.toString())
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        assertThat((result as TimelineItemTextContent).formattedBody).isEqualTo(expected)
    }

    @Test
    fun `test create TextMessageType with unknown formatted body does nothing`() = runTest {
        val sut = createTimelineItemContentMessageFactory(
            htmlConverterTransform = { it }
        )
        val result = sut.create(
            content = createMessageContent(
                type = TextMessageType(
                    body = "body",
                    formatted = FormattedBody(MessageFormat.UNKNOWN, "formatted")
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        assertThat((result as TimelineItemTextContent).formattedBody).isNull()
    }

    @Test
    fun `test create VideoMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = VideoMessageType("filename", null, null, MediaSource("url"), null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVideoContent(
            filename = "filename",
            caption = null,
            formattedCaption = null,
            duration = Duration.ZERO,
            videoSource = MediaSource(url = "url", json = null),
            thumbnailSource = null,
            aspectRatio = null,
            blurHash = null,
            height = null,
            width = null,
            mimeType = MimeTypes.OctetStream,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VideoMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = VideoMessageType(
                    filename = "body.mp4",
                    caption = "body.mp4 caption",
                    formattedCaption = FormattedBody(MessageFormat.HTML, "formatted"),
                    source = MediaSource("url"),
                    info = VideoInfo(
                        duration = 1.minutes,
                        height = 100,
                        width = 300,
                        mimetype = MimeTypes.Mp4,
                        size = 555,
                        thumbnailInfo = ThumbnailInfo(
                            height = 10L,
                            width = 5L,
                            mimetype = MimeTypes.Jpeg,
                            size = 111L,
                        ),
                        thumbnailSource = MediaSource("url_thumbnail"),
                        blurhash = A_BLUR_HASH,
                    ),
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVideoContent(
            filename = "body.mp4",
            caption = "body.mp4 caption",
            formattedCaption = FormattedBody(MessageFormat.HTML, "formatted"),
            duration = 1.minutes,
            videoSource = MediaSource(url = "url", json = null),
            thumbnailSource = MediaSource("url_thumbnail"),
            aspectRatio = 3f,
            blurHash = A_BLUR_HASH,
            height = 100,
            width = 300,
            mimeType = MimeTypes.Mp4,
            formattedFileSize = "555 Bytes",
            fileExtension = "mp4",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create AudioMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = AudioMessageType("filename", null, null, MediaSource("url"), null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemAudioContent(
            filename = "filename",
            caption = null,
            formattedCaption = null,
            duration = Duration.ZERO,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.OctetStream,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create AudioMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = AudioMessageType(
                    filename = "body.mp3",
                    caption = null,
                    formattedCaption = null,
                    source = MediaSource("url"),
                    info = AudioInfo(
                        duration = 1.minutes,
                        size = 123L,
                        mimetype = MimeTypes.Mp3,
                    )
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemAudioContent(
            filename = "body.mp3",
            caption = null,
            formattedCaption = null,
            duration = 1.minutes,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.Mp3,
            formattedFileSize = "123 Bytes",
            fileExtension = "mp3",
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VoiceMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = VoiceMessageType("filename", null, null, MediaSource("url"), null, null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVoiceContent(
            filename = "filename",
            eventId = AN_EVENT_ID,
            caption = null,
            formattedCaption = null,
            duration = Duration.ZERO,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.OctetStream,
            waveform = emptyList<Float>().toImmutableList()
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VoiceMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = VoiceMessageType(
                    filename = "body.ogg",
                    caption = null,
                    formattedCaption = null,
                    source = MediaSource("url"),
                    info = AudioInfo(
                        duration = 1.minutes,
                        size = 123L,
                        mimetype = MimeTypes.Ogg,
                    ),
                    details = AudioDetails(
                        duration = 1.minutes,
                        waveform = persistentListOf(1f, 2f),
                    ),
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemVoiceContent(
            eventId = AN_EVENT_ID,
            filename = "body.ogg",
            caption = null,
            formattedCaption = null,
            duration = 1.minutes,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.Ogg,
            waveform = persistentListOf(1f, 2f)
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create VoiceMessageType feature disabled`() = runTest {
        val sut = createTimelineItemContentMessageFactory(
            featureFlagService = FakeFeatureFlagService(
                initialState = mapOf(
                    FeatureFlags.VoiceMessages.key to false,
                )
            )
        )
        val result = sut.create(
            content = createMessageContent(type = VoiceMessageType("filename", null, null, MediaSource("url"), null, null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemAudioContent(
            filename = "filename",
            caption = null,
            formattedCaption = null,
            duration = Duration.ZERO,
            mediaSource = MediaSource(url = "url", json = null),
            mimeType = MimeTypes.OctetStream,
            formattedFileSize = "0 Bytes",
            fileExtension = ""
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create ImageMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = ImageMessageType("filename", "body", null, MediaSource("url"), null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemImageContent(
            filename = "filename",
            caption = "body",
            formattedCaption = null,
            mediaSource = MediaSource(url = "url", json = null),
            thumbnailSource = null,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
            mimeType = MimeTypes.OctetStream,
            blurhash = null,
            width = null,
            height = null,
            aspectRatio = null
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create StickerMessageType`() = runTest {
        val sut = createTimelineItemContentStickerFactory()
        val result = sut.create(
            content = createStickerContent(
                filename = "filename",
                inImageInfo = ImageInfo(32, 32, "image/webp", 8192, null, MediaSource("thumbnail://url"), null),
                inUrl = "url"
            )
        )
        val expected = TimelineItemStickerContent(
            filename = "filename",
            caption = null,
            formattedCaption = null,
            mediaSource = MediaSource(url = "url", json = null),
            thumbnailSource = MediaSource(url = "thumbnail://url", json = null),
            formattedFileSize = "8192 Bytes",
            fileExtension = "",
            mimeType = MimeTypes.WebP,
            blurhash = null,
            width = 32,
            height = 32,
            aspectRatio = 1.0f
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create ImageMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = ImageMessageType(
                    filename = "body.jpg",
                    caption = "body.jpg caption",
                    formattedCaption = FormattedBody(MessageFormat.HTML, "formatted"),
                    source = MediaSource("url"),
                    info = ImageInfo(
                        height = 10L,
                        width = 5L,
                        mimetype = MimeTypes.Jpeg,
                        size = 888L,
                        thumbnailInfo = ThumbnailInfo(
                            height = 10L,
                            width = 5L,
                            mimetype = MimeTypes.Jpeg,
                            size = 111L,
                        ),
                        thumbnailSource = MediaSource("url_thumbnail"),
                        blurhash = A_BLUR_HASH,
                    )
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemImageContent(
            filename = "body.jpg",
            formattedCaption = FormattedBody(MessageFormat.HTML, "formatted"),
            caption = "body.jpg caption",
            mediaSource = MediaSource(url = "url", json = null),
            thumbnailSource = MediaSource("url_thumbnail"),
            formattedFileSize = "888 Bytes",
            fileExtension = "jpg",
            mimeType = MimeTypes.Jpeg,
            blurhash = A_BLUR_HASH,
            width = 5,
            height = 10,
            aspectRatio = 0.5f,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create FileMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = FileMessageType("filename", null, null, MediaSource("url"), null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemFileContent(
            filename = "filename",
            caption = null,
            formattedCaption = null,
            fileSource = MediaSource(url = "url", json = null),
            thumbnailSource = null,
            formattedFileSize = "0 Bytes",
            fileExtension = "",
            mimeType = MimeTypes.OctetStream
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create FileMessageType with info`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = FileMessageType(
                    filename = "body.pdf",
                    caption = null,
                    formattedCaption = null,
                    source = MediaSource("url"),
                    info = FileInfo(
                        mimetype = MimeTypes.Pdf,
                        size = 123L,
                        thumbnailInfo = ThumbnailInfo(
                            height = 10L,
                            width = 5L,
                            mimetype = MimeTypes.Jpeg,
                            size = 111L,
                        ),
                        thumbnailSource = MediaSource("url_thumbnail"),
                    )
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemFileContent(
            filename = "body.pdf",
            caption = null,
            formattedCaption = null,
            fileSource = MediaSource(url = "url", json = null),
            thumbnailSource = MediaSource("url_thumbnail"),
            formattedFileSize = "123 Bytes",
            fileExtension = "pdf",
            mimeType = MimeTypes.Pdf
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create NoticeMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = NoticeMessageType("body", null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemNoticeContent(
            body = "body",
            htmlDocument = null,
            plainText = "body",
            formattedBody = null,
            isEdited = false,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create NoticeMessageType with HTML formatted body`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = NoticeMessageType(
                    body = "body",
                    formatted = FormattedBody(MessageFormat.HTML, "formatted")
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        (result as TimelineItemNoticeContent).formattedBody.assertSpannedEquals(SpannedString("formatted"))
    }

    @Test
    fun `test create EmoteMessageType`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(type = EmoteMessageType("body", null)),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        val expected = TimelineItemEmoteContent(
            body = "* Bob body",
            htmlDocument = null,
            plainText = "* Bob body",
            formattedBody = null,
            isEdited = false,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `test create EmoteMessageType with HTML formatted body`() = runTest {
        val sut = createTimelineItemContentMessageFactory()
        val result = sut.create(
            content = createMessageContent(
                type = EmoteMessageType(
                    body = "body",
                    formatted = FormattedBody(MessageFormat.HTML, "formatted")
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )

        (result as TimelineItemEmoteContent).formattedBody.assertSpannedEquals(SpannableString("* Bob formatted"))
    }

    @Test
    fun `a message with existing URLSpans keeps it after linkification`() = runTest {
        val expectedSpanned = SpannableStringBuilder().apply {
            append("Test ")
            inSpans(URLSpan("https://www.example.org")) {
                append("me@matrix.org")
            }
        }.toSpannable()
        val sut = createTimelineItemContentMessageFactory(
            htmlConverterTransform = { expectedSpanned },
            permalinkParser = FakePermalinkParser { PermalinkData.FallbackLink(Uri.EMPTY) }
        )
        val result = sut.create(
            content = createMessageContent(
                type = TextMessageType(
                    body = "Test [me@matrix.org](https://www.example.org)",
                    formatted = FormattedBody(MessageFormat.HTML, "Test <a href=\"https://www.example.org\">me@matrix.org</a>")
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        (result as TimelineItemTextContent).formattedBody.assertSpannedEquals(expectedSpanned)
    }

    @Test
    fun `a message with plain URL in a formatted body Spanned format gets linkified too`() = runTest {
        val expectedSpanned = buildSpannedString {
            append("Test ")
            inSpansWithFlags(URLSpan("https://www.example.org"), flags = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                append("https://www.example.org")
            }
        }
        val sut = createTimelineItemContentMessageFactory(
            htmlConverterTransform = { expectedSpanned },
            permalinkParser = FakePermalinkParser { PermalinkData.FallbackLink(Uri.EMPTY) }
        )
        val result = sut.create(
            content = createMessageContent(
                type = TextMessageType(
                    body = "Test [me@matrix.org](https://www.example.org)",
                    formatted = FormattedBody(MessageFormat.HTML, "Test https://www.example.org")
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )
        (result as TimelineItemTextContent).formattedBody.assertSpannedEquals(expectedSpanned)
    }

    @Test
    fun `a message with plain URL in a formatted body with plain text format gets linkified too`() = runTest {
        val resultString = "Test https://www.example.org"
        val expectedSpanned = buildSpannedString {
            append("Test ")
            inSpansWithFlags(URLSpan("https://www.example.org"), flags = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) {
                append("https://www.example.org")
            }
        }.toSpannable()
        val sut = createTimelineItemContentMessageFactory(
            htmlConverterTransform = { resultString },
            permalinkParser = FakePermalinkParser { PermalinkData.FallbackLink(Uri.EMPTY) }
        )
        val result = sut.create(
            content = createMessageContent(
                type = TextMessageType(
                    body = "Test [me@matrix.org](https://www.example.org)",
                    formatted = FormattedBody(MessageFormat.HTML, "Test https://www.example.org")
                )
            ),
            senderDisambiguatedDisplayName = "Bob",
            eventId = AN_EVENT_ID,
        )

        (result as TimelineItemTextContent).formattedBody.assertSpannedEquals(expectedSpanned)
    }

    private fun createMessageContent(
        body: String = "Body",
        inReplyTo: InReplyTo? = null,
        isEdited: Boolean = false,
        isThreaded: Boolean = false,
        type: MessageType,
    ): MessageContent {
        return MessageContent(
            body = body,
            inReplyTo = inReplyTo,
            isEdited = isEdited,
            isThreaded = isThreaded,
            type = type,
        )
    }

    private fun createTimelineItemContentMessageFactory(
        featureFlagService: FeatureFlagService = FakeFeatureFlagService(),
        htmlConverterTransform: (String) -> CharSequence = { it },
        permalinkParser: FakePermalinkParser = FakePermalinkParser(),
    ) = TimelineItemContentMessageFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
        featureFlagService = featureFlagService,
        htmlConverterProvider = FakeHtmlConverterProvider(htmlConverterTransform),
        permalinkParser = permalinkParser,
        textPillificationHelper = FakeTextPillificationHelper(),
    )

    private fun createStickerContent(
        filename: String = "filename",
        inImageInfo: ImageInfo,
        inUrl: String,
        body: String? = null,
    ): StickerContent {
        return aStickerContent(
            filename = filename,
            body = body,
            info = inImageInfo,
            mediaSource = aMediaSource(url = inUrl),
        )
    }

    private fun createTimelineItemContentStickerFactory() = TimelineItemContentStickerFactory(
        fileSizeFormatter = FakeFileSizeFormatter(),
        fileExtensionExtractor = FileExtensionExtractorWithoutValidation()
    )
}

private inline fun SpannableStringBuilder.inSpansWithFlags(span: Any, flags: Int, action: SpannableStringBuilder.() -> Unit) {
    val start = this.length
    action()
    val end = this.length
    setSpan(span, start, end, flags)
}

fun CharSequence?.assertSpannedEquals(other: CharSequence?) {
    if (this == null && other == null) {
        return
    } else if (this is Spanned && other is Spanned) {
        assertThat(this.toString()).isEqualTo(other.toString())
        assertThat(this.length).isEqualTo(other.length)
        val thisSpans = this.getSpans(0, this.length, Any::class.java)
        val otherSpans = other.getSpans(0, other.length, Any::class.java)
        if (thisSpans.size != otherSpans.size) {
            fail("Expected ${thisSpans.size} spans, got ${otherSpans.size}")
        }
        thisSpans.forEachIndexed { index, span ->
            val otherSpan = otherSpans[index]
            // URLSpans don't have a proper `equals` implementation, so we compare the URL instead
            if (span is URLSpan && otherSpan is URLSpan) {
                assertThat(span.url).isEqualTo(otherSpan.url)
            } else {
                assertThat(span).isEqualTo(otherSpan)
            }
            assertThat(this.getSpanStart(span)).isEqualTo(other.getSpanStart(otherSpan))
            assertThat(this.getSpanEnd(span)).isEqualTo(other.getSpanEnd(otherSpan))
            assertThat(this.getSpanFlags(span)).isEqualTo(other.getSpanFlags(otherSpan))
        }
    } else {
        val thisString = this?.toString() ?: "null"
        val otherString = other?.toString() ?: "null"
        fail("Expected Spanned, got $thisString and $otherString")
    }
}
