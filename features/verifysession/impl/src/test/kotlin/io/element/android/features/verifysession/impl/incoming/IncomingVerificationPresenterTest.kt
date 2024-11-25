/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.incoming

import com.google.common.truth.Truth.assertThat
import io.element.android.features.verifysession.impl.ui.aEmojisSessionVerificationData
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.dateformatter.test.A_FORMATTED_DATE
import io.element.android.libraries.dateformatter.test.FakeLastMessageTimestampFormatter
import io.element.android.libraries.matrix.api.core.FlowId
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.VerificationFlowState
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_TIMESTAMP
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class IncomingVerificationPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - nominal case - incoming verification successful`() = runTest {
        val acknowledgeVerificationRequestLambda = lambdaRecorder<SessionVerificationRequestDetails, Unit> { _ -> }
        val acceptVerificationRequestLambda = lambdaRecorder<Unit> { }
        val approveVerificationLambda = lambdaRecorder<Unit> { }
        val resetLambda = lambdaRecorder<Boolean, Unit> { }
        val fakeSessionVerificationService = FakeSessionVerificationService(
            acknowledgeVerificationRequestLambda = acknowledgeVerificationRequestLambda,
            acceptVerificationRequestLambda = acceptVerificationRequestLambda,
            approveVerificationLambda = approveVerificationLambda,
            resetLambda = resetLambda,
        )
        createPresenter(
            service = fakeSessionVerificationService,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.step).isEqualTo(
                IncomingVerificationState.Step.Initial(
                    deviceDisplayName = "a device name",
                    deviceId = A_DEVICE_ID,
                    formattedSignInTime = A_FORMATTED_DATE,
                    isWaiting = false,
                )
            )
            resetLambda.assertions().isCalledOnce().with(value(false))
            acknowledgeVerificationRequestLambda.assertions().isCalledOnce().with(value(aSessionVerificationRequestDetails))
            acceptVerificationRequestLambda.assertions().isNeverCalled()
            // User accept the incoming verification
            initialState.eventSink(IncomingVerificationViewEvents.StartVerification)
            skipItems(1)
            val initialWaitingState = awaitItem()
            assertThat((initialWaitingState.step as IncomingVerificationState.Step.Initial).isWaiting).isTrue()
            advanceUntilIdle()
            acceptVerificationRequestLambda.assertions().isCalledOnce()
            // Remote sent the data
            fakeSessionVerificationService.emitVerificationFlowState(VerificationFlowState.DidAcceptVerificationRequest)
            fakeSessionVerificationService.emitVerificationFlowState(VerificationFlowState.DidStartSasVerification)
            fakeSessionVerificationService.emitVerificationFlowState(
                VerificationFlowState.DidReceiveVerificationData(
                    data = aEmojisSessionVerificationData()
                )
            )
            val emojiState = awaitItem()
            assertThat(emojiState.step).isEqualTo(
                IncomingVerificationState.Step.Verifying(
                    data = aEmojisSessionVerificationData(),
                    isWaiting = false
                )
            )
            // User claims that the emoji matches
            emojiState.eventSink(IncomingVerificationViewEvents.ConfirmVerification)
            val emojiWaitingItem = awaitItem()
            assertThat((emojiWaitingItem.step as IncomingVerificationState.Step.Verifying).isWaiting).isTrue()
            approveVerificationLambda.assertions().isCalledOnce()
            // Remote confirm that the emojis match
            fakeSessionVerificationService.emitVerificationFlowState(
                VerificationFlowState.DidFinish
            )
            val finalItem = awaitItem()
            assertThat(finalItem.step).isEqualTo(IncomingVerificationState.Step.Completed)
        }
    }

    @Test
    fun `present - emoji not matching case - incoming verification failure`() = runTest {
        val acknowledgeVerificationRequestLambda = lambdaRecorder<SessionVerificationRequestDetails, Unit> { _ -> }
        val acceptVerificationRequestLambda = lambdaRecorder<Unit> { }
        val declineVerificationLambda = lambdaRecorder<Unit> { }
        val resetLambda = lambdaRecorder<Boolean, Unit> { }
        val fakeSessionVerificationService = FakeSessionVerificationService(
            acknowledgeVerificationRequestLambda = acknowledgeVerificationRequestLambda,
            acceptVerificationRequestLambda = acceptVerificationRequestLambda,
            declineVerificationLambda = declineVerificationLambda,
            resetLambda = resetLambda,
        )
        createPresenter(
            service = fakeSessionVerificationService,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.step).isEqualTo(
                IncomingVerificationState.Step.Initial(
                    deviceDisplayName = "a device name",
                    deviceId = A_DEVICE_ID,
                    formattedSignInTime = A_FORMATTED_DATE,
                    isWaiting = false,
                )
            )
            resetLambda.assertions().isCalledOnce().with(value(false))
            acknowledgeVerificationRequestLambda.assertions().isCalledOnce().with(value(aSessionVerificationRequestDetails))
            acceptVerificationRequestLambda.assertions().isNeverCalled()
            // User accept the incoming verification
            initialState.eventSink(IncomingVerificationViewEvents.StartVerification)
            skipItems(1)
            val initialWaitingState = awaitItem()
            assertThat((initialWaitingState.step as IncomingVerificationState.Step.Initial).isWaiting).isTrue()
            advanceUntilIdle()
            acceptVerificationRequestLambda.assertions().isCalledOnce()
            // Remote sent the data
            fakeSessionVerificationService.emitVerificationFlowState(VerificationFlowState.DidAcceptVerificationRequest)
            fakeSessionVerificationService.emitVerificationFlowState(VerificationFlowState.DidStartSasVerification)
            fakeSessionVerificationService.emitVerificationFlowState(
                VerificationFlowState.DidReceiveVerificationData(
                    data = aEmojisSessionVerificationData()
                )
            )
            val emojiState = awaitItem()
            // User claims that the emojis do not match
            emojiState.eventSink(IncomingVerificationViewEvents.DeclineVerification)
            val emojiWaitingItem = awaitItem()
            assertThat((emojiWaitingItem.step as IncomingVerificationState.Step.Verifying).isWaiting).isTrue()
            declineVerificationLambda.assertions().isCalledOnce()
            // Remote confirm that there is a failure
            fakeSessionVerificationService.emitVerificationFlowState(
                VerificationFlowState.DidFail
            )
            val finalItem = awaitItem()
            assertThat(finalItem.step).isEqualTo(IncomingVerificationState.Step.Failure)
        }
    }

    @Test
    fun `present - incoming verification is remotely canceled`() = runTest {
        val acknowledgeVerificationRequestLambda = lambdaRecorder<SessionVerificationRequestDetails, Unit> { _ -> }
        val acceptVerificationRequestLambda = lambdaRecorder<Unit> { }
        val declineVerificationLambda = lambdaRecorder<Unit> { }
        val resetLambda = lambdaRecorder<Boolean, Unit> { }
        val onFinishLambda = lambdaRecorder<Unit> { }
        val fakeSessionVerificationService = FakeSessionVerificationService(
            acknowledgeVerificationRequestLambda = acknowledgeVerificationRequestLambda,
            acceptVerificationRequestLambda = acceptVerificationRequestLambda,
            declineVerificationLambda = declineVerificationLambda,
            resetLambda = resetLambda,
        )
        createPresenter(
            service = fakeSessionVerificationService,
            navigator = IncomingVerificationNavigator(onFinishLambda),
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.step).isEqualTo(
                IncomingVerificationState.Step.Initial(
                    deviceDisplayName = "a device name",
                    deviceId = A_DEVICE_ID,
                    formattedSignInTime = A_FORMATTED_DATE,
                    isWaiting = false,
                )
            )
            // Remote cancel the verification request
            fakeSessionVerificationService.emitVerificationFlowState(VerificationFlowState.DidCancel)
            // The screen is dismissed
            skipItems(2)
            onFinishLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - user goes back when comparing emoji - incoming verification failure`() = runTest {
        val acknowledgeVerificationRequestLambda = lambdaRecorder<SessionVerificationRequestDetails, Unit> { _ -> }
        val acceptVerificationRequestLambda = lambdaRecorder<Unit> { }
        val declineVerificationLambda = lambdaRecorder<Unit> { }
        val resetLambda = lambdaRecorder<Boolean, Unit> { }
        val fakeSessionVerificationService = FakeSessionVerificationService(
            acknowledgeVerificationRequestLambda = acknowledgeVerificationRequestLambda,
            acceptVerificationRequestLambda = acceptVerificationRequestLambda,
            declineVerificationLambda = declineVerificationLambda,
            resetLambda = resetLambda,
        )
        createPresenter(
            service = fakeSessionVerificationService,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.step).isEqualTo(
                IncomingVerificationState.Step.Initial(
                    deviceDisplayName = "a device name",
                    deviceId = A_DEVICE_ID,
                    formattedSignInTime = A_FORMATTED_DATE,
                    isWaiting = false,
                )
            )
            resetLambda.assertions().isCalledOnce().with(value(false))
            acknowledgeVerificationRequestLambda.assertions().isCalledOnce().with(value(aSessionVerificationRequestDetails))
            acceptVerificationRequestLambda.assertions().isNeverCalled()
            // User accept the incoming verification
            initialState.eventSink(IncomingVerificationViewEvents.StartVerification)
            skipItems(1)
            val initialWaitingState = awaitItem()
            assertThat((initialWaitingState.step as IncomingVerificationState.Step.Initial).isWaiting).isTrue()
            advanceUntilIdle()
            acceptVerificationRequestLambda.assertions().isCalledOnce()
            // Remote sent the data
            fakeSessionVerificationService.emitVerificationFlowState(VerificationFlowState.DidAcceptVerificationRequest)
            fakeSessionVerificationService.emitVerificationFlowState(VerificationFlowState.DidStartSasVerification)
            fakeSessionVerificationService.emitVerificationFlowState(
                VerificationFlowState.DidReceiveVerificationData(
                    data = aEmojisSessionVerificationData()
                )
            )
            val emojiState = awaitItem()
            // User goes back
            emojiState.eventSink(IncomingVerificationViewEvents.GoBack)
            val emojiWaitingItem = awaitItem()
            assertThat((emojiWaitingItem.step as IncomingVerificationState.Step.Verifying).isWaiting).isTrue()
            declineVerificationLambda.assertions().isCalledOnce()
            // Remote confirm that there is a failure
            fakeSessionVerificationService.emitVerificationFlowState(
                VerificationFlowState.DidFail
            )
            val finalItem = awaitItem()
            assertThat(finalItem.step).isEqualTo(IncomingVerificationState.Step.Failure)
        }
    }

    @Test
    fun `present - user ignores incoming request`() = runTest {
        val acknowledgeVerificationRequestLambda = lambdaRecorder<SessionVerificationRequestDetails, Unit> { _ -> }
        val acceptVerificationRequestLambda = lambdaRecorder<Unit> { }
        val resetLambda = lambdaRecorder<Boolean, Unit> { }
        val fakeSessionVerificationService = FakeSessionVerificationService(
            acknowledgeVerificationRequestLambda = acknowledgeVerificationRequestLambda,
            acceptVerificationRequestLambda = acceptVerificationRequestLambda,
            resetLambda = resetLambda,
        )
        val navigatorLambda = lambdaRecorder<Unit> { }
        createPresenter(
            service = fakeSessionVerificationService,
            navigator = IncomingVerificationNavigator(navigatorLambda),
        ).test {
            val initialState = awaitItem()
            initialState.eventSink(IncomingVerificationViewEvents.IgnoreVerification)
            skipItems(1)
            navigatorLambda.assertions().isCalledOnce()
        }
    }

    private val aSessionVerificationRequestDetails = SessionVerificationRequestDetails(
        senderId = A_USER_ID,
        flowId = FlowId("flowId"),
        deviceId = A_DEVICE_ID,
        displayName = "a device name",
        firstSeenTimestamp = A_TIMESTAMP,
    )

    private fun createPresenter(
        sessionVerificationRequestDetails: SessionVerificationRequestDetails = aSessionVerificationRequestDetails,
        navigator: IncomingVerificationNavigator = IncomingVerificationNavigator { lambdaError() },
        service: SessionVerificationService = FakeSessionVerificationService(),
        dateFormatter: LastMessageTimestampFormatter = FakeLastMessageTimestampFormatter(A_FORMATTED_DATE),
    ) = IncomingVerificationPresenter(
        sessionVerificationRequestDetails = sessionVerificationRequestDetails,
        navigator = navigator,
        sessionVerificationService = service,
        stateMachine = IncomingVerificationStateMachine(service),
        dateFormatter = dateFormatter,
    )
}
