/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.LogoutUseCase
import io.element.android.features.logout.test.FakeLogoutUseCase
import io.element.android.features.verifysession.impl.VerifySelfSessionState.VerificationStep
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class VerifySelfSessionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - Initial state is received`() = runTest {
        val presenter = createVerifySelfSessionPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().run {
                assertThat(verificationFlowStep).isEqualTo(VerificationStep.Initial(false))
                assertThat(displaySkipButton).isTrue()
            }
        }
    }

    @Test
    fun `present - hides skip verification button on non-debuggable builds`() = runTest {
        val buildMeta = aBuildMeta(isDebuggable = false)
        val presenter = createVerifySelfSessionPresenter(buildMeta = buildMeta)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().displaySkipButton).isFalse()
        }
    }

    @Test
    fun `present - Initial state is received, can use recovery key`() = runTest {
        val presenter = createVerifySelfSessionPresenter(
            encryptionService = FakeEncryptionService().apply {
                emitRecoveryState(RecoveryState.INCOMPLETE)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Initial(true))
        }
    }

    @Test
    fun `present - Initial state is received, can use recovery key and is last device`() = runTest {
        val presenter = createVerifySelfSessionPresenter(
            encryptionService = FakeEncryptionService().apply {
                emitIsLastDevice(true)
                emitRecoveryState(RecoveryState.INCOMPLETE)
            }
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Initial(canEnterRecoveryKey = true, isLastDevice = true))
        }
    }

    @Test
    fun `present - Handles requestVerification`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            requestVerificationAndAwaitVerifyingState(service)
        }
    }

    @Test
    fun `present - Handles startSasVerification`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial(false))
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.StartSasVerification)
            // Await for other device response:
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
            // ChallengeReceived:
            service.triggerReceiveVerificationData(SessionVerificationData.Emojis(emptyList()))
            val verifyingState = awaitItem()
            assertThat(verifyingState.verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)
        }
    }

    @Test
    fun `present - Cancelation on initial state does nothing`() = runTest {
        val presenter = createVerifySelfSessionPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.verificationFlowStep).isEqualTo(VerificationStep.Initial(false))
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.Cancel)
            expectNoEvents()
        }
    }

    @Test
    fun `present - A failure when verifying cancels it`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            service.shouldFail = true
            state.eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
            // Cancelling
            assertThat(awaitItem().verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)
            // Cancelled
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
        }
    }

    @Test
    fun `present - A fail when requesting verification resets the state to the initial one`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            service.shouldFail = true
            awaitItem().eventSink(VerifySelfSessionViewEvents.RequestVerification)
            service.shouldFail = false
            assertThat(awaitItem().verificationFlowStep).isInstanceOf(VerificationStep.AwaitingOtherDeviceResponse::class.java)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Initial(false))
        }
    }

    @Test
    fun `present - Canceling the flow once it's verifying cancels it`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(VerifySelfSessionViewEvents.Cancel)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
        }
    }

    @Test
    fun `present - When verifying, if we receive another challenge we ignore it`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            requestVerificationAndAwaitVerifyingState(service)
            service.givenVerificationFlowState(VerificationFlowState.ReceivedVerificationData(SessionVerificationData.Emojis(emptyList())))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - Restart after cancelation returns to requesting verification`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            service.givenVerificationFlowState(VerificationFlowState.Canceled)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
            state.eventSink(VerifySelfSessionViewEvents.RequestVerification)
            // Went back to requesting verification
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Go back after cancelation returns to initial state`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            service.givenVerificationFlowState(VerificationFlowState.Canceled)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
            state.eventSink(VerifySelfSessionViewEvents.Reset)
            // Went back to initial state
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Initial(false))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - When verification is approved, the flow completes if there is no error`() = runTest {
        val emojis = listOf(
            VerificationEmoji(number = 30, emoji = "😀", description = "Smiley")
        )
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = requestVerificationAndAwaitVerifyingState(
                service,
                SessionVerificationData.Emojis(emojis)
            )
            state.eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(
                VerificationStep.Verifying(
                    SessionVerificationData.Emojis(emojis),
                    AsyncData.Loading(),
                )
            )
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Completed)
        }
    }

    @Test
    fun `present - When verification is declined, the flow is canceled`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(VerifySelfSessionViewEvents.DeclineVerification)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(
                VerificationStep.Verifying(
                    SessionVerificationData.Emojis(emptyList()),
                    AsyncData.Loading(),
                )
            )
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Canceled)
        }
    }

    @Test
    fun `present - Skip event skips the flow`() = runTest {
        val service = unverifiedSessionService()
        val presenter = createVerifySelfSessionPresenter(service)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(VerifySelfSessionViewEvents.SkipVerification)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Skipped)
        }
    }

    @Test
    fun `present - When verification is done using recovery key, the flow is completed`() = runTest {
        val service = FakeSessionVerificationService().apply {
            givenNeedsSessionVerification(false)
            givenVerifiedStatus(SessionVerifiedStatus.Verified)
            givenVerificationFlowState(VerificationFlowState.Finished)
        }
        val presenter = createVerifySelfSessionPresenter(
            service = service,
            showDeviceVerifiedScreen = true,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Completed)
        }
    }

    @Test
    fun `present - When verification is not needed, the flow is skipped`() = runTest {
        val service = FakeSessionVerificationService().apply {
            givenNeedsSessionVerification(false)
            givenVerifiedStatus(SessionVerifiedStatus.Verified)
            givenVerificationFlowState(VerificationFlowState.Finished)
        }
        val presenter = createVerifySelfSessionPresenter(
            service = service,
            showDeviceVerifiedScreen = false,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            assertThat(awaitItem().verificationFlowStep).isEqualTo(VerificationStep.Skipped)
        }
    }

    @Test
    fun `present - When user request to sign out, the sign out use case is invoked`() = runTest {
        val service = FakeSessionVerificationService().apply {
            givenNeedsSessionVerification(false)
            givenVerifiedStatus(SessionVerifiedStatus.Verified)
            givenVerificationFlowState(VerificationFlowState.Finished)
        }
        val signOutLambda = lambdaRecorder<Boolean, String?> { "aUrl" }
        val presenter = createVerifySelfSessionPresenter(
            service,
            logoutUseCase = FakeLogoutUseCase(signOutLambda)
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val initialItem = awaitItem()
            initialItem.eventSink(VerifySelfSessionViewEvents.SignOut)
            assertThat(awaitItem().signOutAction.isLoading()).isTrue()
            val finalItem = awaitItem()
            assertThat(finalItem.signOutAction.isSuccess()).isTrue()
            assertThat(finalItem.signOutAction.dataOrNull()).isEqualTo("aUrl")
            signOutLambda.assertions().isCalledOnce().with(value(true))
        }
    }

    private suspend fun ReceiveTurbine<VerifySelfSessionState>.requestVerificationAndAwaitVerifyingState(
        fakeService: FakeSessionVerificationService,
        sessionVerificationData: SessionVerificationData = SessionVerificationData.Emojis(emptyList()),
    ): VerifySelfSessionState {
        var state = awaitItem()
        assertThat(state.verificationFlowStep).isEqualTo(VerificationStep.Initial(false))
        state.eventSink(VerifySelfSessionViewEvents.RequestVerification)
        // Await for other device response:
        state = awaitItem()
        assertThat(state.verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
        // Await for the state to be Ready
        state = awaitItem()
        assertThat(state.verificationFlowStep).isEqualTo(VerificationStep.Ready)
        state.eventSink(VerifySelfSessionViewEvents.StartSasVerification)
        // Await for other device response (again):
        state = awaitItem()
        assertThat(state.verificationFlowStep).isEqualTo(VerificationStep.AwaitingOtherDeviceResponse)
        fakeService.triggerReceiveVerificationData(sessionVerificationData)
        // Finally, ChallengeReceived:
        state = awaitItem()
        assertThat(state.verificationFlowStep).isInstanceOf(VerificationStep.Verifying::class.java)
        return state
    }

    private fun unverifiedSessionService(): FakeSessionVerificationService {
        return FakeSessionVerificationService().apply {
            givenVerifiedStatus(SessionVerifiedStatus.NotVerified)
        }
    }

    private fun createVerifySelfSessionPresenter(
        service: SessionVerificationService = unverifiedSessionService(),
        encryptionService: EncryptionService = FakeEncryptionService(),
        buildMeta: BuildMeta = aBuildMeta(),
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        logoutUseCase: LogoutUseCase = FakeLogoutUseCase(),
        showDeviceVerifiedScreen: Boolean = false,
    ): VerifySelfSessionPresenter {
        return VerifySelfSessionPresenter(
            showDeviceVerifiedScreen = showDeviceVerifiedScreen,
            sessionVerificationService = service,
            encryptionService = encryptionService,
            stateMachine = VerifySelfSessionStateMachine(service, encryptionService),
            buildMeta = buildMeta,
            sessionPreferencesStore = sessionPreferencesStore,
            logoutUseCase = logoutUseCase,
        )
    }
}
