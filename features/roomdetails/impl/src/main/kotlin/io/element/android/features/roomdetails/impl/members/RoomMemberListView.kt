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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import kotlinx.collections.immutable.ImmutableList
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomMemberListView(
    state: RoomMemberListState,
    onBackPressed: () -> Unit,
    onInvitePressed: () -> Unit,
    onMemberSelected: (UserId) -> Unit,
    modifier: Modifier = Modifier,
) {

    fun onUserSelected(roomMember: RoomMember) {
        onMemberSelected(roomMember.userId)
    }

    Scaffold(
        topBar = {
            if (!state.isSearchActive) {
                RoomMemberListTopBar(
                    canInvite = state.canInvite,
                    onBackPressed = onBackPressed,
                    onInvitePressed = onInvitePressed,
                )
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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

            if (!state.isSearchActive) {
                if (state.roomMembers is Async.Success) {
                    RoomMemberList(
                        roomMembers = state.roomMembers.data,
                        showMembersCount = true,
                        onUserSelected = ::onUserSelected
                    )
                } else if (state.roomMembers.isLoading()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomMemberList(
    roomMembers: RoomMembers,
    showMembersCount: Boolean,
    onUserSelected: (RoomMember) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth(), state = rememberLazyListState()) {
        if (roomMembers.invited.isNotEmpty()) {
            roomMemberListSection(
                headerText = { stringResource(id = R.string.screen_room_member_list_pending_header_title) },
                members = roomMembers.invited,
                onMemberSelected = { onUserSelected(it) }
            )
        }
        if (roomMembers.joined.isNotEmpty()) {
            roomMemberListSection(
                headerText = {
                    if (showMembersCount) {
                        val memberCount = roomMembers.joined.count()
                        pluralStringResource(id = R.plurals.screen_room_member_list_header_title, count = memberCount, memberCount)
                    } else {
                        stringResource(id = R.string.screen_room_member_list_room_members_header_title)
                    }
                },
                members = roomMembers.joined,
                onMemberSelected = { onUserSelected(it) }
            )
        }
    }
}

private fun LazyListScope.roomMemberListSection(
    headerText: @Composable () -> String,
    members: ImmutableList<RoomMember>,
    onMemberSelected: (RoomMember) -> Unit,
) {
    item {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = headerText(),
            fontSize = 16.sp,
            style = ElementTextStyles.Regular.callout,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Start,
        )
    }
    items(members) { matrixUser ->
        RoomMemberListItem(
            modifier = Modifier.fillMaxWidth(),
            roomMember = matrixUser,
            onClick = { onMemberSelected(matrixUser) }
        )
    }
}

@Composable
private fun RoomMemberListItem(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomMemberListTopBar(
    canInvite: Boolean,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onInvitePressed: () -> Unit = {},
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
        actions = {
            if (canInvite) {
                TextButton(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = onInvitePressed,
                ) {
                    Text(
                        text = stringResource(StringR.string.action_invite),
                        fontSize = 16.sp,
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomMemberSearchBar(
    query: String,
    state: SearchBarResultState<RoomMembers>,
    active: Boolean,
    placeHolderTitle: String,
    onActiveChanged: (Boolean) -> Unit,
    onTextChanged: (String) -> Unit,
    onUserSelected: (RoomMember) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        query = query,
        onQueryChange = onTextChanged,
        active = active,
        onActiveChange = onActiveChanged,
        modifier = modifier,
        placeHolderTitle = placeHolderTitle,
        resultState = state,
        resultHandler = { results ->
            RoomMemberList(
                roomMembers = results,
                showMembersCount = false,
                onUserSelected = { onUserSelected(it) }
            )
        },
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
        onMemberSelected = {},
        onInvitePressed = {},
    )
}
