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

package io.element.android.features.roomlist.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.roomlist.impl.datasource.InviteStateDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.collectSnackbarMessageAsState
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.getCurrentUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val THRESHOLD_PAGE_MULTIPLIER = 5

class RoomListPresenter @Inject constructor(
    private val client: MatrixClient,
    private val sessionVerificationService: SessionVerificationService,
    private val networkMonitor: NetworkMonitor,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val inviteStateDataSource: InviteStateDataSource,
    private val leaveRoomPresenter: LeaveRoomPresenter,
    private val roomListDataSource: RoomListDataSource,
) : Presenter<RoomListState> {

    @Composable
    override fun present(): RoomListState {
        val leaveRoomState = leaveRoomPresenter.present()
        val matrixUser: MutableState<MatrixUser?> = rememberSaveable {
            mutableStateOf(null)
        }
        val roomList by roomListDataSource.allRooms.collectAsState()
        val filteredRoomList by roomListDataSource.filteredRooms.collectAsState()
        val filter by roomListDataSource.filter.collectAsState()
        val networkConnectionStatus by networkMonitor.connectivity.collectAsState()
        val visibleRange = remember {
            mutableStateOf(IntRange.EMPTY)
        }
        LaunchedEffect(Unit) {
            roomListDataSource.launchIn(this)
            initialLoad(matrixUser)
        }
        VisibleRangeHandler(visibleRange = visibleRange)
        // Session verification status (unknown, not verified, verified)
        val canVerifySession by sessionVerificationService.canVerifySessionFlow.collectAsState(initial = false)
        var verificationPromptDismissed by rememberSaveable { mutableStateOf(false) }
        // We combine both values to only display the prompt if the session is not verified and it wasn't dismissed
        val displayVerificationPrompt by remember {
            derivedStateOf { canVerifySession && !verificationPromptDismissed }
        }

        var displaySearchResults by rememberSaveable { mutableStateOf(false) }

        var contextMenu by remember { mutableStateOf<RoomListState.ContextMenu>(RoomListState.ContextMenu.Hidden) }

        fun handleEvents(event: RoomListEvents) {
            when (event) {
                is RoomListEvents.UpdateFilter -> roomListDataSource.updateFilter(event.newFilter)
                is RoomListEvents.UpdateVisibleRange -> visibleRange.value = event.range
                RoomListEvents.DismissRequestVerificationPrompt -> verificationPromptDismissed = true
                RoomListEvents.ToggleSearchResults -> {
                    if (displaySearchResults) {
                        roomListDataSource.updateFilter("")
                    }
                    displaySearchResults = !displaySearchResults
                }
                is RoomListEvents.ShowContextMenu -> {
                    contextMenu = RoomListState.ContextMenu.Shown(
                        roomId = event.roomListRoomSummary.roomId,
                        roomName = event.roomListRoomSummary.name
                    )
                }
                is RoomListEvents.HideContextMenu -> contextMenu = RoomListState.ContextMenu.Hidden
                is RoomListEvents.LeaveRoom -> leaveRoomState.eventSink(LeaveRoomEvent.ShowConfirmation(event.roomId))
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        return RoomListState(
            matrixUser = matrixUser.value,
            roomList = roomList,
            filter = filter,
            filteredRoomList = filteredRoomList,
            displayVerificationPrompt = displayVerificationPrompt,
            snackbarMessage = snackbarMessage,
            hasNetworkConnection = networkConnectionStatus == NetworkStatus.Online,
            invitesState = inviteStateDataSource.inviteState(),
            displaySearchResults = displaySearchResults,
            contextMenu = contextMenu,
            leaveRoomState = leaveRoomState,
            eventSink = ::handleEvents
        )
    }

    @Composable
    private fun VisibleRangeHandler(visibleRange: State<IntRange>) {
        val hasReachEndThreshold = remember {
            derivedStateOf {
                visibleRange.value.last > roomListDataSource.allRooms.value.size - RoomListService.DEFAULT_PAGE_SIZE * THRESHOLD_PAGE_MULTIPLIER
            }
        }
        val hasReachedStart = remember {
            derivedStateOf {
                visibleRange.value.first == 0
            }
        }
        LaunchedEffect(Unit) {
            launch {
                roomListDataSource.loadMore()
            }
            snapshotFlow { visibleRange.value }
                .debounce(300)
                .onEach { range ->
                    client.roomListService.updateAllRoomsVisibleRange(range)
                }.launchIn(this)

            snapshotFlow { hasReachedStart.value }
                .onEach { shouldReset ->
                    if (shouldReset) {
                        //roomListDataSource.reset()
                    }
                }.launchIn(this)

            snapshotFlow { hasReachEndThreshold.value }
                .onEach { shouldLoadMore ->
                    if (shouldLoadMore) {
                        roomListDataSource.loadMore()
                    }
                }.launchIn(this)
        }
    }

    private fun CoroutineScope.initialLoad(matrixUser: MutableState<MatrixUser?>) = launch {
        matrixUser.value = client.getCurrentUser()
    }
}
