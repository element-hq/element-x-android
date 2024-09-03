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

package io.element.android.features.messages.impl.voicemessages.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UniqueId
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileTimelineDetails
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RedactedVoiceMessageManagerTest {
    @Test
    fun `redacted event - no playing related media`() = runTest {
        val mediaPlayer = FakeMediaPlayer().apply {
            setMedia(uri = "someUri", mediaId = AN_EVENT_ID.value, mimeType = MimeTypes.Ogg)
            play()
        }
        val manager = aDefaultRedactedVoiceMessageManager(mediaPlayer = mediaPlayer)

        assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        assertThat(mediaPlayer.state.value.isPlaying).isTrue()

        manager.onEachMatrixTimelineItem(aRedactedMatrixTimeline(AN_EVENT_ID_2))

        assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        assertThat(mediaPlayer.state.value.isPlaying).isTrue()
    }

    @Test
    fun `redacted event - playing related media is paused`() = runTest {
        val mediaPlayer = FakeMediaPlayer().apply {
            setMedia(uri = "someUri", mediaId = AN_EVENT_ID.value, mimeType = MimeTypes.Ogg)
            play()
        }
        val manager = aDefaultRedactedVoiceMessageManager(mediaPlayer = mediaPlayer)

        assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        assertThat(mediaPlayer.state.value.isPlaying).isTrue()

        manager.onEachMatrixTimelineItem(aRedactedMatrixTimeline(AN_EVENT_ID))

        assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        assertThat(mediaPlayer.state.value.isPlaying).isFalse()
    }
}

fun TestScope.aDefaultRedactedVoiceMessageManager(
    mediaPlayer: MediaPlayer = FakeMediaPlayer(),
) = DefaultRedactedVoiceMessageManager(
    dispatchers = this.testCoroutineDispatchers(true),
    mediaPlayer = mediaPlayer,
)

fun aRedactedMatrixTimeline(eventId: EventId) = listOf<MatrixTimelineItem>(
    MatrixTimelineItem.Event(
        uniqueId = UniqueId("0"),
        event = EventTimelineItem(
            eventId = eventId,
            transactionId = null,
            isEditable = false,
            canBeRepliedTo = false,
            isLocal = false,
            isOwn = false,
            isRemote = false,
            localSendState = null,
            reactions = persistentListOf(),
            receipts = persistentListOf(),
            sender = A_USER_ID,
            senderProfile = ProfileTimelineDetails.Unavailable,
            timestamp = 9442,
            content = RedactedContent,
            debugInfo = TimelineItemDebugInfo(
                model = "enim",
                originalJson = null,
                latestEditedJson = null
            ),
            origin = null,
            messageShield = null,
        ),
    )
)
