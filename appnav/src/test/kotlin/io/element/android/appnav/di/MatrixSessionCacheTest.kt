/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.di

import com.bumble.appyx.core.state.MutableSavedStateMapImpl
import com.google.common.truth.Truth.assertThat
import io.element.android.features.networkmonitor.test.FakeNetworkMonitor
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MatrixSessionCacheTest {
    @Test
    fun `test getOrNull`() = runTest {
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = MatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory())
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test getSyncOrchestratorOrNull`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = MatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory(coroutineScope))

        // With no matrix client there is no sync orchestrator
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        assertThat(matrixSessionCache.getSyncOrchestrator(A_SESSION_ID)).isNull()

        // But as soon as we receive a client, we can get the sync orchestrator
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getSyncOrchestrator(A_SESSION_ID)).isNotNull()

        coroutineScope.cancel()
    }

    @Test
    fun `test getOrRestore`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = MatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory(coroutineScope))
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        // Do it again to hit the cache
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)

        coroutineScope.cancel()
    }

    @Test
    fun `test remove`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = MatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory(coroutineScope))
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
        // Remove
        matrixSessionCache.remove(A_SESSION_ID)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()

        coroutineScope.cancel()
    }

    @Test
    fun `test remove all`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = MatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory(coroutineScope))
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrRestore(A_SESSION_ID).getOrNull()).isEqualTo(fakeMatrixClient)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)
        // Remove all
        matrixSessionCache.removeAll()
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()

        coroutineScope.cancel()
    }

    @Test
    fun `test save and restore`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = MatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory(coroutineScope))
        val fakeMatrixClient = FakeMatrixClient()
        fakeAuthenticationService.givenMatrixClient(fakeMatrixClient)
        matrixSessionCache.getOrRestore(A_SESSION_ID)
        val savedStateMap = MutableSavedStateMapImpl { true }
        matrixSessionCache.saveIntoSavedState(savedStateMap)
        assertThat(savedStateMap.size).isEqualTo(1)
        // Test Restore with non-empty map
        matrixSessionCache.restoreWithSavedState(savedStateMap)
        // Empty the map
        matrixSessionCache.removeAll()
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()
        // Restore again
        matrixSessionCache.restoreWithSavedState(savedStateMap)
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isEqualTo(fakeMatrixClient)

        coroutineScope.cancel()
    }

    @Test
    fun `test AuthenticationService listenToNewMatrixClients emits a Client value and we save it`() = runTest {
        val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
        val fakeAuthenticationService = FakeMatrixAuthenticationService()
        val matrixSessionCache = MatrixSessionCache(fakeAuthenticationService, createSyncOrchestratorFactory(coroutineScope))
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNull()

        fakeAuthenticationService.givenMatrixClient(FakeMatrixClient(sessionId = A_SESSION_ID))
        val loginSucceeded = fakeAuthenticationService.login("user", "pass")

        assertThat(loginSucceeded.isSuccess).isTrue()
        assertThat(matrixSessionCache.getOrNull(A_SESSION_ID)).isNotNull()

        coroutineScope.cancel()
    }

    private fun TestScope.createSyncOrchestratorFactory(
        baseCoroutineScope: CoroutineScope = this,
    ) = object : DefaultSyncOrchestrator.Factory {
        override fun create(matrixClient: MatrixClient): DefaultSyncOrchestrator {
            return DefaultSyncOrchestrator(
                matrixClient,
                sessionCoroutineScope = baseCoroutineScope,
                appForegroundStateService = FakeAppForegroundStateService(),
                networkMonitor = FakeNetworkMonitor(),
                dispatchers = testCoroutineDispatchers(),
            )
        }
    }
}
