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

package io.element.android.features.roomlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
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
import io.element.android.features.roomlist.components.RoomListTopBar
import io.element.android.features.roomlist.components.RoomSummaryRow
import io.element.android.features.roomlist.model.RoomListEvents
import io.element.android.features.roomlist.model.RoomListRoomSummary
import io.element.android.features.roomlist.model.RoomListState
import io.element.android.features.roomlist.model.stubbedRoomSummaries
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.matrix.core.RoomId
import io.element.android.libraries.matrix.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RoomListView(
    state: RoomListState,
    modifier: Modifier = Modifier,
    onRoomClicked: (RoomId) -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    fun onFilterChanged(filter: String) {
        state.eventSink(RoomListEvents.UpdateFilter(filter))
    }

    fun onVisibleRangedChanged(range: IntRange) {
        state.eventSink(RoomListEvents.UpdateVisibleRange(range))
    }

    RoomListContent(
        roomSummaries = state.roomList,
        matrixUser = state.matrixUser,
        filter = state.filter,
        modifier = modifier,
        onRoomClicked = onRoomClicked,
        onFilterChanged = ::onFilterChanged,
        onOpenSettings = onOpenSettings,
        onScrollOver = ::onVisibleRangedChanged,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    LogCompositions(
        tag = "RoomListScreen",
        msg = "Content"
    )

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
                scrollBehavior = scrollBehavior,
                modifier = Modifier,
            )
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

@Preview
@Composable
fun RoomListViewLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun RoomListViewDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    RoomListContent(
        roomSummaries = stubbedRoomSummaries(),
        matrixUser = MatrixUser(id = UserId("@id"), username = "User#1", avatarData = AvatarData("U")),
        onRoomClicked = {},
        filter = "filter",
        onFilterChanged = {},
        onScrollOver = {}
    )
}
