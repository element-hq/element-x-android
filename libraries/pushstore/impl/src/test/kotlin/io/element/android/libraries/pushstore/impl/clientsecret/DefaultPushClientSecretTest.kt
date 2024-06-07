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

package io.element.android.libraries.pushstore.impl.clientsecret

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.InMemoryPushClientSecretStore
import io.element.android.libraries.sessionstorage.test.observer.NoOpSessionObserver
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val A_USER_ID_0 = SessionId("@A_USER_ID_0:domain")
private val A_USER_ID_1 = SessionId("@A_USER_ID_1:domain")

private const val A_UNKNOWN_SECRET = "A_UNKNOWN_SECRET"

internal class DefaultPushClientSecretTest {
    @Test
    fun test() = runTest {
        val factory = FakePushClientSecretFactory()
        val store = InMemoryPushClientSecretStore()
        val sut = DefaultPushClientSecret(factory, store, NoOpSessionObserver())

        val secret0 = factory.getSecretForUser(0)
        val secret1 = factory.getSecretForUser(1)
        val secret2 = factory.getSecretForUser(2)

        assertThat(store.getSecrets()).isEmpty()
        assertThat(sut.getUserIdFromSecret(secret0)).isNull()
        // Create a secret
        assertThat(sut.getSecretForUser(A_USER_ID_0)).isEqualTo(secret0)
        assertThat(store.getSecrets()).hasSize(1)
        // Same secret returned
        assertThat(sut.getSecretForUser(A_USER_ID_0)).isEqualTo(secret0)
        assertThat(store.getSecrets()).hasSize(1)
        // Another secret returned for another user
        assertThat(sut.getSecretForUser(A_USER_ID_1)).isEqualTo(secret1)
        assertThat(store.getSecrets()).hasSize(2)

        // Get users from secrets
        assertThat(sut.getUserIdFromSecret(secret0)).isEqualTo(A_USER_ID_0)
        assertThat(sut.getUserIdFromSecret(secret1)).isEqualTo(A_USER_ID_1)
        // Unknown secret
        assertThat(sut.getUserIdFromSecret(A_UNKNOWN_SECRET)).isNull()

        // User signs out
        sut.onSessionDeleted(A_USER_ID_0.value)
        assertThat(store.getSecrets()).hasSize(1)
        // Create a new secret after reset
        assertThat(sut.getSecretForUser(A_USER_ID_0)).isEqualTo(secret2)

        // Check the store content
        assertThat(store.getSecrets()).isEqualTo(
            mapOf(
                A_USER_ID_0 to secret2,
                A_USER_ID_1 to secret1,
            )
        )
    }
}
