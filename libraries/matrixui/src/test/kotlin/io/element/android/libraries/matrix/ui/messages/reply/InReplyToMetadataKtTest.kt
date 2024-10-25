/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages.reply

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseMessageLikeContent
import io.element.android.libraries.matrix.api.timeline.item.event.FailedToParseStateContent
import io.element.android.libraries.matrix.api.timeline.item.event.FileMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ImageMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.LocationMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileChangeContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.StickerContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.aPollContent
import io.element.android.libraries.matrix.test.timeline.aProfileTimelineDetails
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class InReplyToMetadataKtTest {
    @Test
    fun `any message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(eventContent = aMessageContent()).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(InReplyToMetadata.Text("textContent"))
            }
        }
    }

    @Test
    fun `an image message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aMessageContent(
                    messageType = ImageMessageType(
                        filename = "filename",
                        caption = null,
                        formattedCaption = null,
                        source = aMediaSource(),
                        info = anImageInfo(),
                    )
                )
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = aMediaSource(),
                            textContent = "body",
                            type = AttachmentThumbnailType.Image,
                            blurHash = A_BLUR_HASH,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `an image message content, no thumbnail`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aMessageContent(
                    messageType = ImageMessageType(
                        filename = "filename",
                        caption = "caption",
                        formattedCaption = null,
                        source = aMediaSource(),
                        info = anImageInfo(),
                    )
                )
            ).metadata(hideImage = true)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = null,
                            textContent = "body",
                            type = AttachmentThumbnailType.Image,
                            blurHash = A_BLUR_HASH,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a sticker message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = StickerContent(
                    filename = "filename",
                    body = "body",
                    info = anImageInfo(),
                    source = aMediaSource(url = "url")
                )
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = aMediaSource(url = "url"),
                            textContent = "body",
                            type = AttachmentThumbnailType.Image,
                            blurHash = A_BLUR_HASH,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a sticker message content, no thumbnail`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = StickerContent(
                    filename = "filename",
                    body = "body",
                    info = anImageInfo(),
                    source = aMediaSource(url = "url")
                )
            ).metadata(hideImage = true)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = null,
                            textContent = "body",
                            type = AttachmentThumbnailType.Image,
                            blurHash = A_BLUR_HASH,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a video message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aMessageContent(
                    messageType = VideoMessageType(
                        filename = "filename",
                        caption = null,
                        formattedCaption = null,
                        source = aMediaSource(),
                        info = aVideoInfo(),
                    )
                )
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = aMediaSource(),
                            textContent = "body",
                            type = AttachmentThumbnailType.Video,
                            blurHash = A_BLUR_HASH,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a video message content, no thumbnail`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aMessageContent(
                    messageType = VideoMessageType(
                        filename = "filename",
                        caption = "caption",
                        formattedCaption = null,
                        source = aMediaSource(),
                        info = aVideoInfo(),
                    )
                )
            ).metadata(hideImage = true)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = null,
                            textContent = "body",
                            type = AttachmentThumbnailType.Video,
                            blurHash = A_BLUR_HASH,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a file message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aMessageContent(
                    messageType = FileMessageType(
                        filename = "filename",
                        caption = "caption",
                        formattedCaption = null,
                        source = aMediaSource(),
                        info = FileInfo(
                            mimetype = null,
                            size = null,
                            thumbnailInfo = null,
                            thumbnailSource = aMediaSource(),
                        ),
                    )
                )
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = aMediaSource(),
                            textContent = "body",
                            type = AttachmentThumbnailType.File,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a file message content, no thumbnail`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aMessageContent(
                    messageType = FileMessageType(
                        filename = "filename",
                        caption = "caption",
                        formattedCaption = null,
                        source = aMediaSource(),
                        info = FileInfo(
                            mimetype = null,
                            size = null,
                            thumbnailInfo = null,
                            thumbnailSource = aMediaSource(),
                        ),
                    )
                )
            ).metadata(hideImage = true)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = null,
                            textContent = "body",
                            type = AttachmentThumbnailType.File,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a audio message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aMessageContent(
                    messageType = AudioMessageType(
                        filename = "filename",
                        caption = "caption",
                        formattedCaption = null,
                        source = aMediaSource(),
                        info = AudioInfo(
                            duration = null,
                            size = null,
                            mimetype = null
                        ),
                    )
                )
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            textContent = "body",
                            type = AttachmentThumbnailType.Audio,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a location message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            testEnv {
                anInReplyToDetailsReady(
                    eventContent = aMessageContent(
                        messageType = LocationMessageType(
                            body = "body",
                            geoUri = "geo:3.0,4.0;u=5.0",
                            description = null,
                        )
                    )
                ).metadata(hideImage = false)
            }
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = null,
                            textContent = "Shared location",
                            type = AttachmentThumbnailType.Location,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a voice message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            testEnv {
                anInReplyToDetailsReady(
                    eventContent = aMessageContent(
                        messageType = VoiceMessageType(
                            filename = "filename",
                            caption = "caption",
                            formattedCaption = null,
                            source = aMediaSource(),
                            info = null,
                            details = null,
                        )
                    )
                ).metadata(hideImage = false)
            }
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = null,
                            textContent = "Voice message",
                            type = AttachmentThumbnailType.Voice,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a poll content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = aPollContent()
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = null,
                            textContent = "Do you like polls?",
                            type = AttachmentThumbnailType.Poll,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `redacted content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = RedactedContent
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(InReplyToMetadata.Redacted)
            }
        }
    }

    @Test
    fun `unable to decrypt content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = UnableToDecryptContent(UnableToDecryptContent.Data.Unknown)
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(InReplyToMetadata.UnableToDecrypt)
            }
        }
    }

    @Test
    fun `failed to parse message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = FailedToParseMessageLikeContent("", "")
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `failed to parse state content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = FailedToParseStateContent("", "", "")
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `profile change content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = ProfileChangeContent("", "", "", "")
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `room membership content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = RoomMembershipContent(A_USER_ID, null, null)
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `state content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = StateContent("", OtherState.RoomJoinRules)
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `unknown content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = UnknownContent
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `null content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetailsReady(
                eventContent = null
            ).metadata(hideImage = false)
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }
}

private fun anInReplyToDetailsReady(
    eventId: EventId = AN_EVENT_ID,
    senderId: UserId = A_USER_ID,
    senderProfile: ProfileTimelineDetails = aProfileTimelineDetails(),
    eventContent: EventContent? = aMessageContent(),
    textContent: String? = "textContent",
) = InReplyToDetails.Ready(
    eventId = eventId,
    senderId = senderId,
    senderProfile = senderProfile,
    eventContent = eventContent,
    textContent = textContent,
)

fun aVideoInfo(): VideoInfo {
    return VideoInfo(
        duration = 1.minutes,
        height = 100,
        width = 100,
        mimetype = "video/mp4",
        size = 1000,
        thumbnailInfo = null,
        thumbnailSource = aMediaSource(),
        blurhash = A_BLUR_HASH,
    )
}

fun anImageInfo(): ImageInfo {
    return ImageInfo(
        height = 100,
        width = 100,
        mimetype = "image/jpeg",
        size = 1000,
        thumbnailInfo = null,
        thumbnailSource = aMediaSource(),
        blurhash = A_BLUR_HASH,
    )
}

@Composable
private fun testEnv(content: @Composable () -> Any?): Any? {
    var result: Any? = null
    CompositionLocalProvider(
        LocalConfiguration provides Configuration(),
        LocalContext provides ApplicationProvider.getApplicationContext(),
    ) {
        content().apply {
            result = this
        }
    }
    return result
}
