/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.GetCurrentPushProvider
import io.element.android.libraries.pushstore.api.UserPushStoreFactory

@ContributesBinding(AppScope::class)
class DefaultGetCurrentPushProvider(
    private val pushStoreFactory: UserPushStoreFactory,
) : GetCurrentPushProvider {
    override suspend fun getCurrentPushProvider(sessionId: SessionId): String? {
        return pushStoreFactory.getOrCreate(sessionId).getPushProviderName()
    }
}
