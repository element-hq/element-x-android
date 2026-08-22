/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.PrivatePushConfig
import io.element.android.features.privatepush.api.PrivatePushStatus
import io.element.android.features.privatepush.impl.system.FakeInstalledAppsDetector
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import io.element.android.libraries.push.test.FakePushService
import io.element.android.libraries.pushproviders.api.Distributor
import io.element.android.libraries.pushproviders.api.PushProvider
import io.element.android.libraries.pushproviders.test.FakePushProvider
import io.element.android.libraries.pushproviders.test.aSessionPushConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultPrivatePushServiceTest {
    private val ntfy = Distributor(PrivatePushConfig.NTFY_PACKAGE, "ntfy")

    private fun createService(
        ntfyInstalled: Boolean = true,
        provider: PushProvider? = null,
    ) = DefaultPrivatePushService(
        pushService = FakePushService(
            availablePushProviders = listOfNotNull(provider),
            currentPushProvider = { provider },
        ),
        installedAppsDetector = FakeInstalledAppsDetector(
            if (ntfyInstalled) mutableMapOf(PrivatePushConfig.NTFY_PACKAGE to 63L) else mutableMapOf()
        ),
        preferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
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
    fun `shouldShowSetup is false when private or dismissed, true otherwise`() = runTest {
        val private = createService(provider = aProvider(endpoint = "https://ntfy.feralisme.fr/upabc?up=1"))
        assertThat(private.shouldShowSetup(A_SESSION_ID)).isFalse()

        val notSetUp = createService(ntfyInstalled = false)
        assertThat(notSetUp.shouldShowSetup(A_SESSION_ID)).isTrue()
        notSetUp.setDismissed(A_SESSION_ID, true)
        assertThat(notSetUp.isDismissed(A_SESSION_ID).first()).isTrue()
        assertThat(notSetUp.shouldShowSetup(A_SESSION_ID)).isFalse()
        notSetUp.setDismissed(A_SESSION_ID, false)
        assertThat(notSetUp.shouldShowSetup(A_SESSION_ID)).isTrue()
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
