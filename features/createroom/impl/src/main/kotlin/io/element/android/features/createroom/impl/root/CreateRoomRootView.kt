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

package io.element.android.features.createroom.impl.root

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.DockedSearchBar
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.matrix.ui.model.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import io.element.android.libraries.designsystem.R as DrawableR
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomRootView(
    state: CreateRoomRootState,
    modifier: Modifier = Modifier,
    onClosePressed: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxWidth(),
        topBar = {
            if (!state.isSearchActive) {
                CreateRoomRootViewTopBar(onClosePressed = onClosePressed)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CreateRoomSearchBar(
                modifier = Modifier.fillMaxWidth(),
                query = state.searchQuery,
                placeHolderTitle = stringResource(StringR.string.search_for_someone),
                results = state.searchResults,
                active = state.isSearchActive,
                onActiveChanged = { state.eventSink(CreateRoomRootEvents.OnSearchActiveChanged(it)) },
                onTextChanged = { state.eventSink(CreateRoomRootEvents.UpdateSearchQuery(it)) },
                onResultSelected = { state.eventSink(CreateRoomRootEvents.StartDM(it)) }
            )

            if (!state.isSearchActive) {
                CreateRoomActionButtonsList(
                    onNewRoomClicked = { state.eventSink(CreateRoomRootEvents.CreateRoom) },
                    onInvitePeopleClicked = { state.eventSink(CreateRoomRootEvents.InvitePeople) },
                )
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
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = StringR.string.action_close))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomSearchBar(
    query: String,
    placeHolderTitle: String,
    results: ImmutableList<MatrixUser>,
    active: Boolean,
    modifier: Modifier = Modifier,
    onActiveChanged: (Boolean) -> Unit = {},
    onTextChanged: (String) -> Unit = {},
    onResultSelected: (MatrixUser) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    if (!active) {
        onTextChanged("")
        focusManager.clearFocus()
    }

    DockedSearchBar(
        query = query,
        onQueryChange = onTextChanged,
        onSearch = { focusManager.clearFocus() },
        active = active,
        onActiveChange = onActiveChanged,
        modifier = modifier
            .padding(horizontal = if (!active) 16.dp else 0.dp),
        placeholder = {
            Text(
                text = placeHolderTitle,
                modifier = Modifier.alpha(0.4f), // FIXME align on Design system theme (removing alpha should be fine)
            )
        },
        leadingIcon = if (active) {
            { BackButton(onClick = { onActiveChanged(false) }) }
        } else null,
        trailingIcon = when {
            active && query.isNotEmpty() -> {
                {
                    IconButton(onClick = { onTextChanged("") }) {
                        Icon(Icons.Default.Close, stringResource(StringR.string.a11y_clear))
                    }
                }
            }
            !active -> {
                {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(StringR.string.search),
                        modifier = Modifier.alpha(0.4f), // FIXME align on Design system theme (removing alpha should be fine)
                    )
                }
            }
            else -> null
        },
        shape = if (!active) SearchBarDefaults.dockedShape else SearchBarDefaults.fullScreenShape,
        colors = if (!active) SearchBarDefaults.colors() else SearchBarDefaults.colors(containerColor = Color.Transparent),
        content = {
            LazyColumn {
                items(results) {
                    CreateRoomSearchResultItem(
                        matrixUser = it,
                        onClick = { onResultSelected(it) }
                    )
                }
            }
        },
    )
}

@Composable
fun CreateRoomActionButtonsList(
    modifier: Modifier = Modifier,
    onNewRoomClicked: () -> Unit = {},
    onInvitePeopleClicked: () -> Unit = {},
) {
    Column(modifier = modifier) {
        CreateRoomActionButton(
            iconRes = DrawableR.drawable.ic_groups,
            text = stringResource(id = StringR.string.new_room),
            onClick = onNewRoomClicked,
        )
        CreateRoomActionButton(
            iconRes = DrawableR.drawable.ic_share,
            text = stringResource(id = StringR.string.invite_people_menu),
            onClick = onInvitePeopleClicked,
        )
    }
}

@Composable
fun CreateRoomSearchResultItem(
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    MatrixUserRow(
        modifier = modifier.heightIn(min = 56.dp),
        matrixUser = matrixUser,
        avatarSize = AvatarSize.Custom(36.dp),
        onClick = onClick,
    )
}

@Composable
fun CreateRoomActionButton(
    @DrawableRes iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.alpha(0.5f), // FIXME align on Design system theme (removing alpha should be fine)
            resourceId = iconRes,
            contentDescription = null,
        )
        Text(text = text)
    }
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
    CreateRoomRootView(
        state = state,
    )
}
