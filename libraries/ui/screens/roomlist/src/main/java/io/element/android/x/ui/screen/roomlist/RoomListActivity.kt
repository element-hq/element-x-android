package io.element.android.x.ui.screen.roomlist

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.ui.theme.ElementXTheme
import io.element.android.x.ui.theme.components.Avatar
import org.matrix.rustcomponents.sdk.Room

class RoomListActivity : ComponentActivity() {

    private var initDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ElementXTheme {
                val viewModel: RoomListViewModel = mavericksViewModel()
                if (!initDone) {
                    initDone = true
                    viewModel.handle(RoomListActions.Init)
                }
                val state = viewModel.collectAsState()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        OptionMenu(state.value.user, viewModel)
                        val rooms = state.value.rooms
                        if (rooms is Success) {
                            LazyColumn {
                                items(rooms()) { room ->
                                    RoomCompose(room) {
                                        Toast.makeText(
                                            this@RoomListActivity,
                                            "Room $it clicked!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }
                }
                if (state.value.logoutAction is Success) {
                    finish()
                }
            }
        }
    }

    @Composable
    private fun RoomCompose(room: Room, onClick: (String) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick.invoke(room.id()) },
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = room.avatarUrl()),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
            ) {
                Text(text = "Room: ${room.name() ?: room.id()}")
                Text(text = if (room.isDirect()) "Direct" else "Room")
            }

        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun OptionMenu(matrixUser: MatrixUser, viewModel: RoomListViewModel) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Avatar(data = matrixUser.avatarData)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${matrixUser.username}")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.handle(RoomListActions.Logout) }
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "logout")
                }
            }
        )
    }
}
