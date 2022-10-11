package io.element.android.x.ui.screen.roomlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.ui.theme.ElementXTheme

class RoomListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ElementXTheme {
                val viewModel: RoomListViewModel = mavericksViewModel()
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
                        OptionMenu(viewModel)
                        /* TODO
                        val state = viewModel.state.collectAsState().value
                        RoomListHeader()
                        RoomList()
                         */
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
    private fun OptionMenu(viewModel: RoomListViewModel) {
        TopAppBar(
            title = { Text("Room List") },
            actions = {
                Button(
                    onClick = { viewModel.handle(RoomListActions.Logout) }
                ) {
                    Text(text = "logout")
                }
            }
        )
    }
}
