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

package io.element.android.features.roomdetails.impl.members.search.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.element.android.features.roomdetails.impl.members.search.UserSearchResultState
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.ui.strings.R
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMemberBar(
    query: String,
    state: UserSearchResultState,
    selectedUsers: ImmutableList<MatrixUser>,
    active: Boolean,
    isMultiSelectionEnabled: Boolean,
    modifier: Modifier = Modifier,
    placeHolderTitle: String = stringResource(R.string.common_search_for_someone),
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
                        Icon(Icons.Default.Close, stringResource(R.string.action_clear))
                    }
                }
            }

            !active -> {
                {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.action_search),
                        modifier = Modifier.alpha(0.4f), // FIXME align on Design system theme (removing alpha should be fine)
                    )
                }
            }

            else -> null
        },
        colors = if (!active) SearchBarDefaults.colors() else SearchBarDefaults.colors(containerColor = Color.Transparent),
        content = {
            if (isMultiSelectionEnabled && active && selectedUsers.isNotEmpty()) {
                SelectedMembersList(
                    contentPadding = PaddingValues(16.dp),
                    selectedUsers = selectedUsers,
                    autoScroll = true,
                    onUserRemoved = onUserDeselected,
                )
            }


            if (state is UserSearchResultState.Results) {
                LazyColumn {
                    if (isMultiSelectionEnabled) {
                        items(state.results) { matrixUser ->
                            SearchMultipleMembersResultItem(
                                modifier = Modifier.fillMaxWidth(),
                                matrixUser = matrixUser,
                                isUserSelected = selectedUsers.find { it.userId == matrixUser.userId } != null,
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
                        items(state.results) { matrixUser ->
                            SearchSingleMemberResultItem(
                                modifier = Modifier.fillMaxWidth(),
                                matrixUser = matrixUser,
                                onClick = { onUserSelected(matrixUser) }
                            )
                        }
                    }
                }
            } else if (state is UserSearchResultState.NoResults) {
                Spacer(Modifier.size(80.dp))

                Text(
                    text = stringResource(R.string.common_no_results),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
    )
}
