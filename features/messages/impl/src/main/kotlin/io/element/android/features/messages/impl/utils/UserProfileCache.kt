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

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.joinedRoomMembers
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

interface UserProfileCache {
    val lastCacheUpdate: Flow<Long>
    fun getDisplayName(userId: UserId): String?
}

@ContributesBinding(RoomScope::class)
@SingleIn(RoomScope::class)
class DefaultUserProfileCache @Inject constructor(
    room: MatrixRoom,
    coroutineScope: CoroutineScope,
    clock: SystemClock,
) : UserProfileCache {
    private val cache = MutableStateFlow(mapOf<UserId, RoomMember>())

    private val _lastCacheUpdate = MutableSharedFlow<Long>()
    override val lastCacheUpdate: Flow<Long> = _lastCacheUpdate

    init {
        room.membersStateFlow
            .onEach { state ->
                cache.value = state.joinedRoomMembers().associateBy { it.userId }
                _lastCacheUpdate.emit(clock.epochMillis())
            }
            .launchIn(coroutineScope)
    }

    override fun getDisplayName(userId: UserId): String? {
        return cache.value[userId]?.disambiguatedDisplayName
    }
}
