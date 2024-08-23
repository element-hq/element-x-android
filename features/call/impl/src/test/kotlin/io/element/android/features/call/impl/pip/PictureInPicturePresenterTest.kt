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

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.lambda.lambdaRecorder
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PictureInPicturePresenterTest {
    @Test
    fun `when pip is not supported, the state value supportPip is false`() = runTest {
        val presenter = createPictureInPicturePresenter(supportPip = false)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.supportPip).isFalse()
        }
        presenter.setPipActivity(null)
    }

    @Test
    fun `when pip is supported, the state value supportPip is true`() = runTest {
        val presenter = createPictureInPicturePresenter(
            supportPip = true,
            pipActivity = FakePipActivity(setPipParamsResult = { }),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.supportPip).isTrue()
        }
    }

    @Test
    fun `when entering pip is supported, the state value isInPictureInPicture is true`() = runTest {
        val enterPipModeResult = lambdaRecorder<Boolean> { true }
        val presenter = createPictureInPicturePresenter(
            supportPip = true,
            pipActivity = FakePipActivity(
                setPipParamsResult = { },
                enterPipModeResult = enterPipModeResult,
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.isInPictureInPicture).isFalse()
            initialState.eventSink(PictureInPictureEvents.EnterPictureInPicture)
            enterPipModeResult.assertions().isCalledOnce()
            initialState.eventSink(PictureInPictureEvents.OnPictureInPictureModeChanged(true))
            val pipState = awaitItem()
            assertThat(pipState.isInPictureInPicture).isTrue()
            // User stops pip
            initialState.eventSink(PictureInPictureEvents.OnPictureInPictureModeChanged(false))
            val finalState = awaitItem()
            assertThat(finalState.isInPictureInPicture).isFalse()
        }
    }

    @Test
    fun `with webPipApi, when entering pip is supported, but web deny it, the call is finished`() = runTest {
        val handUpResult = lambdaRecorder<Unit> { }
        val presenter = createPictureInPicturePresenter(
            supportPip = true,
            pipActivity = FakePipActivity(
                setPipParamsResult = { },
                handUpResult = handUpResult
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(PictureInPictureEvents.SetupWebPipApi(FakeWebPipApi(canEnterPipResult = { false })))
            initialState.eventSink(PictureInPictureEvents.EnterPictureInPicture)
            handUpResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `with webPipApi, when entering pip is supported, and web allows it, the state value isInPictureInPicture is true`() = runTest {
        val enterPipModeResult = lambdaRecorder<Boolean> { true }
        val enterPipResult = lambdaRecorder<Unit> { }
        val exitPipResult = lambdaRecorder<Unit> { }
        val presenter = createPictureInPicturePresenter(
            supportPip = true,
            pipActivity = FakePipActivity(
                setPipParamsResult = { },
                enterPipModeResult = enterPipModeResult
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            initialState.eventSink(
                PictureInPictureEvents.SetupWebPipApi(
                    FakeWebPipApi(
                        canEnterPipResult = { true },
                        enterPipResult = enterPipResult,
                        exitPipResult = exitPipResult,
                    )
                )
            )
            initialState.eventSink(PictureInPictureEvents.EnterPictureInPicture)
            enterPipModeResult.assertions().isCalledOnce()
            enterPipResult.assertions().isNeverCalled()
            initialState.eventSink(PictureInPictureEvents.OnPictureInPictureModeChanged(true))
            val pipState = awaitItem()
            assertThat(pipState.isInPictureInPicture).isTrue()
            enterPipResult.assertions().isCalledOnce()
            // User stops pip
            exitPipResult.assertions().isNeverCalled()
            initialState.eventSink(PictureInPictureEvents.OnPictureInPictureModeChanged(false))
            val finalState = awaitItem()
            assertThat(finalState.isInPictureInPicture).isFalse()
            exitPipResult.assertions().isCalledOnce()
        }
    }

    private fun createPictureInPicturePresenter(
        supportPip: Boolean = true,
        pipActivity: PipActivity? = FakePipActivity()
    ): PictureInPicturePresenter {
        return PictureInPicturePresenter(
            pipSupportProvider = FakePipSupportProvider(supportPip),
        ).apply {
            setPipActivity(pipActivity)
        }
    }
}
