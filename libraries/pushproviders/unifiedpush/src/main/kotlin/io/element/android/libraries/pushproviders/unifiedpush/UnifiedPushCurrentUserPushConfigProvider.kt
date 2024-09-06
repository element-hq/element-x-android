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

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.currentSessionId
import javax.inject.Inject

interface UnifiedPushCurrentUserPushConfigProvider {
    suspend fun provide(): CurrentUserPushConfig?
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushCurrentUserPushConfigProvider @Inject constructor(
    private val pushClientSecret: PushClientSecret,
    private val unifiedPushStore: UnifiedPushStore,
    private val appNavigationStateService: AppNavigationStateService,
) : UnifiedPushCurrentUserPushConfigProvider {
    override suspend fun provide(): CurrentUserPushConfig? {
        val currentSession = appNavigationStateService.appNavigationState.value.navigationState.currentSessionId() ?: return null
        val clientSecret = pushClientSecret.getSecretForUser(currentSession)
        val url = unifiedPushStore.getPushGateway(clientSecret) ?: return null
        val pushKey = unifiedPushStore.getEndpoint(clientSecret) ?: return null
        return CurrentUserPushConfig(
            url = url,
            pushKey = pushKey,
        )
    }
}
