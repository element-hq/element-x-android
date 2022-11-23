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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.designsystem.components.ProgressDialog
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.roomlist.components.RoomItem
import io.element.android.x.features.roomlist.components.RoomListTopBar
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.features.roomlist.model.RoomListViewState
import io.element.android.x.features.roomlist.model.stubbedRoomSummaries
import io.element.android.x.matrix.core.RoomId

@Composable
fun RoomListScreen(
    viewModel: RoomListViewModel = mavericksViewModel(),
    onSuccessLogout: () -> Unit = { },
    onRoomClicked: (RoomId) -> Unit = { }
) {
    val logoutAction by viewModel.collectAsState(RoomListViewState::logoutAction)
    val filter by viewModel.collectAsState(RoomListViewState::filter)
    if (logoutAction is Success) {
        onSuccessLogout()
        return
    }
    LogCompositions(tag = "RoomListScreen", msg = "Root")
    val roomSummaries by viewModel.collectAsState(RoomListViewState::rooms)
    val matrixUser by viewModel.collectAsState(RoomListViewState::user)
    RoomListContent(
        roomSummaries = roomSummaries().orEmpty(),
        matrixUser = matrixUser(),
        onRoomClicked = onRoomClicked,
        onLogoutClicked = viewModel::logout,
        isLoginOut = logoutAction is Loading,
        filter = filter,
        onFilterChanged = viewModel::filterRoom,
    )
}

@Composable
fun RoomListContent(
    roomSummaries: List<RoomListRoomSummary>,
    matrixUser: MatrixUser?,
    onRoomClicked: (RoomId) -> Unit,
    filter: String,
    onFilterChanged: (String) -> Unit,
    onLogoutClicked: () -> Unit,
    isLoginOut: Boolean,
) {
    val appBarState = rememberTopAppBarState()
    val lazyListState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    LogCompositions(tag = "RoomListScreen", msg = "Content")
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                    modifier = Modifier.weight(1f),
                    state = lazyListState,
                ) {
                    items(roomSummaries) { room ->
                        RoomItem(room = room) {
                            onRoomClicked(it)
                        }
                    }
                }
            }
        }
    )
    if (isLoginOut) {
        ProgressDialog("Login out...")
    }
}

private fun LazyListState.isScrolled(): Boolean {
    return firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0
}

@Preview
@Composable
private fun PreviewableRoomListContent() {
    ElementXTheme(darkTheme = false) {
        RoomListContent(
            roomSummaries = stubbedRoomSummaries(),
            matrixUser = MatrixUser("User#1", avatarData = AvatarData("U")),
            onRoomClicked = {},
            onLogoutClicked = {},
            filter = "filter",
            onFilterChanged = {},
            isLoginOut = false,
        )
    }
}

@Preview
@Composable
private fun PreviewableDarkRoomListContent() {
    ElementXTheme(darkTheme = true) {
        RoomListContent(
            roomSummaries = stubbedRoomSummaries(),
            matrixUser = MatrixUser("User#1", avatarData = AvatarData("U")),
            onRoomClicked = {},
            onLogoutClicked = {},
            filter = "filter",
            onFilterChanged = {},
            isLoginOut = true,
        )
    }
}

