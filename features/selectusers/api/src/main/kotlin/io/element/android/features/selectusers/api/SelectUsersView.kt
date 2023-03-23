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

package io.element.android.features.selectusers.api

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.ui.components.CheckableMatrixUserRow
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.matrix.ui.model.MatrixUser
import io.element.android.libraries.matrix.ui.model.getBestName
import kotlinx.collections.immutable.ImmutableList
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun SelectUsersView(
    state: SelectUsersState,
    modifier: Modifier = Modifier,
    onUserSelected: (MatrixUser) -> Unit = {},
    onUserDeselected: (MatrixUser) -> Unit = {},
) {
    Column(
        modifier = modifier,
    ) {
        SearchUserBar(
            modifier = Modifier.fillMaxWidth(),
            query = state.searchQuery,
            results = state.searchResults,
            selectedUsers = state.selectedUsers,
            selectedUsersListState = state.selectedUsersListState,
            active = state.isSearchActive,
            isMultiSelectionEnabled = state.isMultiSelectionEnabled,
            onActiveChanged = { state.eventSink(SelectUsersEvents.OnSearchActiveChanged(it)) },
            onTextChanged = { state.eventSink(SelectUsersEvents.UpdateSearchQuery(it)) },
            onUserSelected = {
                state.eventSink(SelectUsersEvents.AddToSelection(it))
                onUserSelected(it)
            },
            onUserDeselected = {
                state.eventSink(SelectUsersEvents.RemoveFromSelection(it))
                onUserDeselected(it)
            },
        )

        if (state.isMultiSelectionEnabled && !state.isSearchActive && state.selectedUsers.isNotEmpty()) {
            SelectedUsersList(
                listState = state.selectedUsersListState,
                modifier = Modifier.padding(16.dp),
                selectedUsers = state.selectedUsers,
                onUserRemoved = {
                    state.eventSink(SelectUsersEvents.RemoveFromSelection(it))
                    onUserDeselected(it)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserBar(
    query: String,
    results: ImmutableList<MatrixUser>,
    selectedUsers: ImmutableList<MatrixUser>,
    selectedUsersListState: LazyListState,
    active: Boolean,
    isMultiSelectionEnabled: Boolean,
    modifier: Modifier = Modifier,
    placeHolderTitle: String = stringResource(StringR.string.search_for_someone),
    onActiveChanged: (Boolean) -> Unit = {},
    onTextChanged: (String) -> Unit = {},
    onUserSelected: (MatrixUser) -> Unit = {},
    onUserDeselected: (MatrixUser) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    if (!active) {
        onTextChanged("")
        focusManager.clearFocus()
    }

    SearchBar(
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
        } else {
            null
        },
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
        colors = if (!active) SearchBarDefaults.colors() else SearchBarDefaults.colors(containerColor = Color.Transparent),
        content = {
            if (isMultiSelectionEnabled && active && selectedUsers.isNotEmpty()) {
                SelectedUsersList(
                    listState = selectedUsersListState,
                    modifier = Modifier.padding(16.dp),
                    selectedUsers = selectedUsers,
                    onUserRemoved = onUserDeselected,
                )
            }

            LazyColumn {
                if (isMultiSelectionEnabled) {
                    items(results) { matrixUser ->
                        SearchMultipleUsersResultItem(
                            modifier = Modifier.fillMaxWidth(),
                            matrixUser = matrixUser,
                            isUserSelected = selectedUsers.find { it.id == matrixUser.id } != null,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    onUserSelected(matrixUser)
                                } else {
                                    onUserDeselected(matrixUser)
                                }
                            }
                        )
                    }
                } else {
                    items(results) { matrixUser ->
                        SearchSingleUserResultItem(
                            modifier = Modifier.fillMaxWidth(),
                            matrixUser = matrixUser,
                            onClick = { onUserSelected(matrixUser) }
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun SearchMultipleUsersResultItem(
    matrixUser: MatrixUser,
    isUserSelected: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    CheckableMatrixUserRow(
        checked = isUserSelected,
        modifier = modifier,
        matrixUser = matrixUser,
        avatarSize = AvatarSize.Custom(36.dp),
        onCheckedChange = onCheckedChange,
    )
}

@Composable
fun SearchSingleUserResultItem(
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    MatrixUserRow(
        modifier = modifier.clickable(onClick = onClick),
        matrixUser = matrixUser,
        avatarSize = AvatarSize.Custom(36.dp),
    )
}

@Composable
fun SelectedUsersList(
    listState: LazyListState,
    selectedUsers: ImmutableList<MatrixUser>,
    modifier: Modifier = Modifier,
    onUserRemoved: (MatrixUser) -> Unit = {},
) {
    LazyRow(
        state = listState,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(selectedUsers.toList()) { matrixUser ->
            SelectedUser(
                matrixUser = matrixUser,
                onUserRemoved = onUserRemoved,
            )
        }
    }
}

@Composable
fun SelectedUser(
    matrixUser: MatrixUser,
    modifier: Modifier = Modifier,
    onUserRemoved: (MatrixUser) -> Unit,
) {
    Box(modifier = modifier.width(56.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(matrixUser.avatarData.copy(size = AvatarSize.Custom(56.dp)))
            Text(
                text = matrixUser.getBestName(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .size(20.dp)
                .align(Alignment.TopEnd),
            onClick = { onUserRemoved(matrixUser) }
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(id = StringR.string.action_remove),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Preview
@Composable
internal fun SelectUsersViewLightPreview(@PreviewParameter(SelectUsersStateProvider::class) state: SelectUsersState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun SelectUsersViewDarkPreview(@PreviewParameter(SelectUsersStateProvider::class) state: SelectUsersState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: SelectUsersState) {
    SelectUsersView(state = state)
}
