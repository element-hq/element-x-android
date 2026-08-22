/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.feral

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.FeralPushConfig
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.verification.FakeSessionVerificationService
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushproviders.test.aSessionPushConfig
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import io.element.android.libraries.sessionstorage.test.aSessionData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeralPushFallbackTest {
    private val builtIn = FakePushProvider(
        index = 0,
        name = FeralPushConfig.NAME,
        distributors = listOf(FeralPushProvider.distributor),
        config = aSessionPushConfig(url = FeralPushConfig.GATEWAY_URL, pushKey = "https://ntfy.feralisme.fr/upabc?up=1"),
    )
    private val unifiedPushWithoutDistributor = FakePushProvider(index = 1, name = "UnifiedPush", distributors = emptyList())
    private val unifiedPushWithNtfy = FakePushProvider(index = 1, name = "UnifiedPush", distributors = listOf(Distributor("io.heckel.ntfy", "ntfy")))

    private class Registration(val provider: PushProvider, val distributor: Distributor, val time: Long)

    /** Run the background heal job (the idle-advance helper only drives foreground tasks, not backgroundScope ones). */
    private fun TestScope.settle() {
        advanceTimeBy(1000)
        runCurrent()
    }

    private fun TestScope.createFallback(
        current: PushProvider?,
        available: List<PushProvider> = listOf(builtIn, unifiedPushWithoutDistributor),
        registrations: MutableList<Registration> = mutableListOf(),
        registerResult: Result<Unit> = Result.success(Unit),
        sessionStore: SessionStore = InMemorySessionStore(listOf(aSessionData(sessionId = A_SESSION_ID.value))),
        matrixClient: MatrixClient = FakeMatrixClient(sessionVerificationService = FakeSessionVerificationService(SessionVerifiedStatus.Verified)),
    ): Pair<DefaultFeralPushFallback, FakePushService> {
        val pushService = FakePushService(
            availablePushProviders = available,
            currentPushProvider = { current },
            registerWithLambda = { _, provider, distributor ->
                registrations += Registration(provider, distributor, testScheduler.currentTime)
                registerResult
            },
        )
        val fallback = DefaultFeralPushFallback(
            pushService = pushService,
            sessionStore = sessionStore,
            matrixClientProvider = FakeMatrixClientProvider { Result.success(matrixClient) },
            coroutineScope = backgroundScope,
        )
        return fallback to pushService
    }

    @Test
    fun `register switches a session stuck on a provider without distributor to the built-in one`() = runTest {
        val registrations = mutableListOf<Registration>()
        val (fallback, _) = createFallback(current = unifiedPushWithoutDistributor, registrations = registrations)
        assertThat(fallback.register(FakeMatrixClient())).isTrue()
        assertThat(registrations).hasSize(1)
        assertThat(registrations.single().provider).isSameInstanceAs(builtIn)
        assertThat(registrations.single().distributor).isEqualTo(FeralPushProvider.distributor)
    }

    @Test
    fun `register is a no-op when the built-in provider is already registered`() = runTest {
        val registrations = mutableListOf<Registration>()
        val (fallback, _) = createFallback(current = builtIn, registrations = registrations)
        assertThat(fallback.register(FakeMatrixClient())).isTrue()
        assertThat(registrations).isEmpty()
    }

    @Test
    fun `register returns false when the registration fails or the built-in provider is missing`() = runTest {
        val (failing, _) = createFallback(current = unifiedPushWithoutDistributor, registerResult = Result.failure(IllegalStateException("network")))
        assertThat(failing.register(FakeMatrixClient())).isFalse()
        val registrations = mutableListOf<Registration>()
        val (missing, _) = createFallback(current = unifiedPushWithoutDistributor, available = listOf(unifiedPushWithoutDistributor), registrations = registrations)
        assertThat(missing.register(FakeMatrixClient())).isFalse()
        assertThat(registrations).isEmpty()
    }

    @Test
    fun `concurrent register calls are serialised and register only once`() = runTest {
        val registrations = mutableListOf<Registration>()
        val (fallback, _) = createFallback(current = unifiedPushWithoutDistributor, registrations = registrations)
        val first = async { fallback.register(FakeMatrixClient()) }
        val second = async { fallback.register(FakeMatrixClient()) }
        assertThat(first.await()).isTrue()
        assertThat(second.await()).isTrue()
        // The second caller waited for the first registration, saw the built-in provider registered and skipped.
        assertThat(registrations).hasSize(1)
    }

    @Test
    fun `healLatestSession falls back once per process, after the session is verified`() = runTest {
        val registrations = mutableListOf<Registration>()
        val verification = FakeSessionVerificationService(SessionVerifiedStatus.Unknown)
        val (fallback, pushService) = createFallback(
            current = unifiedPushWithoutDistributor,
            registrations = registrations,
            matrixClient = FakeMatrixClient(sessionVerificationService = verification),
        )
        fallback.healLatestSession()
        settle()
        // Not verified yet: nothing happens (no UI or FTUE involved, the job just waits).
        assertThat(registrations).isEmpty()
        verification.emitVerifiedStatus(SessionVerifiedStatus.Verified)
        settle()
        assertThat(registrations).hasSize(1)
        assertThat(pushService.getCurrentPushProvider(A_SESSION_ID)).isSameInstanceAs(builtIn)

        // Second app foreground in the same process: one attempt per session.
        fallback.healLatestSession()
        settle()
        assertThat(registrations).hasSize(1)
    }

    @Test
    fun `healLatestSession retries at the next foreground after a failed registration`() = runTest {
        val registrations = mutableListOf<Registration>()
        val (fallback, _) = createFallback(
            current = unifiedPushWithoutDistributor,
            registrations = registrations,
            registerResult = Result.failure(IllegalStateException("network")),
        )
        fallback.healLatestSession()
        settle()
        assertThat(registrations).hasSize(1)
        fallback.healLatestSession()
        settle()
        assertThat(registrations).hasSize(2)
    }

    @Test
    fun `healLatestSession leaves a working provider, the built-in one, or an unregistered session alone`() = runTest {
        listOf(unifiedPushWithNtfy, builtIn, null).forEach { current ->
            val registrations = mutableListOf<Registration>()
            val (fallback, _) = createFallback(current = current, available = listOf(builtIn, unifiedPushWithNtfy), registrations = registrations)
            fallback.healLatestSession()
            settle()
            assertThat(registrations).isEmpty()
        }
    }

    @Test
    fun `healLatestSession does nothing without a session or when it cannot be restored`() = runTest {
        val registrations = mutableListOf<Registration>()
        val (noSession, _) = createFallback(current = unifiedPushWithoutDistributor, registrations = registrations, sessionStore = InMemorySessionStore())
        noSession.healLatestSession()
        settle()
        assertThat(registrations).isEmpty()

        val pushService = FakePushService(
            availablePushProviders = listOf(builtIn, unifiedPushWithoutDistributor),
            currentPushProvider = { unifiedPushWithoutDistributor },
            registerWithLambda = { _, _, _ -> error("must not register") },
        )
        val cannotRestore = DefaultFeralPushFallback(
            pushService = pushService,
            sessionStore = InMemorySessionStore(listOf(aSessionData(sessionId = A_SESSION_ID.value))),
            matrixClientProvider = FakeMatrixClientProvider { Result.failure(IllegalStateException("no session")) },
            coroutineScope = backgroundScope,
        )
        cannotRestore.healLatestSession()
        settle()
    }
}
