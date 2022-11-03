@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.roomlist

import Avatar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.core.data.LogCompositions
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.features.roomlist.model.RoomListViewState
import io.element.android.x.matrix.core.RoomId

@Composable
fun RoomListScreen(
    onSuccessLogout: () -> Unit = { },
    onRoomClicked: (RoomId) -> Unit = { }
) {
    val viewModel: RoomListViewModel = mavericksViewModel()
    val logoutAction by viewModel.collectAsState(RoomListViewState::logoutAction)
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
        onLogoutClicked = viewModel::logout
    )
}

@Composable
fun RoomListContent(
    roomSummaries: List<RoomListRoomSummary>,
    matrixUser: MatrixUser?,
    onRoomClicked: (RoomId) -> Unit,
    onLogoutClicked: () -> Unit,
) {
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    LogCompositions(tag = "RoomListScreen", msg = "Content")
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            RoomListTopBar(matrixUser, onLogoutClicked, scrollBehavior)
        },
        content = { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(roomSummaries) { room ->
                    RoomItem(room = room) {
                        onRoomClicked(it)
                    }
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListTopBar(
    matrixUser: MatrixUser?,
    onLogoutClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LogCompositions(tag = "RoomListScreen", msg = "TopBar")
    if (matrixUser == null) return
    MediumTopAppBar(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        title = {
            Text(
                fontWeight = FontWeight.Bold,
                text = "All Chats"
            )
        },
        navigationIcon = {
            IconButton(onClick = {}) {
                Avatar(matrixUser.avatarData)
            }
        },
        actions = {
            IconButton(
                onClick = onLogoutClicked
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "logout")
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun RoomItem(
    modifier: Modifier = Modifier,
    room: RoomListRoomSummary,
    onClick: (RoomId) -> Unit
) {
    if (room.isPlaceholder) {
        return
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(
                onClick = { onClick(room.roomId) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(room.avatarData)
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .weight(1f)
            ) {
                Text(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = room.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = room.lastMessage?.toString().orEmpty(),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
            ) {
                Text(
                    fontSize = 12.sp,
                    text = room.timestamp ?: "",
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier.size(4.dp))
                val unreadIndicatorColor =
                    if (room.hasUnread) MaterialTheme.colorScheme.primary else Color.Transparent
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(unreadIndicatorColor)
                        .align(Alignment.End),
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewableRoomListContent() {
    val roomSummaries = listOf(
        RoomListRoomSummary(
            name = "Room",
            hasUnread = true,
            timestamp = "14:18",
            lastMessage = "A message",
            avatarData = AvatarData("R"),
            id = "roomId"
        )
    )
    RoomListContent(
        roomSummaries = roomSummaries,
        matrixUser = MatrixUser("User#1", avatarData = AvatarData("U")),
        onRoomClicked = {},
        onLogoutClicked = {}
    )
}