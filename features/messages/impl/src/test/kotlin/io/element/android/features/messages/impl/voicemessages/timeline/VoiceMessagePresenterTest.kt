/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemVoiceContent
import io.element.android.features.messages.impl.voicemessages.VoiceMessageException
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class VoiceMessagePresenterTest {
    @Test
    fun `initial state has proper default values`() = runTest {
        val presenter = createVoiceMessagePresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().let {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("1:01")
            }
        }
    }

    @Test
    fun `pressing play downloads and plays`() = runTest {
        val presenter = createVoiceMessagePresenter(
            mediaPlayer = FakeMediaPlayer(fakeTotalDurationMs = 2_000),
            content = aTimelineItemVoiceContent(duration = 2_000.milliseconds),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:02")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:02")
            }
            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:00")
            }
            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                assertThat(it.progress).isEqualTo(0.5f)
                assertThat(it.time).isEqualTo("0:01")
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
            content = aTimelineItemVoiceContent(duration = 2_000.milliseconds),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:02")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:02")
            }
            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Retry)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:02")
            }
            analyticsService.trackedErrors.first().also {
                assertThat(it).apply {
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
            content = aTimelineItemVoiceContent(duration = 2_000.milliseconds),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:02")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)
            skipItems(2) // skip downloading states

            val playingState = awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                assertThat(it.progress).isEqualTo(0.5f)
                assertThat(it.time).isEqualTo("0:01")
            }

            playingState.eventSink(VoiceMessageEvents.PlayPause)
            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0.5f)
                assertThat(it.time).isEqualTo("0:01")
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
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Disabled)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("1:01")
            }
        }
    }

    @Test
    fun `seeking before play`() = runTest {
        val presenter = createVoiceMessagePresenter(
            mediaPlayer = FakeMediaPlayer(fakeTotalDurationMs = 2_000),
            content = aTimelineItemVoiceContent(duration = 10_000.milliseconds),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:10")
            }

            initialState.eventSink(VoiceMessageEvents.Seek(0.5f))

            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0.5f)
                assertThat(it.time).isEqualTo("0:05")
            }
        }
    }

    @Test
    fun `seeking after play`() = runTest {
        val presenter = createVoiceMessagePresenter(
            content = aTimelineItemVoiceContent(duration = 10_000.milliseconds),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                assertThat(it.progress).isEqualTo(0f)
                assertThat(it.time).isEqualTo("0:10")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            skipItems(2) // skip downloading states

            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                assertThat(it.progress).isEqualTo(0.1f)
                assertThat(it.time).isEqualTo("0:01")
            }

            initialState.eventSink(VoiceMessageEvents.Seek(0.5f))

            awaitItem().also {
                assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                assertThat(it.progress).isEqualTo(0.5f)
                assertThat(it.time).isEqualTo("0:05")
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
    voiceMessagePlayerFactory = { eventId, mediaSource, mimeType, filename ->
        DefaultVoiceMessagePlayer(
            mediaPlayer = mediaPlayer,
            voiceMessageMediaRepoFactory = { _, _, _ -> voiceMessageMediaRepo },
            eventId = eventId,
            mediaSource = mediaSource,
            mimeType = mimeType,
            filename = filename
        )
    },
    analyticsService = analyticsService,
    scope = this,
    content = content,
)
