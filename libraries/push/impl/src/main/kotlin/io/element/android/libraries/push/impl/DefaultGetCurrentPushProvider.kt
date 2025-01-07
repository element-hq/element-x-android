/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.services.appnavstate.api.AppNavigationStateService
import io.element.android.services.appnavstate.api.currentSessionId
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultGetCurrentPushProvider @Inject constructor(
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
