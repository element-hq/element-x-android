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

package io.element.android.features.messages.impl.utils

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserProfileCache : UserProfileCache {
    override val lastCacheUpdate: Flow<Long> = MutableStateFlow(0L)

    val cache = mutableMapOf<UserId, RoomMember>()

    override fun getDisplayName(userId: UserId): String? {
        return cache[userId]?.displayName
    }

    fun setLastCacheUpdate(time: Long) {
        (lastCacheUpdate as MutableStateFlow).value = time
    }
}
