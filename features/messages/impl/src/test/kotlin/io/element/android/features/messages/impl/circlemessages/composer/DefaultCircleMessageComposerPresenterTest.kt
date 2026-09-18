/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.circlemessages.composer

import android.Manifest
import androidx.lifecycle.Lifecycle
import app.cash.turbine.TurbineTestContext
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.api.timeline.circlemessages.composer.CircleMessageComposerEvent
import io.element.android.features.messages.api.timeline.circlemessages.composer.CircleMessageComposerState
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.circlerecorder.test.FakeCircleRecorder
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaplayer.test.FakeAudioFocus
import io.element.android.libraries.mediaupload.test.FakeMediaSender
import io.element.android.libraries.permissions.api.aPermissionsState
import io.element.android.libraries.permissions.test.FakePermissionsPresenter
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCircleMessageComposerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.isRecording).isFalse()
            assertThat(initialState.useFrontCamera).isTrue()
            assertThat(initialState.keepScreenOn).isFalse()
            assertThat(initialState.showCameraPermissionRationaleDialog).isFalse()
            assertThat(initialState.showAudioPermissionRationaleDialog).isFalse()
            assertThat(initialState.showSendFailureDialog).isFalse()
        }
    }

    @Test
    fun `present - start records when permissions are granted`() = runTest {
        val startRecordResult = lambdaRecorder<Unit> { }
        val circleRecorder = FakeCircleRecorder(
            startRecordResult = startRecordResult,
            stopRecordResult = {},
            deleteRecordingResult = {},
        )
        val presenter = createPresenter(circleRecorder = circleRecorder)
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(CircleMessageComposerEvent.Start)
            val recordingState = awaitItem()
            assertThat(recordingState.isRecording).isTrue()
            assertThat(recordingState.keepScreenOn).isTrue()
            assertThat(circleRecorder.bindPreviewCount).isEqualTo(0)
            startRecordResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - pause while recording cancels without sending`() = runTest {
        val startRecordResult = lambdaRecorder<Unit> { }
        val stopRecordResult = lambdaRecorder<Boolean, Unit> { }
        val deleteRecordingResult = lambdaRecorder<Unit> { }
        val sendCircleMessageResult = lambdaRecorder<Result<Unit>>(ensureNeverCalled = true) { Result.success(Unit) }
        val presenter = createPresenter(
            circleRecorder = FakeCircleRecorder(
                startRecordResult = startRecordResult,
                stopRecordResult = stopRecordResult,
                deleteRecordingResult = deleteRecordingResult,
            ),
            sendCircleMessageResult = sendCircleMessageResult,
        )
        presenter.test {
            awaitItem().eventSink(CircleMessageComposerEvent.Start)
            awaitItem().eventSink(CircleMessageComposerEvent.LifecycleEvent(Lifecycle.Event.ON_PAUSE))
            val idleState = awaitItem()
            assertThat(idleState.isRecording).isFalse()
            startRecordResult.assertions().isCalledOnce()
            stopRecordResult.assertions().isCalledOnce().with(value(true))
            deleteRecordingResult.assertions().isCalledOnce()
            sendCircleMessageResult.assertions().isNeverCalled()
        }
    }

    @Test
    fun `present - start while sending is ignored`() = runTest {
        val startRecordResult = lambdaRecorder<Unit> { }
        val sendGate = CompletableDeferred<Unit>()
        val presenter = createPresenter(
            circleRecorder = FakeCircleRecorder(
                startRecordResult = startRecordResult,
                stopRecordResult = {},
                deleteRecordingResult = {},
            ),
            sendCircleMessageResult = {
                sendGate.await()
                Result.success(Unit)
            },
        )
        presenter.test {
            awaitItem().eventSink(CircleMessageComposerEvent.Start)
            awaitItem().eventSink(CircleMessageComposerEvent.Stop)
            advanceUntilIdle()
            expectMostRecentItem().eventSink(CircleMessageComposerEvent.Start)
            advanceUntilIdle()
            startRecordResult.assertions().isCalledOnce()
            sendGate.complete(Unit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - dismiss send failure retries upload`() = runTest {
        val deleteRecordingResult = lambdaRecorder<Unit> { }
        var sendAttempts = 0
        val presenter = createPresenter(
            circleRecorder = FakeCircleRecorder(
                recordingDuration = 2.seconds,
                startRecordResult = {},
                stopRecordResult = {},
                deleteRecordingResult = deleteRecordingResult,
            ),
            sendCircleMessageResult = {
                sendAttempts++
                if (sendAttempts == 1) {
                    Result.failure(IllegalStateException("send failed"))
                } else {
                    Result.success(Unit)
                }
            },
        )
        presenter.test {
            awaitItem().eventSink(CircleMessageComposerEvent.Start)
            awaitItem().eventSink(CircleMessageComposerEvent.Stop)
            val failureState = awaitUntil { it.showSendFailureDialog }
            assertThat(sendAttempts).isEqualTo(1)
            deleteRecordingResult.assertions().isNeverCalled()

            failureState.eventSink(CircleMessageComposerEvent.DismissSendFailureDialog)
            val dismissedState = awaitUntil { !it.showSendFailureDialog }
            assertThat(dismissedState.showSendFailureDialog).isFalse()
            advanceUntilIdle()
            assertThat(sendAttempts).isEqualTo(2)
            deleteRecordingResult.assertions().isCalledOnce()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun TurbineTestContext<CircleMessageComposerState>.awaitUntil(
        predicate: (CircleMessageComposerState) -> Boolean,
    ): CircleMessageComposerState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    private fun TestScope.createPresenter(
        circleRecorder: FakeCircleRecorder = FakeCircleRecorder(
            startRecordResult = {},
            stopRecordResult = {},
            deleteRecordingResult = {},
        ),
        sendCircleMessageResult: suspend () -> Result<Unit> = { Result.success(Unit) },
    ) = DefaultCircleMessageComposerPresenter(
        sessionCoroutineScope = backgroundScope,
        timelineMode = Timeline.Mode.Live,
        circleRecorder = circleRecorder,
        audioFocus = FakeAudioFocus(
            requestAudioFocusResult = { _, _ -> },
            releaseAudioFocusResult = {},
        ),
        mediaSenderFactory = {
            FakeMediaSender(
                sendCircleMessageResult = sendCircleMessageResult,
            )
        },
        messageComposerContext = FakeMessageComposerContext(),
        permissionsPresenterFactory = FakePermissionsPresenterFactory(
            FakePermissionsPresenter(
                initialState = aPermissionsState(
                    showDialog = false,
                    permission = Manifest.permission.CAMERA,
                    permissionGranted = true,
                )
            )
        ),
    )
}
