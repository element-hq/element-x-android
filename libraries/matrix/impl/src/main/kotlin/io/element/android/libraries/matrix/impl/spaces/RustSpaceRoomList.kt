/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import uniffi.matrix_sdk_ui.SpaceRoomListPaginationState
import java.util.Optional
import org.matrix.rustcomponents.sdk.SpaceRoomList as InnerSpaceRoomList

class RustSpaceRoomList(
    override val roomId: RoomId,
    private val innerProvider: suspend () -> InnerSpaceRoomList,
    private val coroutineScope: CoroutineScope,
    spaceRoomMapper: SpaceRoomMapper,
) : SpaceRoomList {
    private val innerCompletable = CompletableDeferred<InnerSpaceRoomList>()

    override val currentSpaceFlow = MutableStateFlow<Optional<SpaceRoom>>(Optional.empty())

    override val spaceRoomsFlow = MutableSharedFlow<List<SpaceRoom>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

    override val paginationStatusFlow: MutableStateFlow<SpaceRoomList.PaginationStatus> =
        MutableStateFlow(SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = false))
    private val spaceListUpdateProcessor = SpaceListUpdateProcessor(
        spaceRoomsFlow = spaceRoomsFlow,
        mapper = spaceRoomMapper
    )

    init {
        coroutineScope.launch {
            val inner = innerProvider()
            innerCompletable.complete(inner)

            inner.paginationStateFlow()
                .onEach { paginationStatus ->
                    paginationStatusFlow.emit(paginationStatus.into())
                }
                .launchIn(this)

            inner.spaceListUpdateFlow()
                .onEach { updates ->
                    spaceListUpdateProcessor.postUpdates(updates)
                }
                .launchIn(this)

            inner.spaceUpdateFlow()
                .map { space -> space.map(spaceRoomMapper::map) }
                .onEach { space ->
                    currentSpaceFlow.emit(space)
                }
                .launchIn(this)
        }
    }

    override suspend fun paginate(): Result<Unit> {
        return runCatchingExceptions {
            innerCompletable.await().paginate()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun destroy() {
        Timber.d("Destroying SpaceRoomList $roomId")
        coroutineScope.cancel()
        try {
            innerCompletable.getCompleted().destroy()
        } catch (_: Exception) {
            // Ignore, we just want to make sure it's completed
        }
    }

    private fun SpaceRoomListPaginationState.into(): SpaceRoomList.PaginationStatus {
        return when (this) {
            is SpaceRoomListPaginationState.Idle -> SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = !endReached)
            SpaceRoomListPaginationState.Loading -> SpaceRoomList.PaginationStatus.Loading
        }
    }
}
