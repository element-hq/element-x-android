/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.impl

import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.user.toMatrixUser
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.roomselect.api.RoomSelectMode
import io.element.android.libraries.sessionstorage.api.SessionStore
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import timber.log.Timber

@AssistedInject
class RoomSelectPresenter(
    @Assisted private val initialSessionId: SessionId,
    @Assisted private val mode: RoomSelectMode,
    @Assisted private val maxNumberOfRooms: Int,
    private val dataSourceFactory: RoomSelectSearchDataSource.Factory,
    private val matrixClientProvider: MatrixClientProvider,
    private val sessionStore: SessionStore,
) : Presenter<RoomSelectState> {
    @AssistedFactory
    fun interface Factory {
        fun create(
            initialSessionId: SessionId,
            mode: RoomSelectMode,
            maxNumberOfRooms: Int,
        ): RoomSelectPresenter
    }

    @Composable
    override fun present(): RoomSelectState {
        var selectedRooms by remember { mutableStateOf(persistentListOf<SelectRoomInfo>()) }
        val queryState = rememberTextFieldState()
        var isSearchActive by remember { mutableStateOf(false) }
        var isAccountListExpanded by remember { mutableStateOf(false) }
        var selectedSessionId by remember { mutableStateOf(initialSessionId) }
        val sessions by remember { sessionStore.sessionsFlow() }.collectAsState(initial = emptyList())
        // All the accounts are proposed, even the ones with an invalid token. Selecting them displays an error.
        val accounts by remember {
            derivedStateOf { sessions.map { it.toMatrixUser() } }
        }
        // The selected session can have an invalid token, or be invalidated while this screen is displayed, for instance if it is
        // signed out from another device. The invalidation of the initial session, which is the current one, is handled by the application.
        val isSelectedSessionInvalid by remember {
            derivedStateOf {
                selectedSessionId != initialSessionId && sessions.none { it.userId == selectedSessionId.value && it.isTokenValid }
            }
        }
        LaunchedEffect(isSelectedSessionInvalid) {
            if (isSelectedSessionInvalid) {
                // The rooms of an invalid session cannot be used
                selectedRooms = persistentListOf()
            }
        }
        val selectedAccount by remember {
            derivedStateOf {
                // Fallback to a MatrixUser with only the userId until the sessions are loaded
                accounts.find { it.userId == selectedSessionId } ?: MatrixUser(selectedSessionId)
            }
        }
        val otherAccounts by remember {
            derivedStateOf {
                accounts.filter { it.userId != selectedSessionId }.toImmutableList()
            }
        }

        val coroutineScope = rememberCoroutineScope()
        // Create one data source per session. When the selected session changes, the previous
        // data source is removed from the composition and its coroutine scope is cancelled.
        val (selectedSessionDataSource, hasClientError) = key(selectedSessionId) {
            val dataSourceCoroutineScope = rememberCoroutineScope()
            // The sessions are restored at startup, so the client should already be in memory.
            // A null value means that the client is being restored, or that the session is invalid: its client cannot be restored,
            // and an in-memory client would have been destroyed, so do not try to get it.
            val initialRoomListService = if (isSelectedSessionInvalid) null else matrixClientProvider.getOrNull(selectedSessionId)?.roomListService
            val roomListServiceResult by produceState(initialRoomListService?.let { Result.success(it) }) {
                if (value == null && !isSelectedSessionInvalid) {
                    value = matrixClientProvider.getOrRestore(selectedSessionId)
                        .onFailure { Timber.e(it, "Failed to get the client for $selectedSessionId") }
                        .map { it.roomListService }
                }
            }
            val roomListService = roomListServiceResult?.getOrNull()
            val dataSource = remember(roomListService) {
                roomListService?.let { dataSourceFactory.create(dataSourceCoroutineScope, it) }
            }
            dataSource to (roomListServiceResult?.isFailure == true)
        }
        // Do not use the data source of an invalid session, its client has been destroyed
        val dataSource = selectedSessionDataSource.takeIf { !isSelectedSessionInvalid }
        val hasRoomListError = hasClientError || isSelectedSessionInvalid

        val searchQuery = queryState.text.toString()
        LaunchedEffect(dataSource, searchQuery) {
            dataSource?.setSearchQuery(searchQuery)
        }

        // Use a key so that the rooms of the previously selected session are not rendered while loading the new ones
        val roomSummaryDetailsList by key(dataSource) {
            (dataSource?.roomInfoList ?: flowOf(persistentListOf())).collectAsState(initial = persistentListOf())
        }

        val searchResults by remember<State<SearchBarResultState<ImmutableList<SelectRoomInfo>>>>(dataSource) {
            derivedStateOf {
                when {
                    roomSummaryDetailsList.isNotEmpty() -> SearchBarResultState.Results(roomSummaryDetailsList.toImmutableList())
                    isSearchActive -> SearchBarResultState.NoResultsFound
                    else -> SearchBarResultState.Initial
                }
            }
        }

        fun handleEvent(event: RoomSelectEvent) {
            when (event) {
                is RoomSelectEvent.ToggleSelectedRoom -> {
                    val index = selectedRooms.indexOfFirst { it.roomId == event.room.roomId }
                    selectedRooms = if (index >= 0) {
                        selectedRooms.removingAt(index)
                    } else {
                        selectedRooms.adding(event.room)
                    }
                }
                RoomSelectEvent.ToggleSearchActive -> isSearchActive = !isSearchActive
                RoomSelectEvent.ToggleAccountListExpanded -> isAccountListExpanded = !isAccountListExpanded
                is RoomSelectEvent.SelectAccount -> {
                    selectedSessionId = event.sessionId
                    // The selected rooms and the search query are specific to the session
                    selectedRooms = persistentListOf()
                    queryState.clearText()
                    isAccountListExpanded = false
                }
                is RoomSelectEvent.UpdateVisibleRange -> coroutineScope.launch {
                    dataSource?.updateVisibleRange(event.range)
                }
            }
        }

        return RoomSelectState(
            mode = mode,
            maxNumberOfRooms = maxNumberOfRooms,
            resultState = searchResults,
            searchQuery = queryState,
            isSearchActive = isSearchActive,
            selectedRooms = selectedRooms,
            selectedAccount = selectedAccount,
            hasRoomListError = hasRoomListError,
            otherAccounts = otherAccounts,
            isAccountListExpanded = isAccountListExpanded,
            eventSink = ::handleEvent,
        )
    }
}
