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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.async.AsyncLoading
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.SelectedUsersList
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserBar(
    query: String,
    state: SearchBarResultState<ImmutableList<UserSearchResult>>,
    showLoader: Boolean,
    selectedUsers: ImmutableList<MatrixUser>,
    active: Boolean,
    isMultiSelectionEnabled: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    onTextChanged: (String) -> Unit,
    onUserSelected: (MatrixUser) -> Unit,
    onUserDeselected: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    placeHolderTitle: String = stringResource(CommonStrings.common_search_for_someone),
) {
    val columnState = rememberLazyListState()

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
                // We want the selected users to behave a bit like a top bar - when the list below is scrolled, the colour
                // should change to indicate elevation.

                val elevation = remember {
                    derivedStateOf {
                        if (columnState.canScrollBackward) {
                            4.dp
                        } else {
                            0.dp
                        }
                    }
                }

                val appBarContainerColor by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.surfaceColorAtElevation(elevation.value),
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                )

                SelectedUsersList(
                    contentPadding = PaddingValues(16.dp),
                    selectedUsers = selectedUsers,
                    autoScroll = true,
                    onUserRemoved = onUserDeselected,
                    modifier = Modifier.background(appBarContainerColor)
                )
            }
        },
        contentSuffix = {
            if (showLoader) {
                AsyncLoading()
            }
        },
        resultState = state,
        resultHandler = { users ->
            LazyColumn(state = columnState) {
                if (isMultiSelectionEnabled) {
                    itemsIndexed(users) { index, searchResult ->
                        SearchMultipleUsersResultItem(
                            modifier = Modifier.fillMaxWidth(),
                            searchResult = searchResult,
                            isUserSelected = selectedUsers.find { it.userId == searchResult.matrixUser.userId } != null,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    onUserSelected(searchResult.matrixUser)
                                } else {
                                    onUserDeselected(searchResult.matrixUser)
                                }
                            }
                        )
                        if (index < users.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                } else {
                    itemsIndexed(users) { index, searchResult ->
                        SearchSingleUserResultItem(
                            modifier = Modifier.fillMaxWidth(),
                            searchResult = searchResult,
                            onClick = { onUserSelected(searchResult.matrixUser) }
                        )
                        if (index < users.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        },
    )
}
