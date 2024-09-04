/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
