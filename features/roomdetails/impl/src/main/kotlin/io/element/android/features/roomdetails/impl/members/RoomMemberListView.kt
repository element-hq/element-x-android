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

package io.element.android.features.roomdetails.impl.members

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.architecture.isLoading
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomMemberListView(
    state: RoomMemberListState,
    onBackPressed: () -> Unit,
    onMemberSelected: (UserId) -> Unit,
    modifier: Modifier = Modifier,
) {

    fun onUserSelected(roomMember: RoomMember) {
        onMemberSelected(roomMember.userId)
    }

    Scaffold(
        topBar = {
            if (!state.isSearchActive) {
                RoomMemberListTopBar(onBackPressed = onBackPressed)
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column {
                RoomMemberSearchBar(
                    query = state.searchQuery,
                    state = state.searchResults,
                    active = state.isSearchActive,
                    placeHolderTitle = stringResource(StringR.string.common_search_for_someone),
                    onActiveChanged = { state.eventSink(RoomMemberListEvents.OnSearchActiveChanged(it)) },
                    onTextChanged = { state.eventSink(RoomMemberListEvents.UpdateSearchQuery(it)) },
                    onUserSelected = ::onUserSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!state.isSearchActive) {
                if (state.roomMembers is Async.Success) {
                    LazyColumn(modifier = Modifier.fillMaxWidth(), state = rememberLazyListState()) {
                        if (state.roomMembers.state.invited.isNotEmpty()) {
                            item {
                                Text(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    text = stringResource(id = R.string.screen_room_member_list_pending_header_title),
                                    style = ElementTextStyles.Regular.callout,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Start,
                                )
                            }
                            items(state.roomMembers.state.invited) { matrixUser ->
                                RoomMemberSearchResultItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    roomMember = matrixUser,
                                    onClick = { onUserSelected(matrixUser) }
                                )
                            }
                        }
                        item {
                            val memberCount = state.roomMembers.state.joined.count()
                            Text(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                text = pluralStringResource(id = R.plurals.screen_room_member_list_header_title, count = memberCount, memberCount),
                                style = ElementTextStyles.Regular.callout,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Start,
                            )
                        }
                        items(state.roomMembers.state.joined) { matrixUser ->
                            RoomMemberSearchResultItem(
                                modifier = Modifier.fillMaxWidth(),
                                roomMember = matrixUser,
                                onClick = { onUserSelected(matrixUser) }
                            )
                        }
                    }
                } else if (state.roomMembers.isLoading()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomMemberListTopBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.screen_room_details_people_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomMemberSearchBar(
    query: String,
    state: RoomMemberSearchResultState,
    active: Boolean,
    placeHolderTitle: String,
    onActiveChanged: (Boolean) -> Unit,
    onTextChanged: (String) -> Unit,
    onUserSelected: (RoomMember) -> Unit,
    modifier: Modifier = Modifier,
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
                        Icon(Icons.Default.Close, stringResource(StringR.string.action_clear))
                    }
                }
            }

            !active -> {
                {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(StringR.string.action_search),
                        modifier = Modifier.alpha(0.4f), // FIXME align on Design system theme (removing alpha should be fine)
                    )
                }
            }

            else -> null
        },
        colors = if (!active) SearchBarDefaults.colors() else SearchBarDefaults.colors(containerColor = Color.Transparent),
        content = {
            if (state is RoomMemberSearchResultState.Results) {
                LazyColumn {
                    items(state.results) { matrixUser ->
                        RoomMemberSearchResultItem(
                            modifier = Modifier.fillMaxWidth(),
                            roomMember = matrixUser,
                            onClick = { onUserSelected(matrixUser) }
                        )
                    }
                }
            } else if (state is RoomMemberSearchResultState.NoResults) {
                Spacer(Modifier.size(80.dp))

                Text(
                    text = stringResource(StringR.string.common_no_results),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
    )
}

@Composable
private fun RoomMemberSearchResultItem(
    roomMember: RoomMember,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    MatrixUserRow(
        modifier = modifier.clickable(onClick = onClick),
        matrixUser = MatrixUser(
            userId = roomMember.userId,
            displayName = roomMember.displayName,
            avatarUrl = roomMember.avatarUrl
        ),
        avatarSize = AvatarSize.Custom(36.dp),
    )
}

@Preview
@Composable
fun RoomMemberListLightPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun RoomMemberListDarkPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: RoomMemberListState) {
    RoomMemberListView(
        state = state,
        onBackPressed = {},
        onMemberSelected = {}
    )
}
