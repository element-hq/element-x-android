@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.roomlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.ProgressDialog
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.roomlist.components.RoomListTopBar
import io.element.android.x.features.roomlist.components.RoomSummaryRow
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.features.roomlist.model.RoomListState
import io.element.android.x.features.roomlist.model.stubbedRoomSummaries
import io.element.android.x.matrix.core.RoomId
import kotlinx.collections.immutable.ImmutableList

@Composable
fun RoomListView(
    state: RoomListState,
    modifier: Modifier = Modifier,
    onRoomClicked: (RoomId) -> Unit = {},
    onFilterChanged: (String) -> Unit = {},
    onLogoutClicked: () -> Unit = {},
    onScrollOver: (IntRange) -> Unit = {},
) {
    RoomListView(
        roomSummaries = state.roomList,
        matrixUser = state.matrixUser,
        filter = state.filter,
        isLoginOut = state.isLoginOut,
        modifier = modifier,
        onRoomClicked = onRoomClicked,
        onFilterChanged = onFilterChanged,
        onLogoutClicked = onLogoutClicked,
        onScrollOver = onScrollOver
    )
}

@Composable
fun RoomListView(
    roomSummaries: ImmutableList<RoomListRoomSummary>,
    matrixUser: MatrixUser?,
    filter: String,
    isLoginOut: Boolean,
    modifier: Modifier = Modifier,
    onRoomClicked: (RoomId) -> Unit = {},
    onFilterChanged: (String) -> Unit = {},
    onLogoutClicked: () -> Unit = {},
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
                onLogoutClicked = onLogoutClicked,
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
    if (isLoginOut) {
        ProgressDialog(text = "Login out...")
    }
}

private fun RoomListRoomSummary.contentType() = isPlaceholder

@Preview
@Composable
fun PreviewableRoomListView() {
    ElementXTheme(darkTheme = false) {
        RoomListView(
            roomSummaries = stubbedRoomSummaries(),
            matrixUser = MatrixUser("User#1", avatarData = AvatarData("U")),
            onRoomClicked = {},
            onLogoutClicked = {},
            filter = "filter",
            onFilterChanged = {},
            isLoginOut = false,
            onScrollOver = {}
        )
    }
}

@Preview
@Composable
fun PreviewableDarkRoomListView() {
    ElementXTheme(darkTheme = true) {
        RoomListView(
            roomSummaries = stubbedRoomSummaries(),
            matrixUser = MatrixUser("User#1", avatarData = AvatarData("U")),
            onRoomClicked = {},
            onLogoutClicked = {},
            filter = "filter",
            onFilterChanged = {},
            isLoginOut = true,
            onScrollOver = {}
        )
    }
}
