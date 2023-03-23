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

package io.element.android.features.verifysession.impl

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.verifysession.impl.VerifySelfSessionState.VerificationStep as VerificationStep
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class VerifySelfSessionPresenterTests {

    @Test
    fun `present - Initial state is received`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Initial)
        }
    }

    @Test
    fun `present - Handles requestVerification`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)
            // Await for other device response:
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
            // Await for the state to be Ready
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Ready)
            // Await for other device response (again):
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
            // Finally, ChallengeReceived:
            val verifyingState = awaitItem()
            assertThat(verifyingState.verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)
        }
    }

    @Test
    fun `present - Handles startSasVerification`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.StartSasVerification)
            // Await for other device response:
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
            // ChallengeReceived:
            val verifyingState = awaitItem()
            assertThat(verifyingState.verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)
        }
    }

    @Test
    fun `present - Cancelation on initial state does nothing`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.CancelAndClose)
            expectNoEvents()
        }
    }

    @Test
    fun `present - A fail in the flow cancels it`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            val verifyingState = awaitChallengeReceivedState()
            assertThat(verifyingState.verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)

            service.shouldFail = true
            eventSink(VerifySelfSessionViewEvents.ConfirmVerification)

            val remainingEvents = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item<VerifySelfSessionState>)?.value }
            assertThat(remainingEvents.last().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
        }
    }

    @Test
    fun `present - Canceling the flow once it's verifying cancels it`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            val verifyingState = awaitChallengeReceivedState()
            assertThat(verifyingState.verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)

            eventSink(VerifySelfSessionViewEvents.CancelAndClose)

            val remainingEvents = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item<VerifySelfSessionState>)?.value }
            assertThat(remainingEvents.last().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
        }
    }

    @Test
    fun `present - When verifying, if we receive another challenge we ignore it`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            val verifyingState = awaitChallengeReceivedState()
            assertThat(verifyingState.verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)

            service.givenVerificationFlowState(VerificationFlowState.ReceivedVerificationData(emptyList()))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - Restart after cancelation returns to requesting verification`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink

            eventSink(VerifySelfSessionViewEvents.RequestVerification)
            assertThat(awaitChallengeReceivedState().verificationFlowStep).isEqualTo(VerificationStep.Verifying(emptyList(), Async.Uninitialized))

            service.givenVerificationFlowState(VerificationFlowState.Canceled)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Canceled)

            eventSink(VerifySelfSessionViewEvents.Restart)
            // Went back to requesting verification
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - When verification is approved, the flow completes if there is no error`() = runTest {
        val emojis = listOf<VerificationEmoji>(
            VerificationEmoji("😄", "Smile")
        )
        val service = FakeSessionVerificationService().apply {
            givenEmojiList(emojis)
        }
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink

            eventSink(VerifySelfSessionViewEvents.RequestVerification)
            assertThat(awaitChallengeReceivedState().verificationFlowStep).isEqualTo(VerificationStep.Verifying(emojis, Async.Uninitialized))

            eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Verifying(emojis, Async.Loading()))
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Completed)
        }
    }

    @Test
    fun `present - When verification is declined, the flow is canceled`() = runTest {
        val service = FakeSessionVerificationService()
        val presenter = VerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionClock.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial)
            val eventSink = initialState.eventSink

            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            assertThat(awaitChallengeReceivedState().verificationFlowStep).isEqualTo(VerificationStep.Verifying(emptyList(), Async.Uninitialized))
            eventSink(VerifySelfSessionViewEvents.DeclineVerification)

            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Verifying(emptyList(), Async.Loading()))
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
        }
    }

    private suspend fun ReceiveTurbine<VerifySelfSessionState>.awaitChallengeReceivedState(): VerifySelfSessionState {
        // Skip 'waiting for response', 'ready' and 'starting verification' state
        skipItems(3)
        // Received challenge
        return awaitItem()
    }
}
