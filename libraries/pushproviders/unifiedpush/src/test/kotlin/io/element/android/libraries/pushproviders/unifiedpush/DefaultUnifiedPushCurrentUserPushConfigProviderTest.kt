/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultUnifiedPushCurrentUserPushConfigProviderTest {
    @Test
    fun `getCurrentUserPushConfig no session`() = runTest {
        val sut = createDefaultUnifiedPushCurrentUserPushConfigProvider()
        val result = sut.provide()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig no push gateway`() = runTest {
        val sut = createDefaultUnifiedPushCurrentUserPushConfigProvider(
            appNavigationStateService = FakeAppNavigationStateService(
                appNavigationState = MutableStateFlow(
                    AppNavigationState(
                        navigationState = NavigationState.Session(owner = "owner", sessionId = A_SESSION_ID),
                        isInForeground = true
                    )
                )
            ),
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { null }
            ),
        )
        val result = sut.provide()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig no push key`() = runTest {
        val sut = createDefaultUnifiedPushCurrentUserPushConfigProvider(
            appNavigationStateService = FakeAppNavigationStateService(
                appNavigationState = MutableStateFlow(
                    AppNavigationState(
                        navigationState = NavigationState.Session(owner = "owner", sessionId = A_SESSION_ID),
                        isInForeground = true
                    )
                )
            ),
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { "aPushGateway" },
                getEndpointResult = { null }
            ),
        )
        val result = sut.provide()
        assertThat(result).isNull()
    }

    @Test
    fun `getCurrentUserPushConfig ok`() = runTest {
        val sut = createDefaultUnifiedPushCurrentUserPushConfigProvider(
            appNavigationStateService = FakeAppNavigationStateService(
                appNavigationState = MutableStateFlow(
                    AppNavigationState(
                        navigationState = NavigationState.Session(owner = "owner", sessionId = A_SESSION_ID),
                        isInForeground = true
                    )
                )
            ),
            pushClientSecret = FakePushClientSecret(
                getSecretForUserResult = { A_SECRET }
            ),
            unifiedPushStore = FakeUnifiedPushStore(
                getPushGatewayResult = { "aPushGateway" },
                getEndpointResult = { "aEndpoint" }
            ),
        )
        val result = sut.provide()
        assertThat(result).isEqualTo(CurrentUserPushConfig("aPushGateway", "aEndpoint"))
    }

    private fun createDefaultUnifiedPushCurrentUserPushConfigProvider(
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        unifiedPushStore: UnifiedPushStore = FakeUnifiedPushStore(),
        appNavigationStateService: AppNavigationStateService = FakeAppNavigationStateService(),
    ): DefaultUnifiedPushCurrentUserPushConfigProvider {
        return DefaultUnifiedPushCurrentUserPushConfigProvider(
            pushClientSecret = pushClientSecret,
            unifiedPushStore = unifiedPushStore,
            appNavigationStateService = appNavigationStateService,
        )
    }
}
