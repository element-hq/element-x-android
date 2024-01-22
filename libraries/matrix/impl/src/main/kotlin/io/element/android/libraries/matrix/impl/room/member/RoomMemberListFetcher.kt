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

package io.element.android.libraries.matrix.impl.room.member

import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.RoomInterface
import org.matrix.rustcomponents.sdk.RoomMembersIterator
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

internal class RoomMemberListFetcher(
    private val room: RoomInterface,
    private val dispatcher: CoroutineDispatcher,
    private val pageSize: Int = 1000,
) {
    private val updatedRoomMemberMutex = Mutex()
    private val roomId = room.id()

    private val _membersFlow = MutableStateFlow<MatrixRoomMembersState>(MatrixRoomMembersState.Unknown)
    val membersFlow: StateFlow<MatrixRoomMembersState> = _membersFlow

    @Suppress("InstanceOfCheckForException")
    suspend fun getUpdatedRoomMembers(withCache: Boolean = true) {
        if (updatedRoomMemberMutex.isLocked) {
            Timber.i("Room members are already being updated for room $roomId")
            return
        }
        updatedRoomMemberMutex.withLock {
            withContext(dispatcher) {
                // Load cached members as fallback and to get faster results
                if (withCache) {
                    if (_membersFlow.value !is MatrixRoomMembersState.Ready) {
                        getCachedRoomMembers()
                    } else {
                        Timber.i("No need to load cached members found for room $roomId")
                    }
                }

                val prevRoomMembers = (_membersFlow.value as? MatrixRoomMembersState.Ready)?.roomMembers?.toImmutableList()
                _membersFlow.value = MatrixRoomMembersState.Pending(prevRoomMembers = prevRoomMembers)

                try {
                    // Start loading new members
                    parseAndEmitMembers(room.members())
                    Result.success(Unit)
                } catch (exception: Exception) {
                    Timber.e(exception, "Failed to load updated members for room $roomId")
                    _membersFlow.value = MatrixRoomMembersState.Error(exception, prevRoomMembers)
                    if (exception is CancellationException) {
                        throw exception
                    }
                    Result.failure(exception)
                }
            }
        }
    }

    @Suppress("InstanceOfCheckForException")
    internal suspend fun getCachedRoomMembers() = withContext(dispatcher) {
        Timber.i("Loading cached members for room $roomId")
        try {
            val iterator = room.membersNoSync()
            parseAndEmitMembers(iterator)
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to load cached members for room $roomId")
            _membersFlow.value = MatrixRoomMembersState.Error(exception, _membersFlow.value.roomMembers()?.toImmutableList())
            if (exception is CancellationException) {
                throw exception
            }
        }
    }

    private suspend fun CoroutineScope.parseAndEmitMembers(roomMembersIterator: RoomMembersIterator) {
        roomMembersIterator.use { iterator ->
            val results = buildList {
                while (true) {
                    // Loading the whole membersIterator as a stop-gap measure.
                    // We should probably implement some sort of paging in the future.
                    ensureActive()
                    addAll(iterator.nextChunk(pageSize.toUInt())?.parallelMap(RoomMemberMapper::map) ?: break)
                    Timber.i("Emitting first $size members for room $roomId")
                    _membersFlow.value = MatrixRoomMembersState.Ready(toImmutableList())
                }
            }
            if (results.isEmpty()) {
                _membersFlow.value = MatrixRoomMembersState.Ready(results.toImmutableList())
            }
        }
    }
}
