/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.preferences.api.CacheService
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultCacheService @Inject constructor() : CacheService {
    private val _clearedCacheEventFlow = MutableSharedFlow<SessionId>(0)
    override val clearedCacheEventFlow: Flow<SessionId> = _clearedCacheEventFlow

    suspend fun onClearedCache(sessionId: SessionId) {
        _clearedCacheEventFlow.emit(sessionId)
    }
}
