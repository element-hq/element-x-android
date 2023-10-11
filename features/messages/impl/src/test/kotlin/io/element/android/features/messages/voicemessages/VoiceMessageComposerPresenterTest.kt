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

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.voicemessages

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.voicemessages.VoiceMessageComposerEvents
import io.element.android.features.messages.impl.voicemessages.VoiceMessageComposerPresenter
import io.element.android.libraries.textcomposer.model.PressEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageState
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class VoiceMessageComposerPresenterTest {

    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.voiceMessageState).isEqualTo(VoiceMessageState.Idle)
        }
    }

    @Test
    fun `present - recording state`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))

            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Recording)
        }
    }

    @Test
    fun `present - abort recording`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.Tapped))

            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)
        }
    }

    @Test
    fun `present - finish recording`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.PressStart))
            awaitItem().eventSink(VoiceMessageComposerEvents.RecordButtonEvent(PressEvent.LongPressEnd))

            assertThat(awaitItem().voiceMessageState).isEqualTo(VoiceMessageState.Idle)
        }
    }
    private fun createPresenter() = VoiceMessageComposerPresenter()
}
