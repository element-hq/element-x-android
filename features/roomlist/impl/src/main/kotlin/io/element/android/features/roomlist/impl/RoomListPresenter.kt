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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryPlaceholders
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.parallelMap
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.utils.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.collectSnackbarMessageAsState
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomSummary
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val extendedRangeSize = 40

class RoomListPresenter @Inject constructor(
    private val client: MatrixClient,
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
    private val sessionVerificationService: SessionVerificationService,
    private val networkMonitor: NetworkMonitor,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val inviteStateDataSource: InviteStateDataSource,
    private val leaveRoomPresenter: LeaveRoomPresenter,
) : Presenter<RoomListState> {

    @Composable
    override fun present(): RoomListState {
        val leaveRoomState = leaveRoomPresenter.present()
        val matrixUser: MutableState<MatrixUser?> = rememberSaveable {
            mutableStateOf(null)
        }
        var filter by rememberSaveable { mutableStateOf("") }
        val roomSummaries by client
            .roomSummaryDataSource
            .roomSummaries()
            .collectAsState()

        val networkConnectionStatus by networkMonitor.connectivity.collectAsState(initial = networkMonitor.currentConnectivityStatus)

        Timber.v("RoomSummaries size = ${roomSummaries.size}")

        val mappedRoomSummaries: MutableState<ImmutableList<RoomListRoomSummary>> = remember { mutableStateOf(persistentListOf()) }
        val filteredRoomSummaries: MutableState<ImmutableList<RoomListRoomSummary>> = remember {
            mutableStateOf(persistentListOf())
        }
        LaunchedEffect(Unit) {
            initialLoad(matrixUser)
        }

        // Session verification status (unknown, not verified, verified)
        val sessionVerifiedStatus by sessionVerificationService.sessionVerifiedStatus.collectAsState()
        var verificationPromptDismissed by rememberSaveable { mutableStateOf(false) }
        // We combine both values to only display the prompt if the session is not verified and it wasn't dismissed
        val displayVerificationPrompt by remember {
            derivedStateOf { sessionVerifiedStatus == SessionVerifiedStatus.NotVerified && !verificationPromptDismissed }
        }

        var displaySearchResults by rememberSaveable { mutableStateOf(false) }

        var contextMenu by remember { mutableStateOf<RoomListState.ContextMenu>(RoomListState.ContextMenu.Hidden) }

        fun handleEvents(event: RoomListEvents) {
            when (event) {
                is RoomListEvents.UpdateFilter -> filter = event.newFilter
                is RoomListEvents.UpdateVisibleRange -> updateVisibleRange(event.range)
                RoomListEvents.DismissRequestVerificationPrompt -> verificationPromptDismissed = true
                RoomListEvents.ToggleSearchResults -> {
                    if (displaySearchResults) {
                        filter = ""
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

        LaunchedEffect(roomSummaries, filter) {
            mappedRoomSummaries.value = if (roomSummaries.isEmpty()) {
                RoomListRoomSummaryPlaceholders.createFakeList(16).toImmutableList()
            } else {
                mapRoomSummaries(roomSummaries).toImmutableList()
            }
            filteredRoomSummaries.value = updateFilteredRoomSummaries(mappedRoomSummaries.value, filter)
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        return RoomListState(
            matrixUser = matrixUser.value,
            roomList = mappedRoomSummaries.value,
            filter = filter,
            filteredRoomList = filteredRoomSummaries.value,
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

    private fun updateFilteredRoomSummaries(mappedRoomSummaries: ImmutableList<RoomListRoomSummary>, filter: String): ImmutableList<RoomListRoomSummary> {
        return when {
            filter.isEmpty() -> emptyList()
            else -> mappedRoomSummaries.filter { it.name.contains(filter, ignoreCase = true) }
        }.toImmutableList()
    }

    private fun CoroutineScope.initialLoad(matrixUser: MutableState<MatrixUser?>) = launch {
        val userAvatarUrl = client.loadUserAvatarURLString().getOrNull()
        val userDisplayName = client.loadUserDisplayName().getOrNull()
        matrixUser.value = MatrixUser(
            userId = UserId(client.sessionId.value),
            displayName = userDisplayName,
            avatarUrl = userAvatarUrl,
        )
    }

    private fun updateVisibleRange(range: IntRange) {
        if (range.isEmpty()) return
        val midExtendedRangeSize = extendedRangeSize / 2
        val extendedRangeStart = (range.first - midExtendedRangeSize).coerceAtLeast(0)
        // Safe to give bigger size than room list
        val extendedRangeEnd = range.last + midExtendedRangeSize
        val extendedRange = IntRange(extendedRangeStart, extendedRangeEnd)
        client.roomSummaryDataSource.setSlidingSyncRange(extendedRange)
    }

    private suspend fun mapRoomSummaries(
        roomSummaries: List<RoomSummary>
    ): List<RoomListRoomSummary> {
        return roomSummaries.parallelMap { roomSummary ->
            when (roomSummary) {
                is RoomSummary.Empty -> RoomListRoomSummaryPlaceholders.create(roomSummary.identifier)
                is RoomSummary.Filled -> {
                    val avatarData = AvatarData(
                        id = roomSummary.identifier(),
                        name = roomSummary.details.name,
                        url = roomSummary.details.avatarURLString,
                        size = AvatarSize.RoomListItem,
                    )
                    val roomIdentifier = roomSummary.identifier()
                    RoomListRoomSummary(
                        id = roomSummary.identifier(),
                        roomId = RoomId(roomIdentifier),
                        name = roomSummary.details.name,
                        hasUnread = roomSummary.details.unreadNotificationCount > 0,
                        timestamp = lastMessageTimestampFormatter.format(roomSummary.details.lastMessageTimestamp),
                        lastMessage = roomSummary.details.lastMessage?.let { message ->
                            roomLastMessageFormatter.format(message.event, roomSummary.details.isDirect)
                        }.orEmpty(),
                        avatarData = avatarData,
                    )
                }
            }
        }
    }
}
