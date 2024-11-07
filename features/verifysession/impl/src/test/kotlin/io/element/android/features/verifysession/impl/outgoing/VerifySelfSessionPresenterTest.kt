/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.features.logout.api.LogoutUseCase
import io.element.android.features.logout.test.FakeLogoutUseCase
import io.element.android.features.verifysession.impl.outgoing.VerifySelfSessionState.Step
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
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
        val presenter = createVerifySelfSessionPresenter(
            service = unverifiedSessionService(),
        )
        presenter.test {
            awaitItem().run {
                assertThat(step).isEqualTo(Step.Initial(false))
                assertThat(displaySkipButton).isTrue()
            }
        }
    }

    @Test
    fun `present - hides skip verification button on non-debuggable builds`() = runTest {
        val buildMeta = aBuildMeta(isDebuggable = false)
        val presenter = createVerifySelfSessionPresenter(
            service = unverifiedSessionService(),
            buildMeta = buildMeta,
        )
        presenter.test {
            assertThat(awaitItem().displaySkipButton).isFalse()
        }
    }

    @Test
    fun `present - Initial state is received, can use recovery key`() = runTest {
        val resetLambda = lambdaRecorder<Boolean, Unit> { }
        val presenter = createVerifySelfSessionPresenter(
            service = unverifiedSessionService(
                resetLambda = resetLambda
            ),
            encryptionService = FakeEncryptionService().apply {
                emitRecoveryState(RecoveryState.INCOMPLETE)
            }
        )
        presenter.test {
            assertThat(awaitItem().step).isEqualTo(Step.Initial(true))
            resetLambda.assertions().isCalledOnce().with(value(true))
        }
    }

    @Test
    fun `present - Initial state is received, can use recovery key and is last device`() = runTest {
        val presenter = createVerifySelfSessionPresenter(
            service = unverifiedSessionService(),
            encryptionService = FakeEncryptionService().apply {
                emitIsLastDevice(true)
                emitRecoveryState(RecoveryState.INCOMPLETE)
            }
        )
        presenter.test {
            assertThat(awaitItem().step).isEqualTo(Step.Initial(canEnterRecoveryKey = true, isLastDevice = true))
        }
    }

    @Test
    fun `present - Handles requestVerification`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            requestVerificationAndAwaitVerifyingState(service)
        }
    }

    @Test
    fun `present - Cancellation on initial state does nothing`() = runTest {
        val presenter = createVerifySelfSessionPresenter(
            service = unverifiedSessionService(),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.step).isEqualTo(Step.Initial(false))
            val eventSink = initialState.eventSink
            eventSink(VerifySelfSessionViewEvents.Cancel)
            expectNoEvents()
        }
    }

    @Test
    fun `present - A failure when verifying cancels it`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
            approveVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
            // Cancelling
            assertThat(awaitItem().step).isInstanceOf(Step.Verifying::class.java)
            service.emitVerificationFlowState(VerificationFlowState.DidFail)
            // Cancelled
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
        }
    }

    @Test
    fun `present - A fail when requesting verification resets the state to the initial one`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            awaitItem().eventSink(VerifySelfSessionViewEvents.UseAnotherDevice)
            awaitItem().eventSink(VerifySelfSessionViewEvents.RequestVerification)
            service.emitVerificationFlowState(VerificationFlowState.DidFail)
            assertThat(awaitItem().step).isInstanceOf(Step.AwaitingOtherDeviceResponse::class.java)
            assertThat(awaitItem().step).isEqualTo(Step.Initial(false))
        }
    }

    @Test
    fun `present - Canceling the flow once it's verifying cancels it`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
            cancelVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(VerifySelfSessionViewEvents.Cancel)
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
        }
    }

    @Test
    fun `present - When verifying, if we receive another challenge we ignore it`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            requestVerificationAndAwaitVerifyingState(service)
            service.emitVerificationFlowState(VerificationFlowState.DidReceiveVerificationData(SessionVerificationData.Emojis(emptyList())))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - Go back after cancellation returns to initial state`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            service.emitVerificationFlowState(VerificationFlowState.DidCancel)
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
            state.eventSink(VerifySelfSessionViewEvents.Reset)
            // Went back to initial state
            assertThat(awaitItem().step).isEqualTo(Step.Initial(false))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - When verification is approved, the flow completes if there is no error`() = runTest {
        val emojis = listOf(
            VerificationEmoji(number = 30, emoji = "😀", description = "Smiley")
        )
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
            approveVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(
                service,
                SessionVerificationData.Emojis(emojis)
            )
            state.eventSink(VerifySelfSessionViewEvents.ConfirmVerification)
            assertThat(awaitItem().step).isEqualTo(
                Step.Verifying(
                    SessionVerificationData.Emojis(emojis),
                    AsyncData.Loading(),
                )
            )
            service.emitVerificationFlowState(VerificationFlowState.DidFinish)
            assertThat(awaitItem().step).isEqualTo(Step.Completed)
        }
    }

    @Test
    fun `present - When verification is declined, the flow is canceled`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
            declineVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(VerifySelfSessionViewEvents.DeclineVerification)
            assertThat(awaitItem().step).isEqualTo(
                Step.Verifying(
                    SessionVerificationData.Emojis(emptyList()),
                    AsyncData.Loading(),
                )
            )
            service.emitVerificationFlowState(VerificationFlowState.DidCancel)
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
        }
    }

    @Test
    fun `present - Skip event skips the flow`() = runTest {
        val service = unverifiedSessionService(
            requestVerificationLambda = { },
            startVerificationLambda = { },
        )
        val presenter = createVerifySelfSessionPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(VerifySelfSessionViewEvents.SkipVerification)
            assertThat(awaitItem().step).isEqualTo(Step.Skipped)
        }
    }

    @Test
    fun `present - When verification is done using recovery key, the flow is completed`() = runTest {
        val service = FakeSessionVerificationService(
            resetLambda = { },
        ).apply {
            emitNeedsSessionVerification(false)
            emitVerifiedStatus(SessionVerifiedStatus.Verified)
            emitVerificationFlowState(VerificationFlowState.DidFinish)
        }
        val presenter = createVerifySelfSessionPresenter(
            service = service,
            showDeviceVerifiedScreen = true,
        )
        presenter.test {
            assertThat(awaitItem().step).isEqualTo(Step.Completed)
        }
    }

    @Test
    fun `present - When verification is not needed, the flow is skipped`() = runTest {
        val service = FakeSessionVerificationService(
            resetLambda = { },
        ).apply {
            emitNeedsSessionVerification(false)
            emitVerifiedStatus(SessionVerifiedStatus.Verified)
            emitVerificationFlowState(VerificationFlowState.DidFinish)
        }
        val presenter = createVerifySelfSessionPresenter(
            service = service,
            showDeviceVerifiedScreen = false,
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().step).isEqualTo(Step.Skipped)
        }
    }

    @Test
    fun `present - When user request to sign out, the sign out use case is invoked`() = runTest {
        val service = FakeSessionVerificationService(
            resetLambda = { },
        ).apply {
            emitNeedsSessionVerification(false)
            emitVerifiedStatus(SessionVerifiedStatus.Verified)
            emitVerificationFlowState(VerificationFlowState.DidFinish)
        }
        val signOutLambda = lambdaRecorder<Boolean, String?> { "aUrl" }
        val presenter = createVerifySelfSessionPresenter(
            service,
            logoutUseCase = FakeLogoutUseCase(signOutLambda)
        )
        presenter.test {
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
        assertThat(state.step).isEqualTo(Step.Initial(false))
        state.eventSink(VerifySelfSessionViewEvents.UseAnotherDevice)
        state = awaitItem()
        assertThat(state.step).isEqualTo(Step.UseAnotherDevice)
        state.eventSink(VerifySelfSessionViewEvents.RequestVerification)
        // Await for other device response:
        fakeService.emitVerificationFlowState(VerificationFlowState.DidAcceptVerificationRequest)
        state = awaitItem()
        assertThat(state.step).isEqualTo(Step.AwaitingOtherDeviceResponse)
        // Await for the state to be Ready
        state = awaitItem()
        assertThat(state.step).isEqualTo(Step.Ready)
        state.eventSink(VerifySelfSessionViewEvents.StartSasVerification)
        // Await for other device response (again):
        fakeService.emitVerificationFlowState(VerificationFlowState.DidStartSasVerification)
        state = awaitItem()
        assertThat(state.step).isEqualTo(Step.AwaitingOtherDeviceResponse)
        // Finally, ChallengeReceived:
        fakeService.emitVerificationFlowState(VerificationFlowState.DidReceiveVerificationData(sessionVerificationData))
        state = awaitItem()
        assertThat(state.step).isInstanceOf(Step.Verifying::class.java)
        return state
    }

    private suspend fun unverifiedSessionService(
        requestVerificationLambda: () -> Unit = { lambdaError() },
        cancelVerificationLambda: () -> Unit = { lambdaError() },
        approveVerificationLambda: () -> Unit = { lambdaError() },
        declineVerificationLambda: () -> Unit = { lambdaError() },
        startVerificationLambda: () -> Unit = { lambdaError() },
        resetLambda: (Boolean) -> Unit = { },
        acknowledgeVerificationRequestLambda: (SessionVerificationRequestDetails) -> Unit = { lambdaError() },
        acceptVerificationRequestLambda: () -> Unit = { lambdaError() },
    ): FakeSessionVerificationService {
        return FakeSessionVerificationService(
            requestVerificationLambda = requestVerificationLambda,
            cancelVerificationLambda = cancelVerificationLambda,
            approveVerificationLambda = approveVerificationLambda,
            declineVerificationLambda = declineVerificationLambda,
            startVerificationLambda = startVerificationLambda,
            resetLambda = resetLambda,
            acknowledgeVerificationRequestLambda = acknowledgeVerificationRequestLambda,
            acceptVerificationRequestLambda = acceptVerificationRequestLambda,
        ).apply {
            emitVerifiedStatus(SessionVerifiedStatus.NotVerified)
        }
    }

    private fun createVerifySelfSessionPresenter(
        service: SessionVerificationService,
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
