/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.libraries.voiceplayer.api.VoiceMessageEvents
import io.element.android.libraries.voiceplayer.api.VoiceMessageException
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration
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
            duration = 2_000.milliseconds,
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
            duration = 2_000.milliseconds,
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
            duration = 2_000.milliseconds,
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
            eventId = null,
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
            duration = 10_000.milliseconds,
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
            duration = 10_000.milliseconds,
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
    eventId: EventId? = EventId("\$anEventId"),
    filename: String = "filename doesn't really matter for a voice message",
    duration: Duration = 61_000.milliseconds,
    contentUri: String = "mxc://matrix.org/1234567890abcdefg",
    mimeType: String = MimeTypes.Ogg,
    mediaSource: MediaSource = MediaSource(contentUri),
) = VoiceMessagePresenter(
    analyticsService = analyticsService,
    sessionCoroutineScope = this,
    player = DefaultVoiceMessagePlayer(
        mediaPlayer = mediaPlayer,
        voiceMessageMediaRepoFactory = { _, _, _ -> voiceMessageMediaRepo },
        eventId = eventId,
        mediaSource = mediaSource,
        mimeType = mimeType,
        filename = filename
    ),
    eventId = eventId,
    duration = duration,
)
