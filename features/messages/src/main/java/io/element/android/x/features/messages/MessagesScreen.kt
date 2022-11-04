@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.messages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.data.LogCompositions
import io.element.android.x.features.messages.model.MessagesViewState

@Composable
fun MessagesScreen(roomId: String) {
    val viewModel: MessagesViewModel = mavericksViewModel(argsFactory = { roomId })
    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val roomTitle by viewModel.collectAsState(prop1 = MessagesViewState::roomTitle)
    MessagesContent(roomTitle)
}

@Composable
fun MessagesContent(roomTitle: String) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    LogCompositions(tag = "RoomListScreen", msg = "Content")
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = roomTitle) }
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding))
        }
    )
}



