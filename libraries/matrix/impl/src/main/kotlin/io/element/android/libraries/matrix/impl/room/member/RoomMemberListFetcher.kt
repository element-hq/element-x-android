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

import io.element.android.libraries.matrix.api.room.MatrixRoomMembersState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.collections.immutable.ImmutableList
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
    private val pageSize: Int = 10_000,
) {
    enum class Source {
        CACHE,
        CACHE_AND_SERVER,
        SERVER,
    }
    private val updatedRoomMemberMutex = Mutex()
    private val roomId = room.id()

    private val _membersFlow = MutableStateFlow<MatrixRoomMembersState>(MatrixRoomMembersState.Unknown)
    val membersFlow: StateFlow<MatrixRoomMembersState> = _membersFlow

    /**
     * Fetches the room members for the given room.
     * It will emit the cached members first, and then the updated members in batches of [pageSize] items, through [membersFlow].
     * @param source Where we should load the members from. Defaults to [Source.CACHE_AND_SERVER].
     */
    suspend fun fetchRoomMembers(source: Source = Source.CACHE_AND_SERVER) {
        if (updatedRoomMemberMutex.isLocked) {
            Timber.i("Room members are already being updated for room $roomId")
            return
        }
        updatedRoomMemberMutex.withLock {
            withContext(dispatcher) {
                _membersFlow.run {
                    when (source) {
                        Source.CACHE -> {
                            fetchCachedRoomMembers(asPendingState = false)
                        }
                        Source.CACHE_AND_SERVER -> {
                            fetchCachedRoomMembers(asPendingState = true)
                            fetchRemoteRoomMembers()
                        }
                        Source.SERVER -> {
                            fetchRemoteRoomMembers()
                        }
                    }
                }
            }
        }
    }

    private suspend fun MutableStateFlow<MatrixRoomMembersState>.fetchCachedRoomMembers(asPendingState: Boolean = true) {
        Timber.i("Loading cached members for room $roomId")
        try {
            // Send current member list with pending state to notify the UI that we are loading new members
            emit(pendingWithCurrentMembers())
            val members = parseAndEmitMembers(room.membersNoSync())
            val newState = if (asPendingState) {
                MatrixRoomMembersState.Pending(prevRoomMembers = members)
            } else {
                MatrixRoomMembersState.Ready(members)
            }
            emit(newState)
        } catch (exception: CancellationException) {
            Timber.d("Cancelled loading cached members for room $roomId")
            throw exception
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to load cached members for room $roomId")
            emit(MatrixRoomMembersState.Error(exception, _membersFlow.value.roomMembers()?.toImmutableList()))
        }
    }

    private suspend fun MutableStateFlow<MatrixRoomMembersState>.fetchRemoteRoomMembers() {
        try {
            // Send current member list with pending state to notify the UI that we are loading new members
            emit(pendingWithCurrentMembers())
            // Start loading new members
            emit(MatrixRoomMembersState.Ready(parseAndEmitMembers(room.members())))
        } catch (exception: CancellationException) {
            Timber.d("Cancelled loading updated members for room $roomId")
            throw exception
        } catch (exception: Exception) {
            Timber.e(exception, "Failed to load updated members for room $roomId")
            emit(MatrixRoomMembersState.Error(exception, _membersFlow.value.roomMembers()?.toImmutableList()))
        }
    }

    private suspend fun parseAndEmitMembers(roomMembersIterator: RoomMembersIterator): ImmutableList<RoomMember> {
        return roomMembersIterator.use { iterator ->
            val results = buildList(capacity = roomMembersIterator.len().toInt()) {
                while (true) {
                    // Loading the whole membersIterator as a stop-gap measure.
                    // We should probably implement some sort of paging in the future.
                    coroutineContext.ensureActive()
                    val chunk = iterator.nextChunk(pageSize.toUInt())
                    // Load next chunk. If null (no more items), exit the loop
                    val members = chunk?.map(RoomMemberMapper::map) ?: break
                    addAll(members)
                    Timber.i("Loaded first $size members for room $roomId")
                }
            }
            results.toImmutableList()
        }
    }

    private fun pendingWithCurrentMembers() = MatrixRoomMembersState.Pending(_membersFlow.value.roomMembers().orEmpty().toImmutableList())
}
