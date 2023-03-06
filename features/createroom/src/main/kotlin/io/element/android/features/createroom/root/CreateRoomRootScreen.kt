/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.createroom.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.BackButton
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.DockedSearchBar
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.R as DrawableR
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomRootScreen(
    state: CreateRoomRootState,
    modifier: Modifier = Modifier,
    onClosePressed: () -> Unit = {}
) {
    val isSearchActive = rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxWidth(),
        topBar = {
            if (!isSearchActive.value) {
                CreateRoomRootViewTopBar(onClosePressed = onClosePressed)
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            CreateRoomSearchBar(
                modifier = Modifier.fillMaxWidth(),
                placeHolderTitle = stringResource(StringR.string.search_for_someone),
                active = isSearchActive,
            )

            TextButton(
                modifier = Modifier.padding(start = 8.dp, top = 16.dp, end = 8.dp),
                onClick = { }) {
                Icon(
                    modifier = Modifier
                        .padding(end = 16.dp),
                    resourceId = DrawableR.drawable.ic_group, // TODO ask design for squared icon
                    contentDescription = ""
                )
                Text(text = stringResource(id = StringR.string.new_room))
            }

            TextButton(
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = { }
            ) {
                Icon(
                    modifier = Modifier
                        .padding(end = 16.dp),
                    resourceId = DrawableR.drawable.ic_share,
                    contentDescription = ""
                )
                Text(text = stringResource(id = StringR.string.invite_people_menu))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomRootViewTopBar(
    modifier: Modifier = Modifier,
    onClosePressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(id = StringR.string.start_chat),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        actions = {
            IconButton(onClick = onClosePressed) {
                Icon(resourceId = DrawableR.drawable.ic_close, contentDescription = stringResource(id = StringR.string.action_close))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomSearchBar(
    placeHolderTitle: String,
    active: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    fun closeSearchBar() {
        focusManager.clearFocus()
        active.value = false
    }

    DockedSearchBar(
        query = text,
        onQueryChange = { text = it },
        onSearch = { closeSearchBar() },
        active = active.value,
        onActiveChange = {
            active.value = it
            if (!active.value) focusManager.clearFocus()
        },
        modifier = modifier
            .padding(horizontal = if (!active.value) 16.dp else 0.dp),
        placeholder = {
            Text(
                text = placeHolderTitle,
                modifier = Modifier.alpha(0.4f), // FIXME align on Design system theme (removing alpha should be fine)
            )
        },
        leadingIcon = if (active.value) {
            {
                BackButton(onClick = { closeSearchBar() })
            }
        } else null,
        trailingIcon = {
            if (active.value) {
                IconButton(onClick = { text = "" }) {
                    Icon(DrawableR.drawable.ic_close, stringResource(StringR.string.a11y_clear))
                }
            } else {
                Icon(
                    resourceId = DrawableR.drawable.ic_search,
                    contentDescription = stringResource(StringR.string.search),
                    modifier = Modifier.alpha(0.4f), // FIXME align on Design system theme (removing alpha should be fine)
                )
            }
        },
        shape = if (!active.value) SearchBarDefaults.dockedShape else SearchBarDefaults.fullScreenShape,
        colors = if (!active.value) SearchBarDefaults.colors() else SearchBarDefaults.colors(containerColor = Color.Transparent),
        content = {},
    )
}

@Preview
@Composable
fun CreateRoomRootViewLightPreview(@PreviewParameter(CreateRoomRootStateProvider::class) state: CreateRoomRootState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun CreateRoomRootViewDarkPreview(@PreviewParameter(CreateRoomRootStateProvider::class) state: CreateRoomRootState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: CreateRoomRootState) {
    CreateRoomRootScreen(
        state = state,
    )
}
