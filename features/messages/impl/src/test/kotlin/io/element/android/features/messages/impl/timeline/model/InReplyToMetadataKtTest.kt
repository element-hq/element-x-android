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
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RoomMembershipContent
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnableToDecryptContent
import io.element.android.libraries.matrix.api.timeline.item.event.UnknownContent
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.VoiceMessageType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.test.timeline.aMessageContent
import io.element.android.libraries.matrix.test.timeline.aPollContent
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InReplyToMetadataKtTest {
    @Test
    fun `any message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(eventContent = aMessageContent()).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(InReplyToMetadata.Text("textContent"))
            }
        }
    }

    @Test
    fun `an image message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = aMessageContent(
                    messageType = ImageMessageType(
                        body = "body",
                        source = aMediaSource(),
                        info = null,
                    )
                )
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = aMediaSource(),
                            textContent = "body",
                            type = AttachmentThumbnailType.Image,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a video message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = aMessageContent(
                    messageType = VideoMessageType(
                        body = "body",
                        source = aMediaSource(),
                        info = VideoInfo(
                            duration = null,
                            height = null,
                            width = null,
                            mimetype = null,
                            size = null,
                            thumbnailInfo = null,
                            thumbnailSource = aMediaSource(),
                            blurhash = null
                        ),
                    )
                )
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(
                    InReplyToMetadata.Thumbnail(
                        attachmentThumbnailInfo = AttachmentThumbnailInfo(
                            thumbnailSource = aMediaSource(),
                            textContent = "body",
                            type = AttachmentThumbnailType.Video,
                            blurHash = null,
                        )
                    )
                )
            }
        }
    }

    @Test
    fun `a file message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = aMessageContent(
                    messageType = FileMessageType(
                        body = "body",
                        source = aMediaSource(),
                        info = FileInfo(
                            mimetype = null,
                            size = null,
                            thumbnailInfo = null,
                            thumbnailSource = aMediaSource(),
                        ),
                    )
                )
            ).metadata()
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
    fun `a audio message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = aMessageContent(
                    messageType = AudioMessageType(
                        body = "body",
                        source = aMediaSource(),
                        info = AudioInfo(
                            duration = null,
                            size = null,
                            mimetype = null
                        ),
                    )
                )
            ).metadata()
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
                anInReplyToDetails(
                    eventContent = aMessageContent(
                        messageType = LocationMessageType(
                            body = "body",
                            geoUri = "geo:3.0,4.0;u=5.0",
                            description = null,
                        )
                    )
                ).metadata()
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
                anInReplyToDetails(
                    eventContent = aMessageContent(
                        messageType = VoiceMessageType(
                            body = "body",
                            source = aMediaSource(),
                            info = null,
                            details = null,
                        )
                    )
                ).metadata()
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
            anInReplyToDetails(
                eventContent = aPollContent()
            ).metadata()
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
            anInReplyToDetails(
                eventContent = RedactedContent
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(InReplyToMetadata.Redacted)
            }
        }
    }

    @Test
    fun `unable to decrypt content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = UnableToDecryptContent(UnableToDecryptContent.Data.Unknown)
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isEqualTo(InReplyToMetadata.UnableToDecrypt)
            }
        }
    }

    @Test
    fun `failed to parse message content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = FailedToParseMessageLikeContent("", "")
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `failed to parse state content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = FailedToParseStateContent("", "", "")
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `profile change content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = ProfileChangeContent("", "", "", "")
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `room membership content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = RoomMembershipContent(A_USER_ID, null)
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `state content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = StateContent("", OtherState.RoomJoinRules)
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `unknown content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = UnknownContent
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }

    @Test
    fun `null content`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            anInReplyToDetails(
                eventContent = null
            ).metadata()
        }.test {
            awaitItem().let {
                assertThat(it).isNull()
            }
        }
    }
}

fun anInReplyToDetails(
    eventId: EventId = AN_EVENT_ID,
    senderId: UserId = A_USER_ID,
    senderDisplayName: String? = "senderDisplayName",
    senderAvatarUrl: String? = "senderAvatarUrl",
    eventContent: EventContent? = aMessageContent(),
    textContent: String? = "textContent",
) = InReplyToDetails(
    eventId = eventId,
    senderId = senderId,
    senderDisplayName = senderDisplayName,
    senderAvatarUrl = senderAvatarUrl,
    eventContent = eventContent,
    textContent = textContent,
)

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
