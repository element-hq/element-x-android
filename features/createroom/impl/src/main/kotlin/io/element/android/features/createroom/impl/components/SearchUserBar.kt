/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import io.element.android.libraries.matrix.ui.components.SelectedUsersRowList
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.libraries.usersearch.api.UserSearchResult
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUserBar(
    isDebugBuild: Boolean,
    query: String,
    state: SearchBarResultState<ImmutableList<UserSearchResult>>,
    showLoader: Boolean,
    selectedUsers: ImmutableList<MatrixUser>,
    active: Boolean,
    isMultiSelectionEnable: Boolean,
    onActiveChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onUserSelect: (MatrixUser) -> Unit,
    onUserDeselect: (MatrixUser) -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    placeHolderTitle: String = stringResource(CommonStrings.common_search_for_someone),
) {
    val columnState = rememberLazyListState()

    SearchBar(
        query = query,
        onQueryChange = onTextChange,
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier,
        placeHolderTitle = placeHolderTitle,
        showBackButton = showBackButton,
        contentPrefix = {
            if (isMultiSelectionEnable && active && selectedUsers.isNotEmpty()) {
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

                SelectedUsersRowList(
                    contentPadding = PaddingValues(16.dp),
                    selectedUsers = selectedUsers,
                    autoScroll = true,
                    onUserRemove = onUserDeselect,
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
                if (isMultiSelectionEnable) {
                    itemsIndexed(users) { index, searchResult ->
                        SearchMultipleUsersResultItem(
                            isDebugBuild = isDebugBuild,
                            modifier = Modifier.fillMaxWidth(),
                            searchResult = searchResult,
                            isUserSelected = selectedUsers.contains(searchResult.matrixUser),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    onUserSelect(searchResult.matrixUser)
                                } else {
                                    onUserDeselect(searchResult.matrixUser)
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
                            isDebugBuild = isDebugBuild,
                            modifier = Modifier.fillMaxWidth(),
                            searchResult = searchResult,
                            onClick = { onUserSelect(searchResult.matrixUser) }
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
