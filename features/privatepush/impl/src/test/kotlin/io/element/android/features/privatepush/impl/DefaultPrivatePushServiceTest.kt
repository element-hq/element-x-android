/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.FeralPushConfig
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.features.privatepush.impl.system.FakeInstalledAppsDetector
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.feral.DefaultFeralPushFallback
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushproviders.test.aSessionPushConfig
import io.element.android.libraries.sessionstorage.test.InMemorySessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPrivatePushServiceTest {
    private val ntfy = Distributor(PrivatePushConfig.NTFY_PACKAGE, "ntfy")

    /**
     * [builtInAvailable] adds the built-in Feral provider to the available ones (it always is in the fdroid
     * build); [provider] is the current (stored) one.
     */
    private fun TestScope.createService(
        ntfyInstalled: Boolean = true,
        provider: PushProvider? = null,
        builtInAvailable: Boolean = false,
        pushService: FakePushService = FakePushService(
            availablePushProviders = listOfNotNull(aBuiltInProvider().takeIf { builtInAvailable }, provider),
            currentPushProvider = { provider },
        ),
    ) = DefaultPrivatePushService(
        pushService = pushService,
        installedAppsDetector = FakeInstalledAppsDetector(
            if (ntfyInstalled) mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L) else mutableMapOf()
        ),
        feralPushFallback = DefaultFeralPushFallback(
            pushService = pushService,
            sessionStore = InMemorySessionStore(),
            matrixClientProvider = FakeMatrixClientProvider(),
            coroutineScope = backgroundScope,
        ),
        preferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
    )

    private fun aBuiltInProvider(registered: Boolean = true) = FakePushProvider(
        name = FeralPushConfig.NAME,
        distributors = listOf(Distributor(FeralPushConfig.DISTRIBUTOR_VALUE, FeralPushConfig.DISTRIBUTOR_NAME)),
        config = if (registered) aSessionPushConfig(url = FeralPushConfig.GATEWAY_URL, pushKey = "https://ntfy.feralisme.fr/upabc?up=1") else null,
    )

    private fun aProvider(distributor: Distributor? = ntfy, endpoint: String?) = FakePushProvider(
        name = "UnifiedPush",
        supportMultipleDistributors = true,
        distributors = listOfNotNull(distributor),
        currentDistributor = { distributor },
        config = endpoint?.let { aSessionPushConfig(url = "https://ntfy.feralisme.fr/_matrix/push/v1/notify", pushKey = it) },
    )

    @Test
    fun `status is NtfyNotInstalled when the helper app is missing`() = runTest {
        val service = createService(ntfyInstalled = false, provider = aProvider(endpoint = "https://ntfy.feralisme.fr/upabc?up=1"))
        assertThat(service.status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NtfyNotInstalled))
    }

    @Test
    fun `status is NotConnected without provider, distributor or endpoint`() = runTest {
        assertThat(createService(provider = null).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected))
        assertThat(createService(provider = aProvider(distributor = null, endpoint = "https://ntfy.feralisme.fr/up1?up=1")).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected))
        assertThat(createService(provider = aProvider(endpoint = null)).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected))
    }

    @Test
    fun `status is Private only when the endpoint host is the Feral server`() = runTest {
        assertThat(createService(provider = aProvider(endpoint = "https://NTFY.feralisme.fr/upabc?up=1")).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.Private)
        assertThat(createService(provider = aProvider(endpoint = "https://ntfy.sh/upabc?up=1")).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.PublicServer("ntfy.sh"))
        assertThat(createService(provider = aProvider(endpoint = "not a url")).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NotConnected))
    }

    @Test
    fun `status is BuiltIn when the Feral provider is registered, whether ntfy is installed or not`() = runTest {
        assertThat(createService(ntfyInstalled = false, provider = aBuiltInProvider()).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.BuiltIn)
        assertThat(createService(ntfyInstalled = true, provider = aBuiltInProvider()).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.BuiltIn)
        // Selected but not registered yet: not BuiltIn.
        assertThat(createService(ntfyInstalled = false, provider = aBuiltInProvider(registered = false)).status(A_SESSION_ID))
            .isEqualTo(PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NtfyNotInstalled))
    }

    @Test
    fun `fallBackToBuiltIn registers the built-in provider when the stored one has no distributor`() = runTest {
        // Stored = UnifiedPush, ntfy uninstalled: no distributor, NoDistributorsAvailable upstream.
        val unifiedPush = aProvider(distributor = null, endpoint = null)
        val builtIn = aBuiltInProvider()
        val registrations = mutableListOf<Triple<MatrixClient, PushProvider, Distributor>>()
        val pushService = FakePushService(
            availablePushProviders = listOf(builtIn, unifiedPush),
            currentPushProvider = { unifiedPush },
            registerWithLambda = { client, provider, distributor -> registrations += Triple(client, provider, distributor); Result.success(Unit) },
        )
        val service = createService(ntfyInstalled = false, pushService = pushService)
        assertThat(service.fallBackToBuiltIn(FakeMatrixClient())).isTrue()
        assertThat(registrations).hasSize(1)
        assertThat(registrations.single().second).isSameInstanceAs(builtIn)
        assertThat(registrations.single().third).isEqualTo(Distributor(FeralPushConfig.DISTRIBUTOR_VALUE, FeralPushConfig.DISTRIBUTOR_NAME))
        // The built-in provider is now the current one.
        assertThat(service.status(A_SESSION_ID)).isEqualTo(PrivatePushStatus.BuiltIn)
    }

    @Test
    fun `fallBackToBuiltIn returns false when the registration fails, the session stays as it was`() = runTest {
        val unifiedPush = aProvider(distributor = null, endpoint = null)
        val pushService = FakePushService(
            availablePushProviders = listOf(aBuiltInProvider(), unifiedPush),
            currentPushProvider = { unifiedPush },
            registerWithLambda = { _, _, _ -> Result.failure(IllegalStateException("network")) },
        )
        val service = createService(ntfyInstalled = false, pushService = pushService)
        assertThat(service.fallBackToBuiltIn(FakeMatrixClient())).isFalse()
        assertThat(service.status(A_SESSION_ID)).isEqualTo(PrivatePushStatus.NotSetUp(PrivatePushStatus.NotSetUp.Reason.NtfyNotInstalled))
    }

    @Test
    fun `fallBackToBuiltIn returns false without a built-in provider`() = runTest {
        val unifiedPush = aProvider(distributor = null, endpoint = null)
        val pushService = FakePushService(
            availablePushProviders = listOf(unifiedPush),
            currentPushProvider = { unifiedPush },
            registerWithLambda = { _, _, _ -> error("must not register") },
        )
        assertThat(createService(ntfyInstalled = false, pushService = pushService).fallBackToBuiltIn(FakeMatrixClient())).isFalse()
    }

    @Test
    fun `dismissed flag is persisted per session`() = runTest {
        val service = createService(ntfyInstalled = false)
        assertThat(service.isDismissed(A_SESSION_ID).first()).isFalse()
        service.setDismissed(A_SESSION_ID, true)
        assertThat(service.isDismissed(A_SESSION_ID).first()).isTrue()
        service.setDismissed(A_SESSION_ID, false)
        assertThat(service.isDismissed(A_SESSION_ID).first()).isFalse()
    }

    @Test
    fun `requestSetup and clearSetupRequest toggle setupRequested per session`() = runTest {
        val service = createService(ntfyInstalled = false)
        assertThat(service.setupRequested(A_SESSION_ID).first()).isFalse()
        service.requestSetup(A_SESSION_ID)
        assertThat(service.setupRequested(A_SESSION_ID).first()).isTrue()
        service.clearSetupRequest(A_SESSION_ID)
        assertThat(service.setupRequested(A_SESSION_ID).first()).isFalse()
    }
}
