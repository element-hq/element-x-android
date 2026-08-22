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
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.push.test.FakePusherSubscriber
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushConfig
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FeralPushProviderTest {
    private val aTopic = "upfedcba9876543210fedcba9876543210"
    private val anEndpoint = "https://ntfy.feralisme.fr/$aTopic?up=1"
    private val aSecret = "aClientSecret"

    private class Fixture(
        val store: FakeFeralPushStore = FakeFeralPushStore(),
        val userPushStore: FakeUserPushStore = FakeUserPushStore(),
        val serviceController: FakeFeralPushServiceController = FakeFeralPushServiceController(),
    )

    private fun Fixture.createProvider(
        registerResult: (MatrixClient, String, String) -> Result<Unit> = { _, _, _ -> Result.success(Unit) },
        unregisterResult: (MatrixClient, String, String) -> Result<Unit> = { _, _, _ -> Result.success(Unit) },
        topic: String = aTopic,
    ) = FeralPushProvider(
        pusherSubscriber = FakePusherSubscriber(
            registerPusherResult = registerResult,
            unregisterPusherResult = unregisterResult,
        ),
        pushClientSecret = FakePushClientSecret(getSecretForUserResult = { aSecret }),
        feralPushStore = store,
        topicGenerator = { topic },
        userPushStoreFactory = FakeUserPushStoreFactory { userPushStore },
        serviceController = serviceController,
    )

    @Test
    fun `provider identity`() {
        val provider = Fixture().createProvider()
        assertThat(provider.index).isEqualTo(0)
        // DefaultPushService picks the first provider (by index) with a distributor for a fresh session:
        // the built-in one always has one, so it wins over UnifiedPush even when ntfy is installed.
        assertThat(provider.index).isLessThan(UnifiedPushConfig.INDEX)
        assertThat(provider.getDistributors()).isNotEmpty()
        assertThat(provider.name).isEqualTo("Feral")
        assertThat(provider.supportMultipleDistributors).isFalse()
        assertThat(provider.getDistributors()).containsExactly(FeralPushProvider.distributor)
        assertThat(provider.canRotateToken()).isFalse()
        assertThat(FeralPushProvider.endpointFor(aTopic)).isEqualTo(anEndpoint)
    }

    @Test
    fun `registerWith registers a pusher on the Feral gateway, persists and starts the service`() = runTest {
        val fixture = Fixture()
        val calls = mutableListOf<Pair<String, String>>()
        val provider = fixture.createProvider(
            registerResult = { _, pushKey, gateway -> calls += pushKey to gateway; Result.success(Unit) },
        )
        val result = provider.registerWith(FakeMatrixClient(), FeralPushProvider.distributor)
        assertThat(result.isSuccess).isTrue()
        assertThat(calls).containsExactly(anEndpoint to FeralPushConfig.GATEWAY_URL)
        assertThat(fixture.store.get(A_SESSION_ID)).isEqualTo(
            FeralPushRegistration(
                sessionId = A_SESSION_ID.value,
                topic = aTopic,
                endpoint = anEndpoint,
                clientSecret = aSecret,
                lastMessageId = null,
            )
        )
        assertThat(fixture.userPushStore.getCurrentRegisteredPushKey()).isEqualTo(anEndpoint)
        assertThat(fixture.serviceController.ensureStartedCalls).isEqualTo(1)
        assertThat(provider.getCurrentDistributorValue(A_SESSION_ID)).isEqualTo("feral.builtin")
        assertThat(provider.getCurrentDistributor(A_SESSION_ID)).isEqualTo(FeralPushProvider.distributor)
        assertThat(provider.getPushConfig(A_SESSION_ID)).isEqualTo(Config(url = FeralPushConfig.GATEWAY_URL, pushKey = anEndpoint))
    }

    @Test
    fun `registerWith keeps the existing topic and replay cursor`() = runTest {
        val fixture = Fixture(
            store = FakeFeralPushStore(listOf(aFeralPushRegistration(A_SESSION_ID, topic = aTopic, lastMessageId = "msg42"))),
        )
        val provider = fixture.createProvider(topic = "upanothertopic")
        assertThat(provider.registerWith(FakeMatrixClient(), FeralPushProvider.distributor).isSuccess).isTrue()
        val registration = fixture.store.get(A_SESSION_ID)!!
        assertThat(registration.topic).isEqualTo(aTopic)
        assertThat(registration.lastMessageId).isEqualTo("msg42")
        assertThat(registration.clientSecret).isEqualTo(aSecret)
    }

    @Test
    fun `registerWith failure stores nothing and does not start the service`() = runTest {
        val fixture = Fixture()
        val provider = fixture.createProvider(registerResult = { _, _, _ -> Result.failure(IllegalStateException("boom")) })
        val result = provider.registerWith(FakeMatrixClient(), FeralPushProvider.distributor)
        assertThat(result.isFailure).isTrue()
        assertThat(fixture.store.get(A_SESSION_ID)).isNull()
        assertThat(fixture.userPushStore.getCurrentRegisteredPushKey()).isNull()
        assertThat(fixture.serviceController.ensureStartedCalls).isEqualTo(0)
        assertThat(provider.getCurrentDistributor(A_SESSION_ID)).isNull()
        assertThat(provider.getCurrentDistributorValue(A_SESSION_ID)).isNull()
        assertThat(provider.getPushConfig(A_SESSION_ID)).isNull()
    }

    @Test
    fun `unregister removes the pusher, the registration and stops the service when none remains`() = runTest {
        val fixture = Fixture(store = FakeFeralPushStore(listOf(aFeralPushRegistration(A_SESSION_ID, topic = aTopic))))
        val calls = mutableListOf<Pair<String, String>>()
        val provider = fixture.createProvider(
            unregisterResult = { _, pushKey, gateway -> calls += pushKey to gateway; Result.success(Unit) },
        )
        assertThat(provider.unregister(FakeMatrixClient()).isSuccess).isTrue()
        assertThat(calls).containsExactly(anEndpoint to FeralPushConfig.GATEWAY_URL)
        assertThat(fixture.store.get(A_SESSION_ID)).isNull()
        assertThat(fixture.serviceController.stopCalls).isEqualTo(1)
    }

    @Test
    fun `unregister keeps the service when another session remains`() = runTest {
        val fixture = Fixture(
            store = FakeFeralPushStore(
                listOf(aFeralPushRegistration(A_SESSION_ID), aFeralPushRegistration(A_SESSION_ID_2, topic = "upother")),
            ),
        )
        val provider = fixture.createProvider()
        assertThat(provider.unregister(FakeMatrixClient(sessionId = A_SESSION_ID)).isSuccess).isTrue()
        assertThat(fixture.store.get(A_SESSION_ID)).isNull()
        assertThat(fixture.store.get(A_SESSION_ID_2)).isNotNull()
        assertThat(fixture.serviceController.stopCalls).isEqualTo(0)
    }

    @Test
    fun `unregister ignores a pusher that is already gone`() = runTest {
        val fixture = Fixture(store = FakeFeralPushStore(listOf(aFeralPushRegistration(A_SESSION_ID))))
        val provider = fixture.createProvider(
            unregisterResult = { _, _, _ ->
                Result.failure(ClientException.Generic(message = "M_NOT_FOUND: Pusher not found", details = null))
            },
        )
        assertThat(provider.unregister(FakeMatrixClient()).isSuccess).isTrue()
        assertThat(fixture.store.get(A_SESSION_ID)).isNull()
    }

    @Test
    fun `unregister failure keeps the registration`() = runTest {
        val fixture = Fixture(store = FakeFeralPushStore(listOf(aFeralPushRegistration(A_SESSION_ID))))
        val provider = fixture.createProvider(unregisterResult = { _, _, _ -> Result.failure(IllegalStateException("network")) })
        assertThat(provider.unregister(FakeMatrixClient()).isFailure).isTrue()
        assertThat(fixture.store.get(A_SESSION_ID)).isNotNull()
        assertThat(fixture.serviceController.stopCalls).isEqualTo(0)
    }

    @Test
    fun `unregister without registration succeeds and touches nothing`() = runTest {
        val fixture = Fixture()
        val provider = fixture.createProvider(unregisterResult = { _, _, _ -> error("must not be called") })
        assertThat(provider.unregister(FakeMatrixClient()).isSuccess).isTrue()
        assertThat(fixture.serviceController.stopCalls).isEqualTo(0)
    }

    @Test
    fun `onSessionDeleted clears the registration and stops the service`() = runTest {
        val fixture = Fixture(store = FakeFeralPushStore(listOf(aFeralPushRegistration(A_SESSION_ID))))
        val provider = fixture.createProvider()
        provider.onSessionDeleted(A_SESSION_ID)
        assertThat(fixture.store.get(A_SESSION_ID)).isNull()
        assertThat(fixture.serviceController.stopCalls).isEqualTo(1)
    }
}
