package io.element.android.x.ui.screen.roomlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.ui.theme.ElementXTheme

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
                            rooms().forEach {
                                Text(text = "Room: ${it.name() ?: it.id()}")
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun OptionMenu(matrixUser: MatrixUser, viewModel: RoomListViewModel) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(matrixUser.avatarUrl),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
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
