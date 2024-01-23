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
import io.element.android.libraries.matrix.impl.util.destroyAll
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
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
import kotlin.coroutines.coroutineContext

/**
 * This class fetches the room members for a given room in a 'paginated' way, and taking into account previous cached values.
 */
internal class RoomMemberListFetcher(
    private val room: RoomInterface,
    private val dispatcher: CoroutineDispatcher,
    private val pageSize: Int = 1000,
) {
    private val updatedRoomMemberMutex = Mutex()
    private val roomId = room.id()

    private val _membersFlow = MutableStateFlow<MatrixRoomMembersState>(MatrixRoomMembersState.Unknown)
    val membersFlow: StateFlow<MatrixRoomMembersState> = _membersFlow

    /**
     * Fetches the room members for the given room.
     * It will emit the cached members first, and then the updated members in batches of [pageSize] items, through [membersFlow].
     * @param withCache Whether to load the cached members first. Defaults to true.
     */
    suspend fun fetchRoomMembers(withCache: Boolean = true) {
        if (updatedRoomMemberMutex.isLocked) {
            Timber.i("Room members are already being updated for room $roomId")
            return
        }
        updatedRoomMemberMutex.withLock {
            withContext(dispatcher) {
                // Load cached members as fallback and to get faster results
                if (withCache) {
                    if (_membersFlow.value !is MatrixRoomMembersState.Ready) {
                        fetchCachedRoomMembers()
                    } else {
                        Timber.i("No need to load cached members found for room $roomId")
                    }
                }

                val prevRoomMembers = (_membersFlow.value as? MatrixRoomMembersState.Ready)?.roomMembers?.toImmutableList()
                _membersFlow.value = MatrixRoomMembersState.Pending(prevRoomMembers = prevRoomMembers)

                try {
                    // Start loading new members
                    parseAndEmitMembers(room.members())
                } catch (exception: CancellationException) {
                    Timber.d("Cancelled loading updated members for room $roomId")
                    throw exception
                } catch (exception: Exception) {
                    Timber.e(exception, "Failed to load updated members for room $roomId")
                    _membersFlow.value = MatrixRoomMembersState.Error(exception, prevRoomMembers)
                }
            }
        }
    }

    internal suspend fun fetchCachedRoomMembers() = withContext(dispatcher) {
        Timber.i("Loading cached members for room $roomId")
        try {
            val iterator = room.membersNoSync()
            parseAndEmitMembers(iterator)
        } catch (exception: CancellationException) {
            Timber.d("Cancelled loading cached members for room $roomId")
            throw exception
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to load cached members for room $roomId")
            _membersFlow.value = MatrixRoomMembersState.Error(exception, _membersFlow.value.roomMembers()?.toImmutableList())
        }
    }

    private suspend fun parseAndEmitMembers(roomMembersIterator: RoomMembersIterator) {
        roomMembersIterator.use { iterator ->
            val results = buildList {
                while (true) {
                    // Loading the whole membersIterator as a stop-gap measure.
                    // We should probably implement some sort of paging in the future.
                    coroutineContext.ensureActive()
                    val chunk = iterator.nextChunk(pageSize.toUInt())
                    val members = try {
                        // Load next chunk. If null (no more items), exit the loop
                        chunk?.parallelMap(RoomMemberMapper::map) ?: break
                    } finally {
                        // Make sure we clear all member references
                        chunk?.destroyAll()
                    }
                    addAll(members)
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
