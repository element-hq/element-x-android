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
import io.element.android.features.messages.mediaplayer.FakeMediaPlayer
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageEvents
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessagePlayerImpl
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessagePresenter
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageState
import io.element.android.libraries.matrix.test.media.FakeMediaLoader
import kotlinx.coroutines.test.runTest
import org.junit.Test

class VoiceMessagePresenterTest {

    private val fakeMediaLoader = FakeMediaLoader()
    private val fakeVoiceCache = FakeVoiceMessageCache()

    @Test
    fun `initial state has proper default values`() = runTest {
        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache)
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
    fun `pressing play with file in cache plays`() = runTest {
        fakeVoiceCache.apply {
            givenIsInCache(true)
        }
        val content = aTimelineItemVoiceContent(durationMs = 2_000)
        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache, content)
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
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }
        }
    }

    @Test
    fun `pressing play with file not in cache downloads it but fails`() = runTest {
        fakeMediaLoader.apply {
            shouldFail = true
        }
        fakeVoiceCache.apply {
            givenIsInCache(false)
            givenMoveToCache(true)
        }
        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Retry)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }
        }
    }

    @Test
    fun `pressing play with file not in cache downloads it but then caching fails`() = runTest {
        fakeMediaLoader.apply {
            shouldFail = false
        }
        fakeVoiceCache.apply {
            givenIsInCache(false)
            givenMoveToCache(false)
        }
        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Downloading)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Retry)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }
        }
    }

    @Test
    fun `acquire control then play then play and pause while having control`() = runTest {
        fakeVoiceCache.apply {
            givenIsInCache(true)
        }
        val content = aTimelineItemVoiceContent(durationMs = 2_000)
        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache, content)
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
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

            awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(1.0f)
                Truth.assertThat(it.time).isEqualTo("0:02")
            }
        }
    }

    @Test
    fun `pressing play with file not in cache downloads it successfully`() = runTest {
        fakeMediaLoader.apply {
            shouldFail = false
        }
        fakeVoiceCache.apply {
            givenIsInCache(false)
            givenMoveToCache(true)
        }
        val content = aTimelineItemVoiceContent(durationMs = 2_000)
        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache, content)
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
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Pause)
                Truth.assertThat(it.progress).isEqualTo(0.5f)
                Truth.assertThat(it.time).isEqualTo("0:01")
            }
        }
    }

    @Test
    fun `content with null eventId shows disabled button`() = runTest {
        fakeMediaLoader.apply {
            shouldFail = false
        }
        fakeVoiceCache.apply {
            givenIsInCache(false)
            givenMoveToCache(true)
        }
        val content = aTimelineItemVoiceContent(eventId = null)
        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache, content)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Disabled)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("1:01")
            }
        }
    }

    @Test
    fun `seeking seeks`() = runTest {
        fakeVoiceCache.apply {
            givenIsInCache(true)
        }
        val content = aTimelineItemVoiceContent(durationMs = 10_000)

        val presenter = createVoiceMessagePresenter(fakeMediaLoader, fakeVoiceCache, content)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem().also {
                Truth.assertThat(it.button).isEqualTo(VoiceMessageState.Button.Play)
                Truth.assertThat(it.progress).isEqualTo(0f)
                Truth.assertThat(it.time).isEqualTo("0:10")
            }

            initialState.eventSink(VoiceMessageEvents.PlayPause)

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

fun createVoiceMessagePresenter(
    fakeMediaLoader: FakeMediaLoader,
    voiceCacheFake: FakeVoiceMessageCache,
    content: TimelineItemVoiceContent = aTimelineItemVoiceContent(),
) = VoiceMessagePresenter(
    mediaLoader = fakeMediaLoader,
    voiceMessagePlayerFactory = { eventId, mediaPath -> VoiceMessagePlayerImpl(FakeMediaPlayer(), eventId, mediaPath) },
    voiceMessageCacheFactory = { voiceCacheFake },
    content = content,
)
