/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
            isOwn = false,
            isRemote = false,
            localSendState = null,
            reactions = persistentListOf(),
            receipts = persistentListOf(),
            sender = A_USER_ID,
            senderProfile = ProfileTimelineDetails.Unavailable,
            timestamp = 9442,
            content = RedactedContent,
            origin = null,
            timelineItemDebugInfoProvider = {
                TimelineItemDebugInfo(
                    model = "enim",
                    originalJson = null,
                    latestEditedJson = null,
                )
            },
            messageShieldProvider = { null },
        ),
    )
)
