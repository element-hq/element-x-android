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
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.encryption.FakeEncryptionService
import io.element.android.libraries.matrix.test.roomlist.FakeRoomListService
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.push.test.FakePushService
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.consumeItemsUntilPredicate
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
        val verificationService = FakeSessionVerificationService()
        val encryptionService = FakeEncryptionService()
        val presenter = LoggedInPresenter(
            matrixClient = FakeMatrixClient(encryptionService = encryptionService),
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

            // Should only capture once (not report while checking state -like unknown-)
            consumeItemsUntilPredicate {
                analyticsService.capturedEvents.size == 1 &&
                    analyticsService.capturedEvents[0] is CryptoSessionStateChange
            }
            consumeItemsUntilPredicate {
                analyticsService.capturedUserProperties.size == 1 &&
                    analyticsService.capturedUserProperties[0].recoveryState == UserProperties.RecoveryState.Incomplete &&
                    analyticsService.capturedUserProperties[0].verificationState == UserProperties.VerificationState.Verified
            }
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun createLoggedInPresenter(
        roomListService: RoomListService = FakeRoomListService(),
        networkStatus: NetworkStatus = NetworkStatus.Offline,
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
        encryptionService: FakeEncryptionService = FakeEncryptionService(),
    ): LoggedInPresenter {
        return LoggedInPresenter(
            matrixClient = FakeMatrixClient(roomListService = roomListService),
            networkMonitor = FakeNetworkMonitor(networkStatus),
            pushService = FakePushService(),
            sessionVerificationService = FakeSessionVerificationService(),
            analyticsService = analyticsService,
            encryptionService = encryptionService
        )
    }
}
