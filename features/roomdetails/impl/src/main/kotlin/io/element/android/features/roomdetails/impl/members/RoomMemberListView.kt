/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.roomdetails.impl.R
import io.element.android.features.roomdetails.impl.members.moderation.RoomMembersModerationView
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.designsystem.theme.components.SegmentedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.toMatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private enum class SelectedSection {
    MEMBERS,
    BANNED
}

@Composable
fun RoomMemberListView(
    state: RoomMemberListState,
    navigator: RoomMemberListNavigator,
    modifier: Modifier = Modifier,
    initialSelectedSectionIndex: Int = 0,
) {
    fun onSelectUser(roomMember: RoomMember) {
        state.eventSink(RoomMemberListEvents.RoomMemberSelected(roomMember))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (!state.isSearchActive) {
                RoomMemberListTopBar(
                    canInvite = state.canInvite,
                    onBackClick = navigator::exitRoomMemberList,
                    onInviteClick = navigator::openInviteMembers,
                )
            }
        }
    ) { padding ->
        var selectedSection by remember { mutableStateOf(SelectedSection.entries[initialSelectedSectionIndex]) }
        if (!state.moderationState.canDisplayBannedUsers && selectedSection == SelectedSection.BANNED) {
            SideEffect {
                selectedSection = SelectedSection.MEMBERS
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RoomMemberSearchBar(
                isDebugBuild = state.isDebugBuild,
                query = state.searchQuery,
                state = state.searchResults,
                active = state.isSearchActive,
                placeHolderTitle = stringResource(CommonStrings.common_search_for_someone),
                onActiveChange = { state.eventSink(RoomMemberListEvents.OnSearchActiveChanged(it)) },
                onTextChange = { state.eventSink(RoomMemberListEvents.UpdateSearchQuery(it)) },
                onSelectUser = ::onSelectUser,
                selectedSection = selectedSection,
                modifier = Modifier.fillMaxWidth(),
            )

            if (!state.isSearchActive) {
                RoomMemberList(
                    isDebugBuild = state.isDebugBuild,
                    roomMembers = state.roomMembers,
                    showMembersCount = true,
                    canDisplayBannedUsersControls = state.moderationState.canDisplayBannedUsers,
                    selectedSection = selectedSection,
                    onSelectedSectionChange = { selectedSection = it },
                    onSelectUser = ::onSelectUser,
                )
            }
        }
    }

    RoomMembersModerationView(
        state = state.moderationState,
        onDisplayMemberProfile = navigator::openRoomMemberDetails
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun RoomMemberList(
    isDebugBuild: Boolean,
    roomMembers: AsyncData<RoomMembers>,
    showMembersCount: Boolean,
    selectedSection: SelectedSection,
    onSelectedSectionChange: (SelectedSection) -> Unit,
    canDisplayBannedUsersControls: Boolean,
    onSelectUser: (RoomMember) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth(), state = rememberLazyListState()) {
        stickyHeader {
            Column {
                if (canDisplayBannedUsersControls) {
                    val segmentedButtonTitles = persistentListOf(
                        stringResource(id = R.string.screen_room_member_list_mode_members),
                        stringResource(id = R.string.screen_room_member_list_mode_banned),
                    )
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .background(ElementTheme.colors.bgCanvasDefault)
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    ) {
                        for ((index, title) in segmentedButtonTitles.withIndex()) {
                            SegmentedButton(
                                index = index,
                                count = segmentedButtonTitles.size,
                                selected = selectedSection.ordinal == index,
                                onClick = { onSelectedSectionChange(SelectedSection.entries[index]) },
                                text = title,
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = roomMembers.isLoading(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        when (roomMembers) {
            is AsyncData.Failure -> failureItem(roomMembers.error)
            is AsyncData.Loading,
            is AsyncData.Success -> memberItems(
                isDebugBuild = isDebugBuild,
                roomMembers = roomMembers.dataOrNull() ?: return@LazyColumn,
                selectedSection = selectedSection,
                onSelectUser = onSelectUser,
                showMembersCount = showMembersCount,
            )
            AsyncData.Uninitialized -> Unit
        }
    }
}

private fun LazyListScope.memberItems(
    isDebugBuild: Boolean,
    roomMembers: RoomMembers,
    selectedSection: SelectedSection,
    onSelectUser: (RoomMember) -> Unit,
    showMembersCount: Boolean,
) {
    when (selectedSection) {
        SelectedSection.MEMBERS -> {
            if (roomMembers.invited.isNotEmpty()) {
                roomMemberListSection(
                    isDebugBuild = isDebugBuild,
                    headerText = { stringResource(id = R.string.screen_room_member_list_pending_header_title) },
                    members = roomMembers.invited,
                    onMemberSelected = { onSelectUser(it) }
                )
            }
            if (roomMembers.joined.isNotEmpty()) {
                roomMemberListSection(
                    isDebugBuild = isDebugBuild,
                    headerText = {
                        if (showMembersCount) {
                            val memberCount = roomMembers.joined.count()
                            pluralStringResource(id = R.plurals.screen_room_member_list_header_title, count = memberCount, memberCount)
                        } else {
                            stringResource(id = R.string.screen_room_member_list_room_members_header_title)
                        }
                    },
                    members = roomMembers.joined,
                    onMemberSelected = { onSelectUser(it) }
                )
            }
        }
        SelectedSection.BANNED -> { // Banned users
            if (roomMembers.banned.isNotEmpty()) {
                roomMemberListSection(
                    isDebugBuild = isDebugBuild,
                    headerText = null,
                    members = roomMembers.banned,
                    onMemberSelected = { onSelectUser(it) }
                )
            } else {
                item {
                    Box(
                        Modifier
                            .fillParentMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 56.dp)
                                .align(Alignment.Center),
                            text = stringResource(id = R.string.screen_room_member_list_banned_empty),
                            color = ElementTheme.colors.textSecondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.failureItem(failure: Throwable) {
    item {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            text = stringResource(id = CommonStrings.error_unknown) + "\n\n" + failure.localizedMessage,
            color = ElementTheme.colors.textCriticalPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

private fun LazyListScope.roomMemberListSection(
    isDebugBuild: Boolean,
    headerText: @Composable (() -> String)?,
    members: ImmutableList<RoomMember>?,
    onMemberSelected: (RoomMember) -> Unit,
) {
    headerText?.let {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                text = it(),
                style = ElementTheme.typography.fontBodyLgRegular,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
    items(members.orEmpty()) { matrixUser ->
        RoomMemberListItem(
            isDebugBuild = isDebugBuild,
            modifier = Modifier.fillMaxWidth(),
            roomMember = matrixUser,
            onClick = { onMemberSelected(matrixUser) }
        )
    }
}

@Composable
private fun RoomMemberListItem(
    isDebugBuild: Boolean,
    roomMember: RoomMember,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val roleText = when (roomMember.role) {
        RoomMember.Role.ADMIN -> stringResource(R.string.screen_room_member_list_role_administrator)
        RoomMember.Role.MODERATOR -> stringResource(R.string.screen_room_member_list_role_moderator)
        RoomMember.Role.USER -> null
    }
    MatrixUserRow(
        isDebugBuild = isDebugBuild,
        modifier = modifier.clickable(onClick = onClick),
        matrixUser = roomMember.toMatrixUser(),
        avatarSize = AvatarSize.UserListItem,
        trailingContent = roleText?.let {
            @Composable {
                Text(
                    text = it,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomMemberListTopBar(
    canInvite: Boolean,
    onBackClick: () -> Unit,
    onInviteClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(CommonStrings.common_people),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            if (canInvite) {
                TextButton(
                    text = stringResource(CommonStrings.action_invite),
                    onClick = onInviteClick,
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomMemberSearchBar(
    isDebugBuild: Boolean,
    query: String,
    state: SearchBarResultState<AsyncData<RoomMembers>>,
    active: Boolean,
    placeHolderTitle: String,
    onActiveChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onSelectUser: (RoomMember) -> Unit,
    selectedSection: SelectedSection,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        query = query,
        onQueryChange = onTextChange,
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier,
        placeHolderTitle = placeHolderTitle,
        resultState = state,
        resultHandler = { results ->
            RoomMemberList(
                isDebugBuild = isDebugBuild,
                roomMembers = results,
                showMembersCount = false,
                onSelectUser = { onSelectUser(it) },
                canDisplayBannedUsersControls = false,
                selectedSection = selectedSection,
                onSelectedSectionChange = {},
            )
        },
    )
}

@PreviewsDayNight
@Composable
internal fun RoomMemberListViewPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) = ElementPreview {
    RoomMemberListView(
        state = state,
        navigator = object : RoomMemberListNavigator {},
    )
}

@PreviewsDayNight
@Composable
internal fun RoomMemberListViewBannedPreview(@PreviewParameter(RoomMemberListStateBannedProvider::class) state: RoomMemberListState) = ElementPreview {
    RoomMemberListView(
        initialSelectedSectionIndex = 1,
        state = state,
        navigator = object : RoomMemberListNavigator {},
    )
}
