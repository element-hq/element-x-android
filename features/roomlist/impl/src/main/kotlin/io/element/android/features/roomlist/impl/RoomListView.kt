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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorContainer
import io.element.android.features.roomlist.impl.components.RoomListContentView
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.components.RoomListTopBar
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.RoomId

@Composable
fun RoomListView(
    state: RoomListState,
    onRoomClick: (RoomId) -> Unit,
    onSettingsClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onRoomSettingsClick: (roomId: RoomId) -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onRoomDirectorySearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    acceptDeclineInviteView: @Composable () -> Unit,
) {
    ConnectivityIndicatorContainer(
        modifier = modifier,
        isOnline = state.hasNetworkConnection,
    ) { topPadding ->
        Box {
            if (state.contextMenu is RoomListState.ContextMenu.Shown) {
                RoomListContextMenu(
                    contextMenu = state.contextMenu,
                    eventSink = state.eventSink,
                    onRoomSettingsClick = onRoomSettingsClick,
                )
            }

            LeaveRoomView(state = state.leaveRoomState)

            RoomListScaffold(
                state = state,
                onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                onRoomClick = onRoomClick,
                onOpenSettings = onSettingsClick,
                onCreateRoomClick = onCreateRoomClick,
                onMenuActionClick = onMenuActionClick,
                modifier = Modifier.padding(top = topPadding),
            )
            // This overlaid view will only be visible when state.displaySearchResults is true
            RoomListSearchView(
                state = state.searchState,
                eventSink = state.eventSink,
                onRoomClick = onRoomClick,
                onRoomDirectorySearchClick = onRoomDirectorySearchClick,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = topPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
            acceptDeclineInviteView()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListScaffold(
    state: RoomListState,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onOpenSettings: () -> Unit,
    onCreateRoomClick: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRoomClick(room: RoomListRoomSummary) {
        onRoomClick(room.roomId)
    }

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RoomListTopBar(
                matrixUser = state.matrixUser,
                showAvatarIndicator = state.showAvatarIndicator,
                areSearchResultsDisplayed = state.searchState.isSearchActive,
                onToggleSearch = { state.eventSink(RoomListEvents.ToggleSearchResults) },
                onMenuActionClick = onMenuActionClick,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
                displayMenuItems = state.displayActions,
                displayFilters = state.displayFilters,
                filtersState = state.filtersState,
            )
        },
        content = { padding ->
            RoomListContentView(
                contentState = state.contentState,
                filtersState = state.filtersState,
                eventSink = state.eventSink,
                onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                onRoomClick = ::onRoomClick,
                onCreateRoomClick = onCreateRoomClick,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            )
        },
        floatingActionButton = {
            if (state.displayActions) {
                FloatingActionButton(
                    // FIXME align on Design system theme
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = onCreateRoomClick
                ) {
                    Icon(
                        // Note cannot use Icons.Outlined.EditSquare, it does not exist :/
                        imageVector = CompoundIcons.Compose(),
                        contentDescription = stringResource(id = R.string.screen_roomlist_a11y_create_message)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

internal fun RoomListRoomSummary.contentType() = displayType.ordinal

@PreviewsDayNight
@Composable
internal fun RoomListViewPreview(@PreviewParameter(RoomListStateProvider::class) state: RoomListState) = ElementPreview {
    RoomListView(
        state = state,
        onRoomClick = {},
        onSettingsClick = {},
        onConfirmRecoveryKeyClick = {},
        onCreateRoomClick = {},
        onRoomSettingsClick = {},
        onMenuActionClick = {},
        onRoomDirectorySearchClick = {},
        acceptDeclineInviteView = {},
    )
}
