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

package io.element.android.features.messages.timeline.factories.event

import com.google.common.truth.Truth
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentRedactedFactory
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TimelineItemContentRedactedFactoryTest {
    @Test
    fun `redacted event - no playing related media`() = runTest {
        val mediaPlayer = FakeMediaPlayer().apply {
            setMedia(uri = "someUri", mediaId = AN_EVENT_ID.value, mimeType = "audio/ogg")
            play()
        }
        val factory = aTimelineItemContentRedactedFactory(mediaPlayer = mediaPlayer)

        Truth.assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        Truth.assertThat(mediaPlayer.state.value.isPlaying).isTrue()

        factory.create(RedactedContent, AN_EVENT_ID_2).let { timelineItemEventContent ->
            Truth.assertThat(timelineItemEventContent).isEqualTo(TimelineItemRedactedContent)
        }

        Truth.assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        Truth.assertThat(mediaPlayer.state.value.isPlaying).isTrue()
    }

    @Test
    fun `redacted event - playing related media is paused`() = runTest {
        val mediaPlayer = FakeMediaPlayer().apply {
            setMedia(uri = "someUri", mediaId = AN_EVENT_ID.value, mimeType = "audio/ogg")
            play()
        }
        val factory = aTimelineItemContentRedactedFactory(mediaPlayer = mediaPlayer)

        Truth.assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        Truth.assertThat(mediaPlayer.state.value.isPlaying).isTrue()

        factory.create(RedactedContent, AN_EVENT_ID).let { timelineItemEventContent ->
            Truth.assertThat(timelineItemEventContent).isEqualTo(TimelineItemRedactedContent)
        }

        Truth.assertThat(mediaPlayer.state.value.mediaId).isEqualTo(AN_EVENT_ID.value)
        Truth.assertThat(mediaPlayer.state.value.isPlaying).isFalse()
    }
}

fun TestScope.aTimelineItemContentRedactedFactory(
    mediaPlayer: MediaPlayer = FakeMediaPlayer(),
) = TimelineItemContentRedactedFactory(
    scope = this,
    dispatchers = this.testCoroutineDispatchers(true),
    mediaPlayer = mediaPlayer,
)
