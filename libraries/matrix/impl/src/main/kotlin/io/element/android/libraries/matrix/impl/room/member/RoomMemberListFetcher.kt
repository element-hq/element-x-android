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
import io.element.android.libraries.matrix.api.room.RoomMember
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
) {
    companion object {
        private const val PAGE_SIZE = 1000
    }

    private val roomMemberMutex = Mutex()

    private val _membersFlow = MutableStateFlow<MatrixRoomMembersState>(MatrixRoomMembersState.Unknown)
    val membersFlow: StateFlow<MatrixRoomMembersState> = _membersFlow

    suspend fun getUpdatedRoomMembers(returnCachedFirst: Boolean = true): Result<Unit> {
        if (roomMemberMutex.isLocked) {
            Timber.i("Room members are already being updated for room ${room.id()}")
            return Result.success(Unit)
        }
        return roomMemberMutex.withLock {
            withContext(dispatcher) {
                // Load cached members as fallback and to get faster results
                if (returnCachedFirst) {
                    val cachedMembers = try {
                        if (_membersFlow.value !is MatrixRoomMembersState.Ready) {
                            Timber.i("Loading cached members for room ${room.id()}")
                            getCachedRoomMembers().toImmutableList()
                        } else {
                            Timber.i("No need to load cached members found for room ${room.id()}")
                            null
                        }
                    } catch (exception: Exception) {
                        Timber.e(exception, "Failed to load cached members for room ${room.id()}")
                        if (exception is CancellationException) {
                            throw exception
                        }
                        null
                    }
                    _membersFlow.value = MatrixRoomMembersState.Pending(prevRoomMembers = cachedMembers)
                }

                // Start loading new members
                val iterator = room.members()
                parseAndEmitMembers(iterator)
                Result.success(Unit)
            }
        }
    }

    suspend fun getCachedRoomMembers() = withContext(dispatcher) {
        val iterator = room.membersNoSync()
        parseAndEmitMembers(iterator)
    }

    private suspend fun CoroutineScope.parseAndEmitMembers(roomMembersIterator: RoomMembersIterator): List<RoomMember> {
        return roomMembersIterator.use { iterator ->
            buildList {
                while (true) {
                    // Loading the whole membersIterator as a stop-gap measure.
                    // We should probably implement some sort of paging in the future.
                    ensureActive()
                    addAll(iterator.nextChunk(PAGE_SIZE.toUInt())?.parallelMap(RoomMemberMapper::map) ?: break)
                    Timber.i("Emitting first $size members for room ${room.id()}")
                    _membersFlow.value = MatrixRoomMembersState.Ready(toImmutableList())
                }
            }
        }
    }
}
