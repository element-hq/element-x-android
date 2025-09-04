/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceService
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.SpaceListUpdate
import org.matrix.rustcomponents.sdk.SpaceServiceInterface
import org.matrix.rustcomponents.sdk.SpaceServiceJoinedSpacesListener
import timber.log.Timber
import org.matrix.rustcomponents.sdk.SpaceService as ClientSpaceService

class RustSpaceService(
    private val innerSpaceService: ClientSpaceService,
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionDispatcher: CoroutineDispatcher,
) : SpaceService {
    private val mapper = SpaceRoomMapper()
    private val mutex = Mutex()

    override val spaceRooms = MutableSharedFlow<List<SpaceRoom>>(replay = 1, extraBufferCapacity = 1)

    override suspend fun joinedSpaces(): Result<List<SpaceRoom>> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerSpaceService.joinedSpaces()
                .map {
                    it.let(mapper::map)
                }
        }
    }

    // override suspend fun spaceRoomList(spaceId: SpaceId): Result<List<SpaceRoom>> = withContext(sessionDispatcher) {
    //     runCatchingExceptions {
    //         innerSpaceService.spaceRoomList(spaceId.value)
    //     }
    // }

    init {
        innerSpaceService
            .spaceDiffFlow()
            .onEach {
                handeUpdate(it)
            }
            .launchIn(sessionCoroutineScope)
    }

    private suspend fun handeUpdate(spaceListUpdates: List<SpaceListUpdate>) {
        mutex.withLock {
            val current = if (spaceRooms.replayCache.isNotEmpty()) {
                spaceRooms.first().toMutableList()
            } else {
                mutableListOf()
            }
            spaceListUpdates.forEach { update ->
                current.applyUpdate(update)
            }
            spaceRooms.emit(current)
        }
    }

    private fun MutableList<SpaceRoom>.applyUpdate(update: SpaceListUpdate) {
        when (update) {
            is SpaceListUpdate.Append -> {
                val newSpaces = update.values.map(mapper::map)
                addAll(newSpaces)
            }
            SpaceListUpdate.Clear -> clear()
            is SpaceListUpdate.Insert -> {
                val newSpace = mapper.map(update.value)
                add(update.index.toInt(), newSpace)
            }
            SpaceListUpdate.PopBack -> {
                removeAt(lastIndex)
            }
            SpaceListUpdate.PopFront -> {
                removeAt(0)
            }
            is SpaceListUpdate.PushBack -> {
                val newSpace = mapper.map(update.value)
                add(newSpace)
            }
            is SpaceListUpdate.PushFront -> {
                val newSpace = mapper.map(update.value)
                add(0, newSpace)
            }
            is SpaceListUpdate.Remove -> {
                removeAt(update.index.toInt())
            }
            is SpaceListUpdate.Reset -> {
                clear()
                val newSpaces = update.values.map(mapper::map)
                addAll(newSpaces)
            }
            is SpaceListUpdate.Set -> {
                val newSpace = mapper.map(update.value)
                this[update.index.toInt()] = newSpace
            }
            is SpaceListUpdate.Truncate -> {
                subList(update.length.toInt(), size).clear()
            }
        }
    }
}

internal fun SpaceServiceInterface.spaceDiffFlow(): Flow<List<SpaceListUpdate>> =
    callbackFlow {
        val listener = object : SpaceServiceJoinedSpacesListener {
            override fun onUpdate(roomUpdates: List<SpaceListUpdate>) {
                trySendBlocking(roomUpdates)
            }
        }
        Timber.d("Open spaceDiffFlow for SpaceServiceInterface ${this@spaceDiffFlow}")
        val taskHandle = subscribeToJoinedSpaces(listener)
        awaitClose {
            Timber.d("Close spaceDiffFlow for SpaceServiceInterface ${this@spaceDiffFlow}")
            taskHandle.cancelAndDestroy()
        }
    }.catch {
        Timber.d(it, "spaceDiffFlow() failed")
    }.buffer(Channel.UNLIMITED)
