/*
 * Copyright (c) 2022 New Vector Ltd
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

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.roomlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.roomlist.components.RoomListTopBar
import io.element.android.x.features.roomlist.components.RoomSummaryRow
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.features.roomlist.model.RoomListViewState
import io.element.android.x.features.roomlist.model.stubbedRoomSummaries
import io.element.android.x.matrix.core.RoomId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun RoomListScreen(
    viewModel: RoomListViewModel = mavericksViewModel(),
    onRoomClicked: (RoomId) -> Unit = { },
    onOpenSettings: () -> Unit = { },
) {
    val filter by viewModel.collectAsState(RoomListViewState::filter)
    LogCompositions(tag = "RoomListScreen", msg = "Root")
    val roomSummaries by viewModel.collectAsState(RoomListViewState::rooms)
    val matrixUser by viewModel.collectAsState(RoomListViewState::user)
    RoomListContent(
        roomSummaries = roomSummaries().orEmpty().toImmutableList(),
        matrixUser = matrixUser(),
        onRoomClicked = onRoomClicked,
        onOpenSettings = onOpenSettings,
        filter = filter,
        onFilterChanged = viewModel::filterRoom,
        onScrollOver = viewModel::updateVisibleRange
    )
}

@Composable
fun RoomListContent(
    roomSummaries: ImmutableList<RoomListRoomSummary>,
    matrixUser: MatrixUser?,
    filter: String,
    modifier: Modifier = Modifier,
    onRoomClicked: (RoomId) -> Unit = {},
    onFilterChanged: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onScrollOver: (IntRange) -> Unit = {},
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
    LogCompositions(tag = "RoomListScreen", msg = "Content")

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                onScrollOver(visibleRange)
                return super.onPostFling(consumed, available)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RoomListTopBar(
                matrixUser = matrixUser,
                filter = filter,
                onFilterChanged = onFilterChanged,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior
            )
        },
        content = { padding ->
            Column(modifier = Modifier.padding(padding)) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .nestedScroll(nestedScrollConnection),
                    state = lazyListState,
                ) {
                    items(
                        items = roomSummaries,
                        contentType = { room -> room.contentType() },
                    ) { room ->
                        RoomSummaryRow(room = room, onClick = ::onRoomClicked)
                    }
                }
            }
        }
    )
}

private fun RoomListRoomSummary.contentType() = isPlaceholder

private fun LazyListState.isScrolled(): Boolean {
    return firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0
}

@Preview
@Composable
fun PreviewableRoomListContent() {
    ElementXTheme(darkTheme = false) {
        RoomListContent(
            roomSummaries = stubbedRoomSummaries(),
            matrixUser = MatrixUser("User#1", avatarData = AvatarData("U")),
            onRoomClicked = {},
            filter = "filter",
            onFilterChanged = {},
            onScrollOver = {}
        )
    }
}

@Preview
@Composable
fun PreviewableDarkRoomListContent() {
    ElementXTheme(darkTheme = true) {
        RoomListContent(
            roomSummaries = stubbedRoomSummaries(),
            matrixUser = MatrixUser("User#1", avatarData = AvatarData("U")),
            onRoomClicked = {},
            filter = "filter",
            onFilterChanged = {},
            onScrollOver = {}
        )
    }
}
