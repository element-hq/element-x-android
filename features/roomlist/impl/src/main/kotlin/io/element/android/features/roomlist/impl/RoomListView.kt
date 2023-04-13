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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.element.android.features.roomlist.impl.components.RoomListTopBar
import io.element.android.features.roomlist.impl.components.RoomSummaryRow
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorView
import kotlinx.coroutines.launch
import io.element.android.libraries.designsystem.R as DrawableR
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun RoomListView(
    state: RoomListState,
    modifier: Modifier = Modifier,
    onRoomClicked: (RoomId) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onVerifyClicked: () -> Unit = {},
    onCreateRoomClicked: () -> Unit = {},
) {
    RoomListContent(
        state = state,
        modifier = modifier,
        onRoomClicked = onRoomClicked,
        onOpenSettings = onOpenSettings,
        onVerifyClicked = onVerifyClicked,
        onCreateRoomClicked = onCreateRoomClicked,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoomListContent(
    state: RoomListState,
    modifier: Modifier = Modifier,
    onVerifyClicked: () -> Unit = {},
    onRoomClicked: (RoomId) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onCreateRoomClicked: () -> Unit = {},
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

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessageText = if (state.snackbarMessage != null ) {
        stringResource(state.snackbarMessage.messageResId)
    } else null
    val coroutineScope = rememberCoroutineScope()
    if (snackbarMessageText != null) {
        SideEffect {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = snackbarMessageText,
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                ConnectivityIndicatorView(isOnline = state.hasNetworkConnection)
                RoomListTopBar(
                    matrixUser = state.matrixUser,
                    filter = state.filter,
                    onFilterChanged = { state.eventSink(RoomListEvents.UpdateFilter(it)) },
                    onOpenSettings = onOpenSettings,
                    scrollBehavior = scrollBehavior,
                    useStatusBarInsets = state.hasNetworkConnection,
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
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
                    items(
                        items = state.roomList,
                        contentType = { room -> room.contentType() },
                    ) { room ->
                        RoomSummaryRow(room = room, onClick = ::onRoomClicked)
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
                Icon(resourceId = DrawableR.drawable.ic_edit_square, contentDescription = stringResource(id = R.string.screen_roomlist_a11y_create_message))
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

@Composable
internal fun RequestVerificationHeader(
    onVerifyClicked: () -> Unit,
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Surface(
            modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row {
                    Text(
                        stringResource(R.string.session_verification_banner_title),
                        modifier = Modifier.weight(1f),
                        style = ElementTextStyles.Bold.body,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Start,
                    )
                    Icon(
                        modifier = Modifier.clickable(onClick = onDismissClicked),
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(StringR.string.action_close)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(stringResource(R.string.session_verification_banner_message), style = ElementTextStyles.Regular.bodyMD)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 7.dp),
                    onClick = onVerifyClicked,
                ) {
                    Text(stringResource(StringR.string.action_continue), style = ElementTextStyles.Button)
                }
            }
        }
    }
}

@Preview
@Composable
internal fun PreviewRequestVerificationHeaderLight() {
    ElementPreviewLight {
        RequestVerificationHeader(onVerifyClicked = {}, onDismissClicked = {})
    }
}

@Preview
@Composable
internal fun PreviewRequestVerificationHeaderDark() {
    ElementPreviewDark {
        RequestVerificationHeader(onVerifyClicked = {}, onDismissClicked = {})
    }
}

private fun RoomListRoomSummary.contentType() = isPlaceholder

@Preview
@Composable
internal fun RoomListViewLightPreview(@PreviewParameter(RoomListStateProvider::class) state: RoomListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun RoomListViewDarkPreview(@PreviewParameter(RoomListStateProvider::class) state: RoomListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomListState) {
    RoomListView(state)
}
