/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SpaceId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val spaceRoomMapper = SpaceRoomMapper()
    private val spaceRoomCache = SpaceRoomCache()
    override val spaceRoomsFlow = MutableSharedFlow<List<SpaceRoom>>(replay = 1, extraBufferCapacity = 1)
    private val spaceListUpdateProcessor = SpaceListUpdateProcessor(
        spaceRoomsFlow = spaceRoomsFlow,
        mapper = spaceRoomMapper,
        spaceRoomCache = spaceRoomCache
    )

    override suspend fun joinedSpaces(): Result<List<SpaceRoom>> = withContext(sessionDispatcher) {
        runCatchingExceptions {
            innerSpaceService.joinedSpaces()
                .map {
                    it.let(spaceRoomMapper::map)
                }
        }
    }

    override fun spaceRoomList(id: RoomId): SpaceRoomList {
        return RustSpaceRoomList(
            roomId = id,
            innerProvider = { innerSpaceService.spaceRoomList(id.value) },
            sessionCoroutineScope = sessionCoroutineScope,
            spaceRoomMapper = spaceRoomMapper,
            spaceRoomCache = spaceRoomCache,
        )
    }

    init {
        innerSpaceService
            .spaceListUpdate()
            .onEach { updates ->
                spaceListUpdateProcessor.postUpdates(updates)
            }
            .launchIn(sessionCoroutineScope)
    }
}

internal fun SpaceServiceInterface.spaceListUpdate(): Flow<List<SpaceListUpdate>> =
    callbackFlow {
        val listener = object : SpaceServiceJoinedSpacesListener {
            override fun onUpdate(roomUpdates: List<SpaceListUpdate>) {
                trySendBlocking(roomUpdates)
            }
        }
        Timber.d("Open spaceDiffFlow for SpaceServiceInterface ${this@spaceListUpdate}")
        val taskHandle = subscribeToJoinedSpaces(listener)
        awaitClose {
            Timber.d("Close spaceDiffFlow for SpaceServiceInterface ${this@spaceListUpdate}")
            taskHandle.cancelAndDestroy()
        }
    }.catch {
        Timber.d(it, "spaceDiffFlow() failed")
    }.buffer(Channel.UNLIMITED)
