/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.ui.messages

import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class UserProfileCache @Inject constructor(
    private val clock: SystemClock,
) {
    private val cache = MutableStateFlow(mapOf<UserId, MatrixUser>())

    private val _lastCacheUpdate = MutableStateFlow(0L)
    val lastCacheUpdate: StateFlow<Long> = _lastCacheUpdate

    fun replace(items: List<MatrixUser>) {
        cache.value = items.associateBy { it.userId }
        _lastCacheUpdate.tryEmit(clock.epochMillis())
    }

    fun getDisplayName(userId: UserId): String? {
        return cache.value[userId]?.displayName
    }
}

val LocalUserProfileCache = staticCompositionLocalOf {
    UserProfileCache(clock = { 0L })
}
