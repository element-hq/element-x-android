/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.test.aCurrentUserPushConfig
import io.element.android.libraries.pushproviders.unifiedpush.FakeUnifiedPushApiFactory
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import io.element.android.libraries.pushproviders.unifiedpush.invalidDiscoveryResponse
import io.element.android.libraries.pushproviders.unifiedpush.matrixDiscoveryResponse
import io.element.android.libraries.pushproviders.unifiedpush.network.DiscoveryResponse
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import io.element.android.libraries.troubleshoot.api.test.TestFilterData
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UnifiedPushMatrixGatewayTestTest {
    @Test
    fun `test UnifiedPushMatrixGatewayTest success`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            currentUserPushConfig = aCurrentUserPushConfig(),
            discoveryResponse = matrixDiscoveryResponse,
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Success)
        }
    }

    @Test
    fun `test UnifiedPushMatrixGatewayTest no config found`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            currentUserPushConfig = null,
            discoveryResponse = matrixDiscoveryResponse,
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
        }
    }

    @Test
    fun `test UnifiedPushMatrixGatewayTest not valid gateway`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            currentUserPushConfig = aCurrentUserPushConfig(),
            discoveryResponse = invalidDiscoveryResponse,
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
            // Reset the error
            sut.reset()
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
        }
    }

    @Test
    fun `test UnifiedPushMatrixGatewayTest network error`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest(
            currentUserPushConfig = aCurrentUserPushConfig(),
            discoveryResponse = { error("Network error") },
        )
        launch {
            sut.run(this)
        }
        sut.state.test {
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.Idle(false))
            assertThat(awaitItem().status).isEqualTo(NotificationTroubleshootTestState.Status.InProgress)
            val lastItem = awaitItem()
            assertThat(lastItem.status).isEqualTo(NotificationTroubleshootTestState.Status.Failure(false))
        }
    }

    @Test
    fun `test isRelevant`() = runTest {
        val sut = createUnifiedPushMatrixGatewayTest()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = UnifiedPushConfig.NAME))).isTrue()
        assertThat(sut.isRelevant(TestFilterData(currentPushProviderName = "other"))).isFalse()
    }

    private fun TestScope.createUnifiedPushMatrixGatewayTest(
        currentUserPushConfig: CurrentUserPushConfig? = null,
        discoveryResponse: () -> DiscoveryResponse = matrixDiscoveryResponse,
    ): UnifiedPushMatrixGatewayTest {
        return UnifiedPushMatrixGatewayTest(
            unifiedPushApiFactory = FakeUnifiedPushApiFactory(discoveryResponse),
            coroutineDispatchers = testCoroutineDispatchers(),
            unifiedPushCurrentUserPushConfigProvider = FakeUnifiedPushCurrentUserPushConfigProvider(
                currentUserPushConfig = { currentUserPushConfig }
            ),
        )
    }
}
