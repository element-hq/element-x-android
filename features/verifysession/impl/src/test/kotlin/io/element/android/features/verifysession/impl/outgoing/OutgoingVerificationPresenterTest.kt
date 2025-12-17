/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.features.verifysession.impl.outgoing.OutgoingVerificationState.Step
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.verification.SessionVerificationData
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.api.verification.VerificationEmoji
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class OutgoingVerificationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - Initial state is received`() = runTest {
        val presenter = createOutgoingVerificationPresenter(
            service = unverifiedSessionService(),
        )
        presenter.test {
            awaitItem().run {
                assertThat(step).isEqualTo(Step.Initial)
            }
        }
    }

    @Test
    fun `present - Handles requestVerification for session verification`() = runTest {
        val requestSessionVerificationRecorder = lambdaRecorder<Unit> {}
        val startVerificationRecorder = lambdaRecorder<Unit> {}
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = requestSessionVerificationRecorder,
            startVerificationLambda = startVerificationRecorder,
        )
        val presenter = createOutgoingVerificationPresenter(
            service = service,
            verificationRequest = anOutgoingSessionVerificationRequest(),
        )
        presenter.test {
            requestVerificationAndAwaitVerifyingState(service)

            requestSessionVerificationRecorder.assertions().isCalledOnce()
            startVerificationRecorder.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - Handles requestVerification for user verification`() = runTest {
        val requestUserVerificationRecorder = lambdaRecorder<UserId, Unit> {}
        val startVerificationRecorder = lambdaRecorder<Unit> {}
        val service = unverifiedSessionService(
            requestUserVerificationLambda = requestUserVerificationRecorder,
            startVerificationLambda = startVerificationRecorder,
        )
        val presenter = createOutgoingVerificationPresenter(
            service = service,
            verificationRequest = anOutgoingUserVerificationRequest(),
        )
        presenter.test {
            requestVerificationAndAwaitVerifyingState(service)

            requestUserVerificationRecorder.assertions().isCalledOnce()
            startVerificationRecorder.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - Cancellation on initial state moves to Exit state`() = runTest {
        val presenter = createOutgoingVerificationPresenter(
            service = unverifiedSessionService(),
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.step).isEqualTo(Step.Initial)
            val eventSink = initialState.eventSink
            eventSink(OutgoingVerificationViewEvents.Cancel)

            assertThat(awaitItem().step).isEqualTo(Step.Exit)
        }
    }

    @Test
    fun `present - A failure when verifying cancels it`() = runTest {
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = { },
            startVerificationLambda = { },
            approveVerificationLambda = { },
        )
        val presenter = createOutgoingVerificationPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(OutgoingVerificationViewEvents.ConfirmVerification)
            // Cancelling
            assertThat(awaitItem().step).isInstanceOf(Step.Verifying::class.java)
            service.emitVerificationFlowState(VerificationFlowState.DidFail)
            // Cancelled
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
        }
    }

    @Test
    fun `present - A fail when requesting verification resets the state to the canceled one`() = runTest {
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = { },
        )
        val presenter = createOutgoingVerificationPresenter(service)
        presenter.test {
            awaitItem().eventSink(OutgoingVerificationViewEvents.RequestVerification)
            service.emitVerificationFlowState(VerificationFlowState.DidFail)
            assertThat(awaitItem().step).isInstanceOf(Step.AwaitingOtherDeviceResponse::class.java)
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
        }
    }

    @Test
    fun `present - Canceling the flow once it's verifying cancels it`() = runTest {
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = { },
            startVerificationLambda = { },
            cancelVerificationLambda = { },
        )
        val presenter = createOutgoingVerificationPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(OutgoingVerificationViewEvents.Cancel)
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
        }
    }

    @Test
    fun `present - When verifying, if we receive another challenge we ignore it`() = runTest {
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = { },
            startVerificationLambda = { },
        )
        val presenter = createOutgoingVerificationPresenter(service)
        presenter.test {
            requestVerificationAndAwaitVerifyingState(service)
            service.emitVerificationFlowState(VerificationFlowState.DidReceiveVerificationData(SessionVerificationData.Emojis(emptyList())))
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - Go back after cancellation returns to initial state`() = runTest {
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = { },
            startVerificationLambda = { },
        )
        val presenter = createOutgoingVerificationPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            service.emitVerificationFlowState(VerificationFlowState.DidCancel)
            assertThat(awaitItem().step).isEqualTo(Step.Canceled)
            state.eventSink(OutgoingVerificationViewEvents.Reset)
            // Went back to initial state
            assertThat(awaitItem().step).isEqualTo(Step.Initial)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - When verification is approved, the flow completes if there is no error`() = runTest {
        val emojis = listOf(
            VerificationEmoji(number = 30)
        )
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = { },
            startVerificationLambda = { },
            approveVerificationLambda = { },
        )
        val presenter = createOutgoingVerificationPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(
                service,
                SessionVerificationData.Emojis(emojis)
            )
            state.eventSink(OutgoingVerificationViewEvents.ConfirmVerification)
            assertThat(awaitItem().step).isEqualTo(
                Step.Verifying(
                    SessionVerificationData.Emojis(emojis),
                    AsyncData.Loading(),
                )
            )
            service.emitVerificationFlowState(VerificationFlowState.DidFinish)
            service.emitVerifiedStatus(SessionVerifiedStatus.Verified)
            assertThat(awaitItem().step).isEqualTo(Step.Completed)
        }
    }

    @Test
    fun `present - When verification is declined, the flow is canceled`() = runTest {
        val service = unverifiedSessionService(
            requestSessionVerificationLambda = { },
            startVerificationLambda = { },
            declineVerificationLambda = { },
        )
        val presenter = createOutgoingVerificationPresenter(service)
        presenter.test {
            val state = requestVerificationAndAwaitVerifyingState(service)
            state.eventSink(OutgoingVerificationViewEvents.DeclineVerification)
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
    fun `present - When verification is done using recovery key, the flow is completed`() = runTest {
        val service = FakeSessionVerificationService(
            resetLambda = { },
        ).apply {
            emitNeedsSessionVerification(false)
            emitVerifiedStatus(SessionVerifiedStatus.Verified)
            emitVerificationFlowState(VerificationFlowState.DidFinish)
        }
        val presenter = createOutgoingVerificationPresenter(
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
        val presenter = createOutgoingVerificationPresenter(
            service = service,
            showDeviceVerifiedScreen = false,
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().step).isEqualTo(Step.Exit)
        }
    }

    private suspend fun ReceiveTurbine<OutgoingVerificationState>.requestVerificationAndAwaitVerifyingState(
        fakeService: FakeSessionVerificationService,
        sessionVerificationData: SessionVerificationData = SessionVerificationData.Emojis(emptyList()),
    ): OutgoingVerificationState {
        var state = awaitItem()
        assertThat(state.step).isEqualTo(Step.Initial)
        state.eventSink(OutgoingVerificationViewEvents.RequestVerification)
        // Await for other device response:
        fakeService.emitVerificationFlowState(VerificationFlowState.DidAcceptVerificationRequest)
        state = awaitItem()
        assertThat(state.step).isEqualTo(Step.AwaitingOtherDeviceResponse)
        // Await for the state to be Ready
        state = awaitItem()
        assertThat(state.step).isEqualTo(Step.Ready)
        state.eventSink(OutgoingVerificationViewEvents.StartSasVerification)
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
        requestSessionVerificationLambda: () -> Unit = { lambdaError() },
        requestUserVerificationLambda: (UserId) -> Unit = { lambdaError() },
        cancelVerificationLambda: () -> Unit = { lambdaError() },
        approveVerificationLambda: () -> Unit = { lambdaError() },
        declineVerificationLambda: () -> Unit = { lambdaError() },
        startVerificationLambda: () -> Unit = { lambdaError() },
        resetLambda: (Boolean) -> Unit = { },
        acknowledgeVerificationRequestLambda: (VerificationRequest.Incoming) -> Unit = { lambdaError() },
        acceptVerificationRequestLambda: () -> Unit = { lambdaError() },
    ): FakeSessionVerificationService {
        return FakeSessionVerificationService(
            requestCurrentSessionVerificationLambda = requestSessionVerificationLambda,
            requestUserVerificationLambda = requestUserVerificationLambda,
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
}

internal fun createOutgoingVerificationPresenter(
    service: SessionVerificationService = FakeSessionVerificationService(),
    verificationRequest: VerificationRequest.Outgoing = anOutgoingSessionVerificationRequest(),
    encryptionService: EncryptionService = FakeEncryptionService(),
    showDeviceVerifiedScreen: Boolean = false,
): OutgoingVerificationPresenter {
    return OutgoingVerificationPresenter(
        showDeviceVerifiedScreen = showDeviceVerifiedScreen,
        verificationRequest = verificationRequest,
        sessionVerificationService = service,
        encryptionService = encryptionService,
    )
}
