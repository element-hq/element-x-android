/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.features.roomlist.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.MediumTopAppBar
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.matrix.core.UserId
import io.element.android.libraries.matrix.ui.model.MatrixUser
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun RoomListTopBar(
    matrixUser: MatrixUser?,
    filter: String,
    onFilterChanged: (String) -> Unit,
    onOpenSettings: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
) {
    LogCompositions(
        tag = "RoomListScreen",
        msg = "TopBar"
    )
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
            modifier = modifier,
        )
    } else {
        DefaultRoomListTopBar(
            matrixUser = matrixUser,
            onOpenSettings = onOpenSettings,
            onSearchClicked = {
                searchWidgetStateIsOpened = true
            },
            scrollBehavior = scrollBehavior,
            modifier = modifier,
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

@Preview
@Composable
fun SearchRoomListTopBarLightPreview() = ElementPreviewLight { SearchRoomListTopBarPreview() }

@Preview
@Composable
fun SearchRoomListTopBarDarkPreview() = ElementPreviewDark { SearchRoomListTopBarPreview() }

@Composable
private fun SearchRoomListTopBarPreview() {
    SearchRoomListTopBar(
        text = "Hello",
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()),
    )
}

@Composable
private fun DefaultRoomListTopBar(
    matrixUser: MatrixUser?,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
    onSearchClicked: () -> Unit = {},
) {
    MediumTopAppBar(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        title = {
            Text(
                fontWeight = FontWeight.Bold,
                text = stringResource(id = StringR.string.all_chats)
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
                onClick = onOpenSettings
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@Preview
@Composable
fun DefaultRoomListTopBarLightPreview() = ElementPreviewLight { DefaultRoomListTopBarPreview() }

@Preview
@Composable
fun DefaultRoomListTopBarDarkPreview() = ElementPreviewDark { DefaultRoomListTopBarPreview() }

@Composable
private fun DefaultRoomListTopBarPreview() {
    DefaultRoomListTopBar(
        matrixUser = MatrixUser(UserId("id"), "Alice", AvatarData("Alice")),
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState()),
    )
}
