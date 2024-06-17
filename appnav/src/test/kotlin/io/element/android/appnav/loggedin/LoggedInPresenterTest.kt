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

package io.element.android.appnav.loggedin

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.CryptoSessionStateChange
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LoggedInPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createLoggedInPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showSyncSpinner).isFalse()
            assertThat(initialState.pusherRegistrationState.isUninitialized()).isTrue()
            assertThat(initialState.ignoreRegistrationError).isFalse()
            skipItems(1)
        }
    }

    @Test
    fun `present - show sync spinner`() = runTest {
        val roomListService = FakeRoomListService()
        val presenter = createLoggedInPresenter(roomListService, NetworkStatus.Online)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.showSyncSpinner).isFalse()
            roomListService.postSyncIndicator(RoomListService.SyncIndicator.Show)
            consumeItemsUntilPredicate { it.showSyncSpinner }
            roomListService.postSyncIndicator(RoomListService.SyncIndicator.Hide)
            consumeItemsUntilPredicate { !it.showSyncSpinner }
        }
    }

    @Test
    fun `present - report crypto status analytics`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val roomListService = FakeRoomListService()
        val verificationService = FakeSessionVerificationService()
        val encryptionService = FakeEncryptionService()
        val presenter = LoggedInPresenter(
            matrixClient = FakeMatrixClient(roomListService = roomListService, encryptionService = encryptionService),
            networkMonitor = FakeNetworkMonitor(NetworkStatus.Online),
            pushService = FakePushService(),
            sessionVerificationService = verificationService,
            analyticsService = analyticsService,
            encryptionService = encryptionService
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            encryptionService.emitRecoveryState(RecoveryState.UNKNOWN)
            encryptionService.emitRecoveryState(RecoveryState.INCOMPLETE)
            verificationService.emitVerifiedStatus(SessionVerifiedStatus.Verified)

            skipItems(6)

            assertThat(analyticsService.capturedEvents.size).isEqualTo(1)
            assertThat(analyticsService.capturedEvents[0]).isInstanceOf(CryptoSessionStateChange::class.java)

            assertThat(analyticsService.capturedUserProperties.size).isEqualTo(1)
            assertThat(analyticsService.capturedUserProperties[0].recoveryState).isEqualTo(UserProperties.RecoveryState.Incomplete)
            assertThat(analyticsService.capturedUserProperties[0].verificationState).isEqualTo(UserProperties.VerificationState.Verified)

            // ensure a sync status change does not trigger a new capture
            roomListService.postSyncIndicator(RoomListService.SyncIndicator.Show)
            skipItems(1)
            assertThat(analyticsService.capturedEvents.size).isEqualTo(1)
        }
    }

    @Test
    fun `present - ensure default pusher is not registered if session is not verified`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val pushService = createFakePushService(registerWithLambda = lambda)
        val presenter = createLoggedInPresenter(pushService = pushService)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.errorOrNull())
                .isInstanceOf(PusherRegistrationFailure.AccountNotVerified::class.java)
            lambda.assertions()
                .isNeverCalled()
        }
    }

    @Test
    fun `present - ensure default pusher is registered with default provider`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val pushService = createFakePushService(
            registerWithLambda = lambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.isSuccess()).isTrue()
            lambda.assertions()
                .isCalledOnce()
                .with(
                    // MatrixClient
                    any(),
                    // PushProvider with highest priority (lower index)
                    value(pushService.getAvailablePushProviders()[0]),
                    // First distributor
                    value(pushService.getAvailablePushProviders()[0].getDistributors()[0]),
                )
        }
    }

    @Test
    fun `present - ensure default pusher is registered with default provider - fail to register`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.failure(AN_EXCEPTION)
        }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val pushService = createFakePushService(
            registerWithLambda = lambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.isFailure()).isTrue()
            lambda.assertions()
                .isCalledOnce()
                .with(
                    // MatrixClient
                    any(),
                    // PushProvider with highest priority (lower index)
                    value(pushService.getAvailablePushProviders()[0]),
                    // First distributor
                    value(pushService.getAvailablePushProviders()[0].getDistributors()[0]),
                )
        }
    }

    @Test
    fun `present - ensure current provider is registered with current distributor`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val distributor = Distributor("aDistributorValue1", "aDistributorName1")
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = listOf(
                Distributor("aDistributorValue0", "aDistributorName0"),
                distributor,
            ),
            currentDistributor = { distributor },
        )
        val pushService = createFakePushService(
            pushProvider1 = pushProvider,
            currentPushProvider = { pushProvider },
            registerWithLambda = lambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.isSuccess()).isTrue()
            lambda.assertions()
                .isCalledOnce()
                .with(
                    // MatrixClient
                    any(),
                    // Current push provider
                    value(pushProvider),
                    // Current distributor
                    value(distributor),
                )
        }
    }

    @Test
    fun `present - if current push provider does not have current distributor, the first one is used`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = listOf(
                Distributor("aDistributorValue0", "aDistributorName0"),
                Distributor("aDistributorValue1", "aDistributorName1"),
            ),
            currentDistributor = { null },
        )
        val pushService = createFakePushService(
            pushProvider0 = pushProvider,
            currentPushProvider = { pushProvider },
            registerWithLambda = lambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.isSuccess()).isTrue()
            lambda.assertions()
                .isCalledOnce()
                .with(
                    // MatrixClient
                    any(),
                    // PushProvider with highest priority (lower index)
                    value(pushService.getAvailablePushProviders()[0]),
                    // First distributor
                    value(pushService.getAvailablePushProviders()[0].getDistributors()[0]),
                )
        }
    }

    @Test
    fun `present - if current push provider does not have distributors, nothing happen`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = emptyList(),
        )
        val pushService = createFakePushService(
            pushProvider0 = pushProvider,
            currentPushProvider = { pushProvider },
            registerWithLambda = lambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.errorOrNull())
                .isInstanceOf(PusherRegistrationFailure.NoDistributorsAvailable::class.java)
            lambda.assertions()
                .isNeverCalled()
        }
    }

    @Test
    fun `present - case no push provider available provider`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val pushService = createFakePushService(
            pushProvider0 = null,
            pushProvider1 = null,
            registerWithLambda = lambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.errorOrNull())
                .isInstanceOf(PusherRegistrationFailure.NoProvidersAvailable::class.java)
            lambda.assertions()
                .isNeverCalled()
            // Reset the error and do not show again
            finalState.eventSink(LoggedInEvents.CloseErrorDialog(doNotShowAgain = true))
            skipItems(1)
            val lastState = awaitItem()
            assertThat(lastState.pusherRegistrationState.isUninitialized()).isTrue()
            assertThat(lastState.ignoreRegistrationError).isTrue()
        }
    }

    @Test
    fun `present - case one push provider but no distributor available`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val selectPushProviderLambda = lambdaRecorder<MatrixClient, PushProvider, Unit> { _, _ -> }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val pushProvider = FakePushProvider(
            index = 0,
            name = "aFakePushProvider",
            distributors = emptyList(),
        )
        val pushService = createFakePushService(
            pushProvider0 = pushProvider,
            pushProvider1 = null,
            registerWithLambda = lambda,
            selectPushProviderLambda = selectPushProviderLambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.errorOrNull())
                .isInstanceOf(PusherRegistrationFailure.NoDistributorsAvailable::class.java)
            lambda.assertions()
                .isNeverCalled()
            selectPushProviderLambda.assertions()
                .isCalledOnce()
                .with(
                    // MatrixClient
                    any(),
                    // PushProvider
                    value(pushProvider),
                )
            // Reset the error
            finalState.eventSink(LoggedInEvents.CloseErrorDialog(doNotShowAgain = false))
            val lastState = awaitItem()
            assertThat(lastState.pusherRegistrationState.isUninitialized()).isTrue()
        }
    }

    @Test
    fun `present - case two push providers but first one does not have distributor - second one will be used`() = runTest {
        val lambda = lambdaRecorder<MatrixClient, PushProvider, Distributor, Result<Unit>> { _, _, _ ->
            Result.success(Unit)
        }
        val sessionVerificationService = FakeSessionVerificationService()
        sessionVerificationService.givenVerifiedStatus(SessionVerifiedStatus.Verified)
        val pushProvider0 = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = emptyList(),
        )
        val distributor = Distributor("aDistributorValue1", "aDistributorName1")
        val pushProvider1 = FakePushProvider(
            index = 1,
            name = "aFakePushProvider1",
            distributors = listOf(distributor),
        )
        val pushService = createFakePushService(
            pushProvider0 = pushProvider0,
            pushProvider1 = pushProvider1,
            registerWithLambda = lambda,
        )
        val presenter = createLoggedInPresenter(
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            skipItems(2)
            val finalState = awaitItem()
            assertThat(finalState.pusherRegistrationState.isSuccess()).isTrue()
            lambda.assertions().isCalledOnce()
                .with(
                    // MatrixClient
                    any(),
                    // PushProvider with the distributor
                    value(pushProvider1),
                    // First distributor of second push provider
                    value(distributor),
                )
        }
    }

    private fun createFakePushService(
        pushProvider0: PushProvider? = FakePushProvider(
            index = 0,
            name = "aFakePushProvider0",
            distributors = listOf(Distributor("aDistributorValue0", "aDistributorName0")),
            currentDistributor = { null },
        ),
        pushProvider1: PushProvider? = FakePushProvider(
            index = 1,
            name = "aFakePushProvider1",
            distributors = listOf(Distributor("aDistributorValue1", "aDistributorName1")),
            currentDistributor = { null },
        ),
        registerWithLambda: suspend (MatrixClient, PushProvider, Distributor) -> Result<Unit> = { _, _, _ ->
            Result.success(Unit)
        },
        selectPushProviderLambda: (MatrixClient, PushProvider) -> Unit = { _, _ -> lambdaError() },
        currentPushProvider: () -> PushProvider? = { null },
    ): PushService {
        return FakePushService(
            availablePushProviders = listOfNotNull(pushProvider0, pushProvider1),
            registerWithLambda = registerWithLambda,
            currentPushProvider = currentPushProvider,
            selectPushProviderLambda = selectPushProviderLambda,
        )
    }

    private fun createLoggedInPresenter(
        roomListService: RoomListService = FakeRoomListService(),
        networkStatus: NetworkStatus = NetworkStatus.Offline,
        analyticsService: AnalyticsService = FakeAnalyticsService(),
        sessionVerificationService: SessionVerificationService = FakeSessionVerificationService(),
        encryptionService: EncryptionService = FakeEncryptionService(),
        pushService: PushService = FakePushService(),
    ): LoggedInPresenter {
        return LoggedInPresenter(
            matrixClient = FakeMatrixClient(roomListService = roomListService),
            networkMonitor = FakeNetworkMonitor(networkStatus),
            pushService = pushService,
            sessionVerificationService = sessionVerificationService,
            analyticsService = analyticsService,
            encryptionService = encryptionService
        )
    }
}
