package io.element.android.x.features.roomlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import org.matrix.rustcomponents.sdk.Room

@Composable
fun RoomListScreen(
    viewModel: RoomListViewModel = mavericksViewModel(),
    onRoomClicked: (String) -> Unit = { }
) {
    val state by viewModel.collectAsState()
    RoomListContent(state, onRoomClicked)
}

@Composable
fun RoomListContent(
    state: RoomListViewState,
    onRoomClicked: (String) -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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

@Composable
private fun RoomItem(
    modifier: Modifier = Modifier,
    room: Room,
    onClick: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                onClick(room.id())
            }
            .fillMaxWidth()
    ) {
        Column(modifier = modifier.padding(8.dp)) {
            Text(text = "Room: ${room.name() ?: room.id()}")
            Text(text = if (room.isDirect()) "Direct" else "Room")
        }
    }
}