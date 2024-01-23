/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
