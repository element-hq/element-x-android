@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.roomlist.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.element.android.x.core.compose.LogCompositions
import io.element.android.x.core.compose.textFieldState
import io.element.android.x.designsystem.components.avatar.Avatar
import io.element.android.x.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.x.features.roomlist.model.MatrixUser

@Composable
fun RoomListTopBar(
    matrixUser: MatrixUser?,
    filter: String,
    onFilterChanged: (String) -> Unit,
    onLogoutClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    LogCompositions(tag = "RoomListScreen", msg = "TopBar")
    var searchWidgetStateIsOpened by rememberSaveable { mutableStateOf(false) }

    fun closeFilter() {
        onFilterChanged("")
        searchWidgetStateIsOpened = false
    }

    BackHandler(enabled = searchWidgetStateIsOpened) {
        closeFilter()
    }

    if (searchWidgetStateIsOpened) {
        SearchRoomListTopBar(
            text = filter,
            onFilterChanged = onFilterChanged,
            onCloseClicked = ::closeFilter,
            scrollBehavior = scrollBehavior,
        )
    } else {
        DefaultRoomListTopBar(
            matrixUser = matrixUser,
            onLogoutClicked = onLogoutClicked,
            onSearchClicked = {
                searchWidgetStateIsOpened = true
            },
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
fun SearchRoomListTopBar(
    text: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    onFilterChanged: (String) -> Unit = {},
    onCloseClicked: () -> Unit = {},
) {
    var filterState by textFieldState(stateValue = text)
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        title = {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = filterState,
                textStyle = TextStyle(
                    fontSize = 17.sp
                ),
                onValueChange = {
                    filterState = it
                    onFilterChanged(it)
                },
                placeholder = {
                    Text(
                        text = "Search",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = ContentAlpha.medium)
                    )
                },
                singleLine = true,
                trailingIcon = {
                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onFilterChanged("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "clear",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = ContentAlpha.medium),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    onCloseClicked()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "close",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun DefaultRoomListTopBar(
    matrixUser: MatrixUser?,
    onLogoutClicked: () -> Unit,
    onSearchClicked: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
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
                onClick = onSearchClicked
            ) {
                Icon(Icons.Default.Search, contentDescription = "search")
            }
            IconButton(
                onClick = { openDialog.value = true }
            ) {
                Icon(Icons.Default.Logout, contentDescription = "logout")
            }
        },
        scrollBehavior = scrollBehavior,
    )
    // Log out confirmation dialog
    ConfirmationDialog(
        isDisplayed = openDialog.value,
        title = "Log out",
        content = "Do you confirm you want to log out?",
        submitText = "Log out",
        onSubmitClicked = onLogoutClicked,
        onDismiss = {
            openDialog.value = false
        }
    )
}
