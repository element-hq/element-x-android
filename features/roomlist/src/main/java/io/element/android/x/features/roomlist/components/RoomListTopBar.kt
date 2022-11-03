@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.roomlist.components

import Avatar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import io.element.android.x.core.data.LogCompositions
import io.element.android.x.features.roomlist.model.MatrixUser

@Composable
fun RoomListTopBar(
    matrixUser: MatrixUser?,
    onLogoutClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LogCompositions(tag = "RoomListScreen", msg = "TopBar")
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
            if (matrixUser != null) {
                IconButton(onClick = {}) {
                    Avatar(matrixUser.avatarData)
                }
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