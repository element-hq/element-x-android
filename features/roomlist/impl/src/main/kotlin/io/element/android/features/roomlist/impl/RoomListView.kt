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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.preview.ElementPreviews
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorView
import io.element.android.features.roomlist.impl.components.RequestVerificationHeader
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.components.RoomListTopBar
import io.element.android.features.roomlist.impl.components.RoomSummaryRow
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchResultView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.theme.components.Divider
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.designsystem.utils.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.designsystem.R as DrawableR

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
    Column(modifier = modifier) {
        ConnectivityIndicatorView(isOnline = state.hasNetworkConnection)
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
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomListContent(
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
                areSearchResultsDisplayed = state.displaySearchResults,
                onFilterChanged = { state.eventSink(RoomListEvents.UpdateFilter(it)) },
                onToggleSearch = { state.eventSink(RoomListEvents.ToggleSearchResults) },
                onMenuActionClicked = onMenuActionClicked,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .nestedScroll(nestedScrollConnection),
                state = lazyListState,
            ) {
                if (state.displayVerificationPrompt) {
                    item {
                        RequestVerificationHeader(
                            onVerifyClicked = onVerifyClicked,
                            onDismissClicked = { state.eventSink(RoomListEvents.DismissRequestVerificationPrompt) }
                        )
                    }
                }

                if (state.invitesState != InvitesState.NoInvites) {
                    item {
                        InvitesEntryPointView(onInvitesClicked, state.invitesState)
                    }
                }

                itemsIndexed(
                    items = state.roomList,
                    contentType = { _, room -> room.contentType() },
                ) { index, room ->
                    RoomSummaryRow(
                        room = room,
                        onClick = ::onRoomClicked,
                        onLongClick = onRoomLongClicked,
                    )
                    if (index != state.roomList.lastIndex) {
                        Divider()
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
                    // Correct icon alignment for better rendering.
                    modifier = Modifier.padding(start = 1.dp, bottom = 1.dp),
                    resourceId = DrawableR.drawable.ic_edit_square,
                    contentDescription = stringResource(id = R.string.screen_roomlist_a11y_create_message)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        },
    )
}

internal fun RoomListRoomSummary.contentType() = isPlaceholder

@ElementPreviews
@Composable
internal fun RoomListViewLightPreview(@PreviewParameter(RoomListStateProvider::class) state: RoomListState) {
    ElementPreview {
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
}

