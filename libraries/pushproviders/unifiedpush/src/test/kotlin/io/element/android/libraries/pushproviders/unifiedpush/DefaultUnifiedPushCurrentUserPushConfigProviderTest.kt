/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultUnifiedPushCurrentUserPushConfigProviderTest {
    @Test
    fun `getCurrentUserPushConfig no push gateway`() = runTest {
        val sut = createDefaultUnifiedPushCurrentUserPushConfigProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { null }
            ),
        )
        val result = sut.provide(A_SESSION_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig no push key`() = runTest {
        val sut = createDefaultUnifiedPushCurrentUserPushConfigProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { "aPushGateway" },
                getEndpointResult = { null }
            ),
        )
        val result = sut.provide(A_SESSION_ID)
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig ok`() = runTest {
        val sut = createDefaultUnifiedPushCurrentUserPushConfigProvider(
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { "aPushGateway" },
                getEndpointResult = { "aEndpoint" }
            ),
        )
        val result = sut.provide(A_SESSION_ID)
        assertThat(result).isEqualTo(Config("aPushGateway", "aEndpoint"))
    }

    private fun createDefaultUnifiedPushCurrentUserPushConfigProvider(
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
    ): DefaultUnifiedPushPushConfigProvider {
        return DefaultUnifiedPushPushConfigProvider(
            pushClientSecret = pushClientSecret,
            unifiedPushStore = unifiedPushStore,
        )
    }
}
