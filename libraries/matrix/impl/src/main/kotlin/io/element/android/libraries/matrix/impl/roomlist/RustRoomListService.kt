/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.RoomListEntriesUpdate
import org.matrix.rustcomponents.sdk.RoomListException
import org.matrix.rustcomponents.sdk.RoomListInput
import org.matrix.rustcomponents.sdk.RoomListLoadingState
import org.matrix.rustcomponents.sdk.RoomListRange
import org.matrix.rustcomponents.sdk.RoomListServiceState
import timber.log.Timber
import org.matrix.rustcomponents.sdk.RoomListService as InnerRustRoomListService

class RustRoomListService(
    private val innerRoomListService: InnerRustRoomListService,
    private val sessionCoroutineScope: CoroutineScope,
    dispatcher: CoroutineDispatcher,
    roomSummaryDetailsFactory: RoomSummaryDetailsFactory = RoomSummaryDetailsFactory(),
) : RoomListService {

    private val allRooms = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val inviteRooms = MutableStateFlow<List<RoomSummary>>(emptyList())

    private val allRoomsLoadingState: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded)
    private val allRoomsListProcessor = RoomSummaryListProcessor(allRooms, innerRoomListService, roomSummaryDetailsFactory, shouldFetchFullRoom = false)
    private val invitesLoadingState: MutableStateFlow<RoomList.LoadingState> = MutableStateFlow(RoomList.LoadingState.NotLoaded)
    private val inviteRoomsListProcessor = RoomSummaryListProcessor(inviteRooms, innerRoomListService, roomSummaryDetailsFactory, shouldFetchFullRoom = true)

    init {
        sessionCoroutineScope.launch(dispatcher) {
            val allRooms = innerRoomListService.allRooms()
            allRooms
                .observeEntriesWithProcessor(allRoomsListProcessor)
                .launchIn(this)
            allRooms
                .observeLoadingState(allRoomsLoadingState)
                .launchIn(this)


            launch {
                // Wait until running, as invites is only available after that
                innerRoomListService.stateFlow().first {
                    it == RoomListServiceState.RUNNING
                }
                val invites = innerRoomListService.invites()
                invites
                    .observeEntriesWithProcessor(inviteRoomsListProcessor)
                    .launchIn(this)
                invites
                    .observeLoadingState(invitesLoadingState)
                    .launchIn(this)

            }
        }
    }

    override fun allRooms(): RoomList {
        return RustRoomList(allRooms, allRoomsLoadingState)
    }

    override fun invites(): RoomList {
        return RustRoomList(inviteRooms, invitesLoadingState)
    }

    override fun updateAllRoomsVisibleRange(range: IntRange) {
        Timber.v("setVisibleRange=$range")
        sessionCoroutineScope.launch {
            try {
                val ranges = listOf(RoomListRange(range.first.toUInt(), range.last.toUInt()))
                innerRoomListService.applyInput(
                    RoomListInput.Viewport(ranges)
                )
            } catch (exception: RoomListException) {
                Timber.e(exception, "Failed updating visible range")
            }
        }
    }

    override val state: StateFlow<RoomListService.State> =
        innerRoomListService.stateFlow()
            .map { it.toRoomListState() }
            .onEach { state ->
                Timber.d("RoomList state=$state")
            }
            .distinctUntilChanged()
            .stateIn(sessionCoroutineScope, SharingStarted.Eagerly, RoomListService.State.Idle)
}

private fun RoomListLoadingState.toLoadingState(): RoomList.LoadingState {
    return when (this) {
        is RoomListLoadingState.Loaded -> RoomList.LoadingState.Loaded(maximumNumberOfRooms?.toInt() ?: 0)
        RoomListLoadingState.NotLoaded -> RoomList.LoadingState.NotLoaded
    }
}

private fun RoomListServiceState.toRoomListState(): RoomListService.State {
    return when (this) {
        RoomListServiceState.INITIAL,
        RoomListServiceState.RECOVERING,
        RoomListServiceState.SETTING_UP -> RoomListService.State.Idle
        RoomListServiceState.RUNNING -> RoomListService.State.Running
        RoomListServiceState.ERROR -> RoomListService.State.Error
        RoomListServiceState.TERMINATED -> RoomListService.State.Terminated
    }
}

private fun org.matrix.rustcomponents.sdk.RoomList.observeEntriesWithProcessor(processor: RoomSummaryListProcessor): Flow<List<RoomListEntriesUpdate>> {
    return entriesFlow { roomListEntries ->
        processor.postEntries(roomListEntries)
    }.onEach { update ->
        processor.postUpdate(update)
    }
}

private fun org.matrix.rustcomponents.sdk.RoomList.observeLoadingState(stateFlow: MutableStateFlow<RoomList.LoadingState>): Flow<RoomList.LoadingState> {
    return loadingStateFlow()
        .map { it.toLoadingState() }
        .onEach {
            stateFlow.value = it
        }
}

