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

package io.element.android.features.createroom.impl.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.SelectedUsersList
import io.element.android.libraries.ui.strings.R
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SearchUserBar(
    query: String,
    state: SearchBarResultState<ImmutableList<MatrixUser>>,
    selectedUsers: ImmutableList<MatrixUser>,
    active: Boolean,
    isMultiSelectionEnabled: Boolean,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    placeHolderTitle: String = stringResource(R.string.common_search_for_someone),
    onActiveChanged: (Boolean) -> Unit = {},
    onTextChanged: (String) -> Unit = {},
    onUserSelected: (MatrixUser) -> Unit = {},
    onUserDeselected: (MatrixUser) -> Unit = {},
) {
    SearchBar(
        query = query,
        onQueryChange = onTextChanged,
        active = active,
        onActiveChange = onActiveChanged,
        modifier = modifier,
        placeHolderTitle = placeHolderTitle,
        showBackButton = showBackButton,
        contentPrefix = {
            if (isMultiSelectionEnabled && active && selectedUsers.isNotEmpty()) {
                SelectedUsersList(
                    contentPadding = PaddingValues(16.dp),
                    selectedUsers = selectedUsers,
                    autoScroll = true,
                    onUserRemoved = onUserDeselected,
                )
            }
        },
        resultState = state,
        resultHandler = { users ->
            LazyColumn {
                if (isMultiSelectionEnabled) {
                    items(users) { matrixUser ->
                        SearchMultipleUsersResultItem(
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
                    items(users) { matrixUser ->
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
