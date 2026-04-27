/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PictureInPicturePresenterTest {
    @Test
    fun `when pip is not supported, the state value supportPip is false`() = runTest {
        val presenter = createPictureInPicturePresenter(supportPip = false)
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.supportPip).isFalse()
        }
        presenter.setPipView(null)
    }

    @Test
    fun `when pip is supported, the state value supportPip is true`() = runTest {
        val presenter = createPictureInPicturePresenter(
            supportPip = true,
            pipView = FakePipView(setPipParamsResult = { }),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.supportPip).isTrue()
        }
    }

    @Test
    fun `when entering pip is supported, the state value isInPictureInPicture is true`() = runTest {
        val enterPipModeResult = lambdaRecorder<Boolean> { true }
        val presenter = createPictureInPicturePresenter(
            supportPip = true,
            pipView = FakePipView(
                setPipParamsResult = { },
                enterPipModeResult = enterPipModeResult,
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.isInPictureInPicture).isFalse()
            initialState.eventSink(PictureInPictureEvent.EnterPictureInPicture)
            enterPipModeResult.assertions().isCalledOnce()
            initialState.eventSink(PictureInPictureEvent.OnPictureInPictureModeChanged(true))
            val pipState = awaitItem()
            assertThat(pipState.isInPictureInPicture).isTrue()
            // User stops pip
            initialState.eventSink(PictureInPictureEvent.OnPictureInPictureModeChanged(false))
            val finalState = awaitItem()
            assertThat(finalState.isInPictureInPicture).isFalse()
        }
    }

    @Test
    fun `with webPipApi, when entering pip is supported, but web deny it, the call is finished`() = runTest {
        val handUpResult = lambdaRecorder<Unit> { }
        val presenter = createPictureInPicturePresenter(
            supportPip = true,
            pipView = FakePipView(
                setPipParamsResult = { },
                handUpResult = handUpResult
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(PictureInPictureEvent.SetPipController(FakePipController(canEnterPipResult = { false })))
            initialState.eventSink(PictureInPictureEvent.EnterPictureInPicture)
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
            pipView = FakePipView(
                setPipParamsResult = { },
                enterPipModeResult = enterPipModeResult
            ),
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(
                PictureInPictureEvent.SetPipController(
                    FakePipController(
                        canEnterPipResult = { true },
                        enterPipResult = enterPipResult,
                        exitPipResult = exitPipResult,
                    )
                )
            )
            initialState.eventSink(PictureInPictureEvent.EnterPictureInPicture)
            enterPipModeResult.assertions().isCalledOnce()
            enterPipResult.assertions().isNeverCalled()
            initialState.eventSink(PictureInPictureEvent.OnPictureInPictureModeChanged(true))
            val pipState = awaitItem()
            assertThat(pipState.isInPictureInPicture).isTrue()
            enterPipResult.assertions().isCalledOnce()
            // User stops pip
            exitPipResult.assertions().isNeverCalled()
            initialState.eventSink(PictureInPictureEvent.OnPictureInPictureModeChanged(false))
            val finalState = awaitItem()
            assertThat(finalState.isInPictureInPicture).isFalse()
            exitPipResult.assertions().isCalledOnce()
        }
    }

    private fun createPictureInPicturePresenter(
        supportPip: Boolean = true,
        pipView: PipView? = FakePipView()
    ): PictureInPicturePresenter {
        return PictureInPicturePresenter(
            pipSupportProvider = FakePipSupportProvider(supportPip),
        ).apply {
            setPipView(pipView)
        }
    }
}
