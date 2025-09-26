/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.spaces

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uniffi.matrix_sdk_ui.SpaceRoomListPaginationState
import java.util.Optional
import org.matrix.rustcomponents.sdk.SpaceRoomList as InnerSpaceRoomList

class RustSpaceRoomList(
    private val innerProvider: suspend () -> InnerSpaceRoomList,
    sessionCoroutineScope: CoroutineScope,
    spaceRoomMapper: SpaceRoomMapper,
) : SpaceRoomList {
    private val inner = CompletableDeferred<InnerSpaceRoomList>()

    override val currentSpaceFlow = MutableStateFlow<Optional<SpaceRoom>>(Optional.empty())

    override val spaceRoomsFlow = MutableSharedFlow<List<SpaceRoom>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)

    override val paginationStatusFlow: MutableStateFlow<SpaceRoomList.PaginationStatus> =
        MutableStateFlow(SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = false))
    private val spaceListUpdateProcessor = SpaceListUpdateProcessor(
        spaceRoomsFlow = spaceRoomsFlow,
        mapper = spaceRoomMapper
    )

    init {
        sessionCoroutineScope.launch {
            inner.complete(innerProvider())
        }
        sessionCoroutineScope.launch {
            inner.await().paginationStateFlow()
                .onEach { paginationStatus ->
                    paginationStatusFlow.emit(paginationStatus.into())
                }
                .collect()
        }

        sessionCoroutineScope.launch {
            inner.await().spaceListUpdateFlow()
                .onEach { updates ->
                    spaceListUpdateProcessor.postUpdates(updates)
                }
                .collect()
        }
        sessionCoroutineScope.launch {
            inner.await().spaceUpdateFlow()
                .map { space -> space.map(spaceRoomMapper::map) }
                .onEach { space ->
                    currentSpaceFlow.emit(space)
                }
                .collect()
        }
    }

    override suspend fun paginate(): Result<Unit> {
        return runCatchingExceptions {
            inner.await().paginate()
        }
    }

    private fun SpaceRoomListPaginationState.into(): SpaceRoomList.PaginationStatus {
        return when (this) {
            is SpaceRoomListPaginationState.Idle -> SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = !endReached)
            SpaceRoomListPaginationState.Loading -> SpaceRoomList.PaginationStatus.Loading
        }
    }
}
