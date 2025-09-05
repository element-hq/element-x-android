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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uniffi.matrix_sdk_ui.SpaceRoomListPaginationState
import org.matrix.rustcomponents.sdk.SpaceRoomList as InnerSpaceRoomList

class RustSpaceRoomList(
    private val inner: InnerSpaceRoomList,
    sessionCoroutineScope: CoroutineScope,
    spaceRoomMapper: SpaceRoomMapper,
) : SpaceRoomList {
    override val spaceRoomsFlow = MutableSharedFlow<List<SpaceRoom>>(replay = 1, extraBufferCapacity = Int.MAX_VALUE)
    override val paginationStatusFlow = MutableStateFlow(inner.paginationState().into())
    private val spaceListUpdateProcessor = SpaceListUpdateProcessor(spaceRoomsFlow, spaceRoomMapper)

    init {
        inner.paginationStateFlow()
            .onEach { paginationStatus ->
                paginationStatusFlow.emit(paginationStatus.into())
            }
            .launchIn(sessionCoroutineScope)

        inner.spaceListUpdateFlow()
            .onEach { updates ->
                spaceListUpdateProcessor.postUpdates(updates)
            }
            .launchIn(sessionCoroutineScope)
    }

    override suspend fun paginate(): Result<Unit> {
        return runCatchingExceptions {
            inner.paginate()
        }
    }

    private fun SpaceRoomListPaginationState.into(): SpaceRoomList.PaginationStatus {
        return when (this) {
            is SpaceRoomListPaginationState.Idle -> SpaceRoomList.PaginationStatus.Idle(hasMoreToLoad = !endReached)
            SpaceRoomListPaginationState.Loading -> SpaceRoomList.PaginationStatus.Loading
        }
    }
}
