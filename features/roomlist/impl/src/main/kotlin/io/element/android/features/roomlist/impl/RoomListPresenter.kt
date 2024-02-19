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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomPresenter
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.features.roomlist.impl.datasource.InviteStateDataSource
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.migration.MigrationScreenPresenter
import io.element.android.features.roomlist.impl.search.RoomListSearchEvents
import io.element.android.features.roomlist.impl.search.RoomListSearchState
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.indicator.api.IndicatorService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.getCurrentUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val EXTENDED_RANGE_SIZE = 40

class RoomListPresenter @Inject constructor(
    private val client: MatrixClient,
    private val sessionVerificationService: SessionVerificationService,
    private val networkMonitor: NetworkMonitor,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val inviteStateDataSource: InviteStateDataSource,
    private val leaveRoomPresenter: LeaveRoomPresenter,
    private val roomListDataSource: RoomListDataSource,
    private val featureFlagService: FeatureFlagService,
    private val indicatorService: IndicatorService,
    private val searchPresenter: Presenter<RoomListSearchState>,
    private val migrationScreenPresenter: MigrationScreenPresenter,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : Presenter<RoomListState> {
    private val encryptionService: EncryptionService = client.encryptionService()

    @Composable
    override fun present(): RoomListState {
        val coroutineScope = rememberCoroutineScope()
        val leaveRoomState = leaveRoomPresenter.present()
        val matrixUser: MutableState<MatrixUser?> = rememberSaveable {
            mutableStateOf(null)
        }
        val roomList by produceState(initialValue = AsyncData.Loading()) {
            roomListDataSource.allRooms.collect { value = AsyncData.Success(it) }
        }
        val networkConnectionStatus by networkMonitor.connectivity.collectAsState()
        val searchState = searchPresenter.present()

        LaunchedEffect(Unit) {
            roomListDataSource.launchIn(this)
            initialLoad(matrixUser)
        }

        val isMigrating = migrationScreenPresenter.present().isMigrating

        // Session verification status (unknown, not verified, verified)
        val canVerifySession by sessionVerificationService.canVerifySessionFlow.collectAsState(initial = false)
        var verificationPromptDismissed by rememberSaveable { mutableStateOf(false) }
        // We combine both values to only display the prompt if the session is not verified and it wasn't dismissed
        val displayVerificationPrompt by remember {
            derivedStateOf { canVerifySession && !verificationPromptDismissed }
        }
        val recoveryState by encryptionService.recoveryStateStateFlow.collectAsState()
        val secureStorageFlag by featureFlagService.isFeatureEnabledFlow(FeatureFlags.SecureStorage)
            .collectAsState(initial = null)
        var recoveryKeyPromptDismissed by rememberSaveable { mutableStateOf(false) }
        val displayRecoveryKeyPrompt by remember {
            derivedStateOf {
                secureStorageFlag == true &&
                    recoveryState == RecoveryState.INCOMPLETE &&
                    !recoveryKeyPromptDismissed
            }
        }

        // Avatar indicator
        val showAvatarIndicator by indicatorService.showRoomListTopBarIndicator()

        val contextMenu = remember { mutableStateOf<RoomListState.ContextMenu>(RoomListState.ContextMenu.Hidden) }

        fun handleEvents(event: RoomListEvents) {
            when (event) {
                is RoomListEvents.UpdateVisibleRange -> updateVisibleRange(event.range)
                RoomListEvents.DismissRequestVerificationPrompt -> verificationPromptDismissed = true
                RoomListEvents.DismissRecoveryKeyPrompt -> recoveryKeyPromptDismissed = true
                RoomListEvents.ToggleSearchResults -> searchState.eventSink(RoomListSearchEvents.ToggleSearchVisibility)
                is RoomListEvents.ShowContextMenu -> {
                    coroutineScope.showContextMenu(event, contextMenu)
                }
                is RoomListEvents.HideContextMenu -> {
                    contextMenu.value = RoomListState.ContextMenu.Hidden
                }
                is RoomListEvents.LeaveRoom -> leaveRoomState.eventSink(LeaveRoomEvent.ShowConfirmation(event.roomId))
                is RoomListEvents.SetRoomIsFavorite -> coroutineScope.launch {
                    client.getRoom(event.roomId)?.use { room ->
                        room.setIsFavorite(event.isFavorite)
                    }
                }
                is RoomListEvents.MarkAsRead -> coroutineScope.launch {
                    client.getRoom(event.roomId)?.use { room ->
                        room.setUnreadFlag(isUnread = false)
                        val receiptType = if (sessionPreferencesStore.isSendPublicReadReceiptsEnabled().first()) {
                            ReceiptType.READ
                        } else {
                            ReceiptType.READ_PRIVATE
                        }
                        room.markAsRead(receiptType)
                    }
                }
                is RoomListEvents.MarkAsUnread -> coroutineScope.launch {
                    client.getRoom(event.roomId)?.use { room ->
                        room.setUnreadFlag(isUnread = true)
                    }
                }
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        return RoomListState(
            matrixUser = matrixUser.value,
            showAvatarIndicator = showAvatarIndicator,
            roomList = roomList,
            displayVerificationPrompt = displayVerificationPrompt,
            displayRecoveryKeyPrompt = displayRecoveryKeyPrompt,
            snackbarMessage = snackbarMessage,
            hasNetworkConnection = networkConnectionStatus == NetworkStatus.Online,
            invitesState = inviteStateDataSource.inviteState(),
            contextMenu = contextMenu.value,
            leaveRoomState = leaveRoomState,
            searchState = searchState,
            displayMigrationStatus = isMigrating,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.initialLoad(matrixUser: MutableState<MatrixUser?>) = launch {
        matrixUser.value = client.getCurrentUser()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun CoroutineScope.showContextMenu(event: RoomListEvents.ShowContextMenu, contextMenuState: MutableState<RoomListState.ContextMenu>) = launch {
        val initialState = RoomListState.ContextMenu.Shown(
            roomId = event.roomListRoomSummary.roomId,
            roomName = event.roomListRoomSummary.name,
            isDm = event.roomListRoomSummary.isDm,
            isFavorite = event.roomListRoomSummary.isFavorite,
            markAsUnreadFeatureFlagEnabled = featureFlagService.isFeatureEnabled(FeatureFlags.MarkAsUnread),
            hasNewContent = event.roomListRoomSummary.hasNewContent
        )
        contextMenuState.value = initialState

        client.getRoom(event.roomListRoomSummary.roomId)?.use { room ->

            val isShowingContextMenuFlow = snapshotFlow { contextMenuState.value is RoomListState.ContextMenu.Shown }
                .distinctUntilChanged()

            val isFavoriteFlow = room.roomInfoFlow
                .map { it.isFavorite }
                .distinctUntilChanged()

            isFavoriteFlow
                .onEach { isFavorite ->
                    contextMenuState.value = initialState.copy(isFavorite = isFavorite)
                }
                .flatMapLatest { isShowingContextMenuFlow }
                .takeWhile { isShowingContextMenu -> isShowingContextMenu }
                .collect()
        }
    }

    private fun updateVisibleRange(range: IntRange) {
        if (range.isEmpty()) return
        val midExtendedRangeSize = EXTENDED_RANGE_SIZE / 2
        val extendedRangeStart = (range.first - midExtendedRangeSize).coerceAtLeast(0)
        // Safe to give bigger size than room list
        val extendedRangeEnd = range.last + midExtendedRangeSize
        val extendedRange = IntRange(extendedRangeStart, extendedRangeEnd)
        client.roomListService.updateAllRoomsVisibleRange(extendedRange)
    }
}
