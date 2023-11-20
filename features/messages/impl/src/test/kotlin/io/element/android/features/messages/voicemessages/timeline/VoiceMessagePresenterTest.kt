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

package io.element.android.features.messages.voicemessages.timeline

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.features.messages.impl.voicemessages.VoiceMessageException
import io.element.android.features.messages.impl.voicemessages.timeline.DefaultVoiceMessagePlayer
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageEvents
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageMediaRepo
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessagePresenter
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageState
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class VoiceMessagePresenterTest {
    @Test
    fun `initial state has proper default values`() = runTest {
        val presenter = createVoiceMessagePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }
        }
    }

    @Test
    fun `pressing play downloads and plays`() = runTest {
        val presenter = createVoiceMessagePresenter(
            mediaPlayer = FakeMediaPlayer(fakeTotalDurationMs = 2_000),
            content = aTimelineItemVoiceContent(durationMs = 2_000),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:02")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:02")
            }
            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:00")
            }
            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }
        }
    }

    @Test
    fun `pressing play downloads and fails`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val presenter = createVoiceMessagePresenter(
            mediaPlayer = FakeMediaPlayer(fakeTotalDurationMs = 2_000),
            voiceMessageMediaRepo = FakeVoiceMessageMediaRepo().apply { shouldFail = true },
            analyticsService = analyticsService,
            content = aTimelineItemVoiceContent(durationMs = 2_000),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:02")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:02")
            }
            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Retry)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:02")
            }
            analyticsService.trackedErrors.first().also {
                Truth.assertThat(it).apply {
                    isInstanceOf(VoiceMessageException.PlayMessageError::class.java)
                    hasMessageThat().isEqualTo("Error while trying to play voice message")
                }
            }
        }
    }

    @Test
    fun `pressing pause while playing pauses`() = runTest {
        val presenter = createVoiceMessagePresenter(
            mediaPlayer = FakeMediaPlayer(fakeTotalDurationMs = 2_000),
            content = aTimelineItemVoiceContent(durationMs = 2_000),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:02")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)
            skipItems(2) // skip downloading states

            val playingState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }

            playingState.eventSink(VoiceMessageEvents.PlayPause)
            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }
        }
    }

    @Test
    fun `content with null eventId shows disabled button`() = runTest {
        val presenter = createVoiceMessagePresenter(
            content = aTimelineItemVoiceContent(eventId = null),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Disabled)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }
        }
    }

    @Test
    fun `seeking before play`() = runTest {
        val presenter = createVoiceMessagePresenter(
            mediaPlayer = FakeMediaPlayer(fakeTotalDurationMs = 2_000),
            content = aTimelineItemVoiceContent(durationMs = 10_000),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:10")
            }

            initialState.eventSink(VoiceMessageEvents.Seek(0.5f))

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:05")
            }
        }
    }

    @Test
    fun `seeking after play`() = runTest {
        val presenter = createVoiceMessagePresenter(
            content = aTimelineItemVoiceContent(durationMs = 10_000),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:10")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            skipItems(2) // skip downloading states

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(0.1f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }

            initialState.eventSink(VoiceMessageEvents.Seek(0.5f))

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:05")
            }
        }
    }
}

fun TestScope.createVoiceMessagePresenter(
    mediaPlayer: FakeMediaPlayer = FakeMediaPlayer(),
    voiceMessageMediaRepo: VoiceMessageMediaRepo = FakeVoiceMessageMediaRepo(),
    analyticsService: AnalyticsService = FakeAnalyticsService(),
    content: TimelineItemVoiceContent = aTimelineItemVoiceContent(),
) = VoiceMessagePresenter(
    voiceMessagePlayerFactory = { eventId, mediaSource, mimeType, body ->
        DefaultVoiceMessagePlayer(
            mediaPlayer = mediaPlayer,
            voiceMessageMediaRepoFactory = { _, _, _ -> voiceMessageMediaRepo },
            eventId = eventId,
            mediaSource = mediaSource,
            mimeType = mimeType,
            body = body
        )
    },
    analyticsService = analyticsService,
    scope = this,
    content = content,
)
