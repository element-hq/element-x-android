/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.currentSessionId
import dev.zacsweers.metro.Inject

interface UnifiedPushCurrentUserPushConfigProvider {
    suspend fun provide(): CurrentUserPushConfig?
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultUnifiedPushCurrentUserPushConfigProvider(
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
