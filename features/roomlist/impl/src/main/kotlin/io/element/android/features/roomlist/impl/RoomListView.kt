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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorContainer
import io.element.android.features.roomlist.impl.components.ConfirmRecoveryKeyBanner
import io.element.android.features.roomlist.impl.components.RequestVerificationHeader
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.components.RoomListTopBar
import io.element.android.features.roomlist.impl.components.RoomSummaryRow
import io.element.android.features.roomlist.impl.filters.RoomListFiltersView
import io.element.android.features.roomlist.impl.migration.MigrationScreenView
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomListView(
    state: RoomListState,
    onRoomClicked: (RoomId) -> Unit,
    onSettingsClicked: () -> Unit,
    onVerifyClicked: () -> Unit,
    onConfirmRecoveryKeyClicked: () -> Unit,
    onCreateRoomClicked: () -> Unit,
    onInvitesClicked: () -> Unit,
    onRoomSettingsClicked: (roomId: RoomId) -> Unit,
    onMenuActionClicked: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConnectivityIndicatorContainer(
        modifier = modifier,
        isOnline = state.hasNetworkConnection,
    ) { topPadding ->
        Box {
            fun onRoomLongClicked(
                roomListRoomSummary: RoomListRoomSummary
            ) {
                state.eventSink(RoomListEvents.ShowContextMenu(roomListRoomSummary))
            }

            if (state.contextMenu is RoomListState.ContextMenu.Shown) {
                RoomListContextMenu(
                    contextMenu = state.contextMenu,
                    eventSink = state.eventSink,
                    onRoomSettingsClicked = onRoomSettingsClicked,
                )
            }

            LeaveRoomView(state = state.leaveRoomState)

            RoomListContent(
                modifier = Modifier.padding(top = topPadding),
                state = state,
                onVerifyClicked = onVerifyClicked,
                onConfirmRecoveryKeyClicked = onConfirmRecoveryKeyClicked,
                onRoomClicked = onRoomClicked,
                onRoomLongClicked = { onRoomLongClicked(it) },
                onOpenSettings = onSettingsClicked,
                onCreateRoomClicked = onCreateRoomClicked,
                onInvitesClicked = onInvitesClicked,
                onMenuActionClicked = onMenuActionClicked,
            )
            // This overlaid view will only be visible when state.displaySearchResults is true
            RoomListSearchView(
                state = state.searchState,
                onRoomClicked = onRoomClicked,
                onRoomLongClicked = { onRoomLongClicked(it) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = topPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@Composable
private fun EmptyRoomListView(
    onCreateRoomClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.screen_roomlist_empty_title),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.screen_roomlist_empty_message),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            text = stringResource(CommonStrings.action_start_chat),
            leadingIcon = IconSource.Vector(CompoundIcons.Compose()),
            onClick = onCreateRoomClicked,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListContent(
    state: RoomListState,
    onVerifyClicked: () -> Unit,
    onConfirmRecoveryKeyClicked: () -> Unit,
    onRoomClicked: (RoomId) -> Unit,
    onRoomLongClicked: (RoomListRoomSummary) -> Unit,
    onOpenSettings: () -> Unit,
    onCreateRoomClicked: () -> Unit,
    onInvitesClicked: () -> Unit,
    onMenuActionClicked: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRoomClicked(room: RoomListRoomSummary) {
        onRoomClicked(room.roomId)
    }

    val appBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()

    val visibleRange by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val firstItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val size = layoutInfo.visibleItemsInfo.size
            firstItemIndex until firstItemIndex + size
        }
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                state.eventSink(RoomListEvents.UpdateVisibleRange(visibleRange))
                return super.onPostFling(consumed, available)
            }
        }
    }

    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                RoomListTopBar(
                    matrixUser = state.matrixUser,
                    showAvatarIndicator = state.showAvatarIndicator,
                    areSearchResultsDisplayed = state.searchState.isSearchActive,
                    onToggleSearch = { state.eventSink(RoomListEvents.ToggleSearchResults) },
                    onMenuActionClicked = onMenuActionClicked,
                    onOpenSettings = onOpenSettings,
                    scrollBehavior = scrollBehavior,
                    displayMenuItems = !state.displayMigrationStatus,
                )
                if (state.displayFilters) {
                    RoomListFiltersView(state = state.filtersState)
                }
            }
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .nestedScroll(nestedScrollConnection),
                state = lazyListState,
                // FAB height is 56dp, bottom padding is 16dp, we add 8dp as extra margin -> 56+16+8 = 80
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                when {
                    state.displayEmptyState -> Unit
                    state.securityBannerState == SecurityBannerState.SessionVerification -> {
                        item {
                            RequestVerificationHeader(
                                onVerifyClicked = onVerifyClicked,
                                onDismissClicked = { state.eventSink(RoomListEvents.DismissRequestVerificationPrompt) }
                            )
                        }
                    }
                    state.securityBannerState == SecurityBannerState.RecoveryKeyConfirmation -> {
                        item {
                            ConfirmRecoveryKeyBanner(
                                onContinueClicked = onConfirmRecoveryKeyClicked,
                                onDismissClicked = { state.eventSink(RoomListEvents.DismissRecoveryKeyPrompt) }
                            )
                        }
                    }
                }

                if (state.invitesState != InvitesState.NoInvites) {
                    item {
                        InvitesEntryPointView(onInvitesClicked, state.invitesState)
                    }
                }

                val roomList = state.roomList.dataOrNull().orEmpty()
                // Note: do not use a key for the LazyColumn, or the scroll will not behave as expected if a room
                // is moved to the top of the list.
                itemsIndexed(
                    items = roomList,
                    contentType = { _, room -> room.contentType() },
                ) { index, room ->
                    RoomSummaryRow(
                        room = room,
                        onClick = ::onRoomClicked,
                        onLongClick = onRoomLongClicked,
                    )
                    if (index != roomList.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
            if (state.displayEmptyState) {
                if (state.filtersState.hasAnyFilterSelected) {
                    // TODO add empty state for filtered rooms
                } else {
                    EmptyRoomListView(onCreateRoomClicked)
                }
            }
            MigrationScreenView(isMigrating = state.displayMigrationStatus)
        },
        floatingActionButton = {
            if (!state.displayMigrationStatus) {
                FloatingActionButton(
                    // FIXME align on Design system theme
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = onCreateRoomClicked
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

internal fun RoomListRoomSummary.contentType() = isPlaceholder

@PreviewsDayNight
@Composable
internal fun RoomListViewPreview(@PreviewParameter(RoomListStateProvider::class) state: RoomListState) = ElementPreview {
    RoomListView(
        state = state,
        onRoomClicked = {},
        onSettingsClicked = {},
        onVerifyClicked = {},
        onConfirmRecoveryKeyClicked = {},
        onCreateRoomClicked = {},
        onInvitesClicked = {},
        onRoomSettingsClicked = {},
        onMenuActionClicked = {},
    )
}
