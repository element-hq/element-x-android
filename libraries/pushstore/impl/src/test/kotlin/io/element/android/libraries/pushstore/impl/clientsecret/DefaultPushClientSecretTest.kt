/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushstore.impl.clientsecret

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.InMemoryPushClientSecretStore
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
        val sut = DefaultPushClientSecret(factory, store)

        val secret0 = factory.getSecretForUser(0)
        val secret1 = factory.getSecretForUser(1)

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

        // Check the store content
        assertThat(store.getSecrets()).isEqualTo(
            mapOf(
                A_USER_ID_0 to secret0,
                A_USER_ID_1 to secret1,
            )
        )
    }
}
