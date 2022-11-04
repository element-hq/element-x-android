@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.messages

import Avatar
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
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.messages.model.MessagesViewState

@Composable
fun MessagesScreen(roomId: String) {
    val viewModel: MessagesViewModel = mavericksViewModel(argsFactory = { roomId })
    LogCompositions(tag = "MessagesScreen", msg = "Root")
    val roomTitle by viewModel.collectAsState(MessagesViewState::roomName)
    val roomAvatar by viewModel.collectAsState(MessagesViewState::roomAvatar)
    MessagesContent(roomTitle, roomAvatar)
}

@Composable
fun MessagesContent(
    roomTitle: String?,
    roomAvatar: AvatarData?
) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    LogCompositions(tag = "RoomListScreen", msg = "Content")
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (roomAvatar != null) {
                        IconButton(onClick = {}) {
                            Avatar(roomAvatar)
                        }
                    }
                },
                title = { Text(text = roomTitle ?: "") }
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding))
        }
    )
}



