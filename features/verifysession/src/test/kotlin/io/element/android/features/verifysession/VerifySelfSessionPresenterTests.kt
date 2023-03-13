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

package io.element.android.features.verifysession

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.matrix.api.verification.SessionVerificationServiceState
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
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.Initial)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)
            // We receive the same verification state several times due to different internal state of the service:
            // RequestVerification:
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.AwaitingOtherDeviceResponse)
            // VerificationRequestAccepted:
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.AwaitingOtherDeviceResponse)
            // SasVerificationStarted:
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.AwaitingOtherDeviceResponse)
            // Finally, ChallengeReceived:
            val verifyingState = awaitItem()
            assertThat(verifyingState.verificationState).isInstanceOf(VerificationState.Verifying::class.java)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.StartSasVerification)
            // StartingSasVerification:
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.AwaitingOtherDeviceResponse)
            // StartedSasVerification:
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.AwaitingOtherDeviceResponse)
            // ChallengeReceived:
            val verifyingState = awaitItem()
            assertThat(verifyingState.verificationState).isInstanceOf(VerificationState.Verifying::class.java)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            val verifyingState = awaitChallengeReceivedState()
            assertThat(verifyingState.verificationState).isInstanceOf(VerificationState.Verifying::class.java)

            service.shouldFail = true
            eventSink(VerifySelfSessionViewEvents.ConfirmVerification)

            val remainingEvents = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item<VerifySelfSessionState>)?.value }
            assertThat(remainingEvents.last().verificationState).isEqualTo(VerificationState.Canceled)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            val verifyingState = awaitChallengeReceivedState()
            assertThat(verifyingState.verificationState).isInstanceOf(VerificationState.Verifying::class.java)

            eventSink(VerifySelfSessionViewEvents.CancelAndClose)

            val remainingEvents = cancelAndConsumeRemainingEvents().mapNotNull { (it as? Event.Item<VerifySelfSessionState>)?.value }
            assertThat(remainingEvents.last().verificationState).isEqualTo(VerificationState.Canceled)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            val verifyingState = awaitChallengeReceivedState()
            assertThat(verifyingState.verificationState).isInstanceOf(VerificationState.Verifying::class.java)

            service.givenVerificationAttemptStatus(SessionVerificationServiceState.ReceivedVerificationData(emptyList()))

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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink

            eventSink(VerifySelfSessionViewEvents.RequestVerification)
            assertThat(awaitChallengeReceivedState().verificationState).isEqualTo(VerificationState.Verifying(emptyList(), Async.Uninitialized))

            service.givenVerificationAttemptStatus(SessionVerificationServiceState.Canceled)
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.Canceled)

            eventSink(VerifySelfSessionViewEvents.Restart)
            // Went back to requesting verification
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.AwaitingOtherDeviceResponse)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink

            eventSink(VerifySelfSessionViewEvents.RequestVerification)
            assertThat(awaitChallengeReceivedState().verificationState).isEqualTo(VerificationState.Verifying(emojis, Async.Uninitialized))

            eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.Verifying(emojis, Async.Loading()))
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.Completed)
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
            assertThat(initialState.verificationState).isEqualTo(VerificationState.Initial)
            val eventSink = initialState.eventSink

            eventSink(VerifySelfSessionViewEvents.RequestVerification)

            assertThat(awaitChallengeReceivedState().verificationState).isEqualTo(VerificationState.Verifying(emptyList(), Async.Uninitialized))
            eventSink(VerifySelfSessionViewEvents.DeclineVerification)

            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.Verifying(emptyList(), Async.Loading()))
            assertThat(awaitItem().verificationState).isEqualTo(VerificationState.Canceled)
        }
    }

    private suspend fun ReceiveTurbine<VerifySelfSessionState>.awaitChallengeReceivedState(): VerifySelfSessionState {
        // We receive the same verification state several times due to different internal state of the service:
        skipItems(3)
        return awaitItem()
    }
}
