@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.roomlist.components

import Avatar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.features.roomlist.model.MatrixUser

@Composable
fun RoomListTopBar(
    matrixUser: MatrixUser?,
    onLogoutClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LogCompositions(tag = "RoomListScreen", msg = "TopBar")
    val openDialog = remember { mutableStateOf(false) }
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
                onClick = { openDialog.value = true }
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "logout")
            }
        },
        scrollBehavior = scrollBehavior,
    )
    if (openDialog.value) {
        AskLogoutConfirmDialog(onLogoutClicked) {
            openDialog.value = false
        }
    }
}

@Composable
fun AskLogoutConfirmDialog(onLogoutClicked: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Log out")
        },
        text = {
            Text("Do you confirm you want to log out?")
        },
        confirmButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        onDismiss()
                        onLogoutClicked()
                    })
                {
                    Text("Logout")
                }
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.padding(all = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismiss
                )
                {
                    Text("Cancel")
                }
            }
        }
    )
}
