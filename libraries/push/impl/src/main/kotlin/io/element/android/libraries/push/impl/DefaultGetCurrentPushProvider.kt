/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.currentSessionId

@ContributesBinding(AppScope::class)
@Inject class DefaultGetCurrentPushProvider(
    private val pushStoreFactory: UserPushStoreFactory,
    private val appNavigationStateService: AppNavigationStateService,
) : GetCurrentPushProvider {
    override suspend fun getCurrentPushProvider(): String? {
        return appNavigationStateService
            .appNavigationState
            .value
            .navigationState
            .currentSessionId()
            ?.let { pushStoreFactory.getOrCreate(it) }
            ?.getPushProviderName()
    }
}
