package io.element.android.x.features.roomlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.designsystem.components.Avatar
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.room.RoomSummary

@Composable
fun RoomListScreen(
    viewModel: RoomListViewModel = mavericksViewModel(),
    onSuccessLogout: () -> Unit = { },
    onRoomClicked: (RoomId) -> Unit = { }
) {
    val state by viewModel.collectAsState()
    if (state.logoutAction is Success) {
        onSuccessLogout()
        return
    }
    RoomListContent(
        state = state,
        onRoomClicked = onRoomClicked,
        onLogoutClicked = {
            viewModel.handle(RoomListActions.Logout)
        }
    )
}

@Composable
fun RoomListContent(
    state: RoomListViewState,
    onRoomClicked: (RoomId) -> Unit,
    onLogoutClicked: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            RoomListTopBar(
                state = state,
                onLogoutClicked = onLogoutClicked
            )
            val rooms = state.rooms
            if (rooms is Success) {
                LazyColumn {
                    items(rooms()) { room ->
                        RoomItem(room = room) {
                            onRoomClicked(it)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListTopBar(state: RoomListViewState, onLogoutClicked: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val matrixUser = state.user
                Avatar(data = matrixUser.avatarData)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${matrixUser.username}")
            }
        },
        actions = {
            IconButton(
                onClick = onLogoutClicked
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "logout")
            }
        }
    )
}

@Composable
private fun RoomItem(
    modifier: Modifier = Modifier,
    room: RoomSummary,
    onClick: (RoomId) -> Unit
) {
    if (room !is RoomSummary.Filled) {
        return
    }
    val details = room.details
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                onClick(room.details.roomId)
            }
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(modifier = modifier.padding(8.dp)) {
            Text(fontSize = 18.sp, text = details.name.orEmpty())
            Text(text = details.lastMessage?.toString().orEmpty(), maxLines = 2)
        }
    }
}