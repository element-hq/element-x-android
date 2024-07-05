/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.call.impl.pip

import android.os.Build.VERSION_CODES
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.ui.ElementCallActivity
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class PictureInPicturePresenterTest {
    @Test
    @Config(sdk = [VERSION_CODES.N, VERSION_CODES.O, VERSION_CODES.S])
    fun `when pip is not supported, the state value supportPip is false`() = runTest {
        val presenter = createPictureInPicturePresenter(supportPip = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.supportPip).isFalse()
        }
        presenter.onDestroy()
    }

    @Test
    @Config(sdk = [VERSION_CODES.O, VERSION_CODES.S])
    fun `when pip is supported, the state value supportPip is true`() = runTest {
        val presenter = createPictureInPicturePresenter(supportPip = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.supportPip).isTrue()
        }
        presenter.onDestroy()
    }

    @Test
    @Config(sdk = [VERSION_CODES.S])
    fun `when entering pip is supported, the state value isInPictureInPicture is true`() = runTest {
        val presenter = createPictureInPicturePresenter(supportPip = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isInPictureInPicture).isFalse()
            initialState.eventSink(PictureInPictureEvents.EnterPictureInPicture)
            presenter.onPictureInPictureModeChanged(true)
            val pipState = awaitItem()
            assertThat(pipState.isInPictureInPicture).isTrue()
            // User stops pip
            presenter.onPictureInPictureModeChanged(false)
            val finalState = awaitItem()
            assertThat(finalState.isInPictureInPicture).isFalse()
        }
        presenter.onDestroy()
    }

    @Test
    @Config(sdk = [VERSION_CODES.S])
    fun `when onUserLeaveHint is called, the state value isInPictureInPicture becomes true`() = runTest {
        val presenter = createPictureInPicturePresenter(supportPip = true)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isInPictureInPicture).isFalse()
            presenter.onUserLeaveHint()
            presenter.onPictureInPictureModeChanged(true)
            val pipState = awaitItem()
            assertThat(pipState.isInPictureInPicture).isTrue()
        }
        presenter.onDestroy()
    }

    private fun createPictureInPicturePresenter(
        supportPip: Boolean = true,
    ): PictureInPicturePresenter {
        val activity = Robolectric.buildActivity(ElementCallActivity::class.java)
        return PictureInPicturePresenter(
            pipSupportProvider = FakePipSupportProvider(supportPip),
        ).apply {
            onCreate(activity.get())
        }
    }
}
