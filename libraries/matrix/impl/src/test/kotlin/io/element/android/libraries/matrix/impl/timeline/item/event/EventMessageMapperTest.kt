/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline.item.event

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EmoteMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.GalleryItemType
import io.element.android.libraries.matrix.api.timeline.item.event.GalleryMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.NoticeMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustAudioMessageContent
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustFileMessageContent
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustGalleryItemTypeAudio
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustGalleryItemTypeFile
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustGalleryItemTypeImage
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustGalleryItemTypeOther
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustGalleryItemTypeVideo
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustGalleryMessageContent
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustImageMessageContent
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustMessageTypeGallery
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustVideoMessageContent
import org.junit.Test
import org.matrix.rustcomponents.sdk.EmoteMessageContent
import org.matrix.rustcomponents.sdk.LocationContent
import org.matrix.rustcomponents.sdk.MessageType
import org.matrix.rustcomponents.sdk.NoticeMessageContent
import org.matrix.rustcomponents.sdk.TextMessageContent
import org.matrix.rustcomponents.sdk.UnstableVoiceContent
import org.matrix.rustcomponents.sdk.AssetType as RustAssetType

class EventMessageMapperTest {
    private val sut = EventMessageMapper()

    @Test
    fun `mapMessageType with Text returns TextMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Text(
                content = TextMessageContent(body = "Hello", formatted = null)
            )
        )
        assertThat(result).isEqualTo(TextMessageType(body = "Hello", formatted = null))
    }

    @Test
    fun `mapMessageType with Notice returns NoticeMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Notice(content = NoticeMessageContent(body = "A notice", formatted = null))
        )
        assertThat(result).isEqualTo(NoticeMessageType(body = "A notice", formatted = null))
    }

    @Test
    fun `mapMessageType with Emote returns EmoteMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Emote(content = EmoteMessageContent(body = "An emote", formatted = null))
        )
        assertThat(result).isEqualTo(EmoteMessageType(body = "An emote", formatted = null))
    }

    @Test
    fun `mapMessageType with Other returns OtherMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Other(msgtype = "m.custom", body = "custom body")
        )
        assertThat(result).isEqualTo(OtherMessageType(msgType = "m.custom", body = "custom body"))
    }

    @Test
    fun `mapMessageType with Location returns LocationMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Location(
                content = LocationContent(
                    body = "Location body",
                    geoUri = "geo:51.5,-0.1",
                    description = "London",
                    zoomLevel = null,
                    asset = RustAssetType.PIN,
                )
            )
        )
        assertThat(result).isEqualTo(
            LocationMessageType(
                body = "Location body",
                geoUri = "geo:51.5,-0.1",
                description = "London",
                assetType = AssetType.PIN,
            )
        )
    }

    @Test
    fun `mapMessageType with Audio without voice returns AudioMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Audio(content = aRustAudioMessageContent(filename = "audio.mp3", voice = null))
        )
        assertThat(result).isEqualTo(
            AudioMessageType(
                filename = "audio.mp3",
                caption = null,
                formattedCaption = null,
                source = MediaSource(url = "mxc://server/audio", json = """{"url":"mxc://server/audio"}"""),
                info = null,
            )
        )
    }

    @Test
    fun `mapMessageType with Audio with voice returns VoiceMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Audio(content = aRustAudioMessageContent(filename = "voice.ogg", voice = UnstableVoiceContent()))
        )
        assertThat(result).isEqualTo(
            VoiceMessageType(
                filename = "voice.ogg",
                caption = null,
                formattedCaption = null,
                source = MediaSource(url = "mxc://server/audio", json = """{"url":"mxc://server/audio"}"""),
                info = null,
                details = null,
            )
        )
    }

    @Test
    fun `mapMessageType with File returns FileMessageType`() {
        val result = sut.mapMessageType(
            MessageType.File(content = aRustFileMessageContent(filename = "document.pdf"))
        )
        assertThat(result).isEqualTo(
            FileMessageType(
                filename = "document.pdf",
                caption = null,
                formattedCaption = null,
                source = MediaSource(url = "mxc://server/file", json = """{"url":"mxc://server/file"}"""),
                info = null,
            )
        )
    }

    @Test
    fun `mapMessageType with Image returns ImageMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Image(content = aRustImageMessageContent(filename = "image.jpg"))
        )
        assertThat(result).isEqualTo(
            ImageMessageType(
                filename = "image.jpg",
                caption = null,
                formattedCaption = null,
                source = MediaSource(url = "mxc://server/image", json = """{"url":"mxc://server/image"}"""),
                info = null,
            )
        )
    }

    @Test
    fun `mapMessageType with Video returns VideoMessageType`() {
        val result = sut.mapMessageType(
            MessageType.Video(content = aRustVideoMessageContent(filename = "video.mp4"))
        )
        assertThat(result).isEqualTo(
            VideoMessageType(
                filename = "video.mp4",
                caption = null,
                formattedCaption = null,
                source = MediaSource(url = "mxc://server/video", json = """{"url":"mxc://server/video"}"""),
                info = null,
            )
        )
    }

    @Test
    fun `mapMessageType with Gallery with Image item returns GalleryMessageType with Image`() {
        val result = sut.mapMessageType(
            aRustMessageTypeGallery(
                content = aRustGalleryMessageContent(
                    body = "A gallery",
                    itemTypes = listOf(aRustGalleryItemTypeImage(aRustImageMessageContent(filename = "image.jpg"))),
                )
            )
        )
        assertThat(result).isEqualTo(
            GalleryMessageType(
                body = "A gallery",
                formatted = null,
                items = listOf(
                    GalleryItemType.Image(
                        content = ImageMessageType(
                            filename = "image.jpg",
                            caption = null,
                            formattedCaption = null,
                            source = MediaSource(url = "mxc://server/image", json = """{"url":"mxc://server/image"}"""),
                            info = null,
                        )
                    )
                ),
            )
        )
    }

    @Test
    fun `mapMessageType with Gallery with Audio item returns GalleryMessageType with Audio`() {
        val result = sut.mapMessageType(
            aRustMessageTypeGallery(
                content = aRustGalleryMessageContent(
                    itemTypes = listOf(aRustGalleryItemTypeAudio(aRustAudioMessageContent(filename = "audio.mp3")))
                )
            )
        )
        assertThat(result).isEqualTo(
            GalleryMessageType(
                body = "A gallery",
                formatted = null,
                items = listOf(
                    GalleryItemType.Audio(
                        content = AudioMessageType(
                            filename = "audio.mp3",
                            caption = null,
                            formattedCaption = null,
                            source = MediaSource(url = "mxc://server/audio", json = """{"url":"mxc://server/audio"}"""),
                            info = null,
                        )
                    )
                ),
            )
        )
    }

    @Test
    fun `mapMessageType with Gallery with Video item returns GalleryMessageType with Video`() {
        val result = sut.mapMessageType(
            aRustMessageTypeGallery(
                content = aRustGalleryMessageContent(
                    itemTypes = listOf(aRustGalleryItemTypeVideo(aRustVideoMessageContent(filename = "video.mp4")))
                )
            )
        )
        assertThat(result).isEqualTo(
            GalleryMessageType(
                body = "A gallery",
                formatted = null,
                items = listOf(
                    GalleryItemType.Video(
                        content = VideoMessageType(
                            filename = "video.mp4",
                            caption = null,
                            formattedCaption = null,
                            source = MediaSource(url = "mxc://server/video", json = """{"url":"mxc://server/video"}"""),
                            info = null,
                        )
                    )
                ),
            )
        )
    }

    @Test
    fun `mapMessageType with Gallery with File item returns GalleryMessageType with File`() {
        val result = sut.mapMessageType(
            aRustMessageTypeGallery(
                content = aRustGalleryMessageContent(
                    itemTypes = listOf(aRustGalleryItemTypeFile(aRustFileMessageContent(filename = "document.pdf")))
                )
            )
        )
        assertThat(result).isEqualTo(
            GalleryMessageType(
                body = "A gallery",
                formatted = null,
                items = listOf(
                    GalleryItemType.File(
                        content = FileMessageType(
                            filename = "document.pdf",
                            caption = null,
                            formattedCaption = null,
                            source = MediaSource(url = "mxc://server/file", json = """{"url":"mxc://server/file"}"""),
                            info = null,
                        )
                    )
                ),
            )
        )
    }

    @Test
    fun `mapMessageType with Gallery with Other item returns GalleryMessageType with Other`() {
        val result = sut.mapMessageType(
            aRustMessageTypeGallery(
                content = aRustGalleryMessageContent(
                    itemTypes = listOf(aRustGalleryItemTypeOther(itemType = "m.custom", body = "custom item"))
                )
            )
        )
        assertThat(result).isEqualTo(
            GalleryMessageType(
                body = "A gallery",
                formatted = null,
                items = listOf(
                    GalleryItemType.Other(itemType = "m.custom", body = "custom item")
                ),
            )
        )
    }
}
