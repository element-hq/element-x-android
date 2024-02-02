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
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorContainer
import io.element.android.features.roomlist.impl.components.ConfirmRecoveryKeyBanner
import io.element.android.features.roomlist.impl.components.RequestVerificationHeader
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.components.RoomListTopBar
import io.element.android.features.roomlist.impl.components.RoomSummaryRow
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchResultView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.designsystem.utils.LogCompositions
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
                onRoomClicked = onRoomClicked,
                onRoomLongClicked = { onRoomLongClicked(it) },
                onOpenSettings = onSettingsClicked,
                onCreateRoomClicked = onCreateRoomClicked,
                onInvitesClicked = onInvitesClicked,
                onMenuActionClicked = onMenuActionClicked,
            )
            // This overlaid view will only be visible when state.displaySearchResults is true
            RoomListSearchResultView(
                state = state,
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
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            leadingIcon = IconSource.Resource(CommonDrawables.ic_new_message),
            onClick = onCreateRoomClicked,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListContent(
    state: RoomListState,
    onVerifyClicked: () -> Unit,
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
    LogCompositions(
        tag = "RoomListScreen",
        msg = "Content"
    )

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
            RoomListTopBar(
                matrixUser = state.matrixUser,
                showAvatarIndicator = state.showAvatarIndicator,
                areSearchResultsDisplayed = state.displaySearchResults,
                onFilterChanged = { state.eventSink(RoomListEvents.UpdateFilter(it)) },
                onToggleSearch = { state.eventSink(RoomListEvents.ToggleSearchResults) },
                onMenuActionClicked = onMenuActionClicked,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
            )
        },
        content = { padding ->
            println(state.roomList)
            if (state.roomList is AsyncData.Success && state.roomList.data.isEmpty()) {
                EmptyRoomListView(onCreateRoomClicked)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .nestedScroll(nestedScrollConnection),
                    state = lazyListState,
                ) {
                    when {
                        state.displayVerificationPrompt -> {
                            item {
                                RequestVerificationHeader(
                                    onVerifyClicked = onVerifyClicked,
                                    onDismissClicked = { state.eventSink(RoomListEvents.DismissRequestVerificationPrompt) }
                                )
                            }
                        }
                        state.displayRecoveryKeyPrompt -> {
                            item {
                                ConfirmRecoveryKeyBanner(
                                    onContinueClicked = onOpenSettings,
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
                    // Add a last Spacer item to ensure that the FAB does not hide the last room item
                    // FAB height is 56dp, bottom padding is 16dp, we add 8dp as extra margin -> 56+16+8 = 80
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                // FIXME align on Design system theme
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = onCreateRoomClicked
            ) {
                Icon(
                    // Note cannot use Icons.Outlined.EditSquare, it does not exist :/
                    resourceId = CommonDrawables.ic_new_message,
                    contentDescription = stringResource(id = R.string.screen_roomlist_a11y_create_message)
                )
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
        onCreateRoomClicked = {},
        onInvitesClicked = {},
        onRoomSettingsClicked = {},
        onMenuActionClicked = {},
    )
}
