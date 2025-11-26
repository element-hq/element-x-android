/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchField
import io.element.android.libraries.designsystem.theme.components.SegmentedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.getBestName
import io.element.android.libraries.matrix.api.room.toMatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun RoomMemberListView(
    state: RoomMemberListState,
    navigator: RoomMemberListNavigator,
    modifier: Modifier = Modifier,
) {
    fun onSelectUser(roomMember: RoomMember) {
        state.eventSink(RoomMemberListEvents.RoomMemberSelected(roomMember))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            RoomMemberListTopBar(
                canInvite = state.canInvite,
                onBackClick = navigator::exitRoomMemberList,
                onInviteClick = navigator::openInviteMembers,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            var searchQuery by textFieldState(state.searchQuery)
            SearchField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                    state.eventSink(RoomMemberListEvents.UpdateSearchQuery(newQuery))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = stringResource(CommonStrings.common_search_for_someone),
            )
            RoomMemberList(
                roomMembersData = state.filteredRoomMembers,
                selectedSection = state.selectedSection,
                showBannedSection = state.showBannedSection,
                searchQuery = state.searchQuery,
                onSelectedSectionChange = { state.eventSink(RoomMemberListEvents.ChangeSelectedSection(it)) },
                onSelectUser = ::onSelectUser,
            )
        }
    }
}

@Composable
private fun RoomMemberList(
    roomMembersData: AsyncData<RoomMembers>,
    selectedSection: SelectedSection,
    showBannedSection: Boolean,
    searchQuery: String,
    onSelectedSectionChange: (SelectedSection) -> Unit,
    onSelectUser: (RoomMember) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxWidth(), state = rememberLazyListState()) {
        stickyHeader {
            Column {
                AnimatedVisibility(visible = showBannedSection) {
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
                AnimatedVisibility(visible = roomMembersData.isLoading()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
        when (roomMembersData) {
            is AsyncData.Failure -> failureItem(roomMembersData.error)
            is AsyncData.Loading,
            is AsyncData.Success -> {
                val roomMembers = roomMembersData.dataOrNull() ?: return@LazyColumn
                if (roomMembers.isEmpty(selectedSection)) {
                    emptySearchItem(searchQuery)
                } else {
                    memberItems(
                        roomMembers = roomMembers,
                        selectedSection = selectedSection,
                        onSelectUser = onSelectUser,
                    )
                }
            }
            AsyncData.Uninitialized -> Unit
        }
    }
}

private fun LazyListScope.memberItems(
    roomMembers: RoomMembers,
    selectedSection: SelectedSection,
    onSelectUser: (RoomMember) -> Unit,
) {
    when (selectedSection) {
        SelectedSection.MEMBERS -> {
            if (roomMembers.invited.isNotEmpty()) {
                roomMemberListSectionHeader(
                    text = {
                        val memberCount = roomMembers.invited.count()
                        pluralStringResource(id = R.plurals.screen_room_member_list_pending_header_title, memberCount, memberCount)
                    },
                )
                roomMemberListSectionItems(
                    members = roomMembers.invited,
                    onMemberSelected = { onSelectUser(it) }
                )
            }
            if (roomMembers.joined.isNotEmpty()) {
                roomMemberListSectionHeader(
                    text = {
                        val memberCount = roomMembers.joined.count()
                        pluralStringResource(id = R.plurals.screen_room_member_list_header_title, count = memberCount, memberCount)
                    },
                )
                roomMemberListSectionItems(
                    members = roomMembers.joined,
                    onMemberSelected = { onSelectUser(it) }
                )
            }
        }
        SelectedSection.BANNED -> {
            if (roomMembers.banned.isNotEmpty()) {
                roomMemberListSectionHeader(
                    text = {
                        val memberCount = roomMembers.banned.count()
                        pluralStringResource(id = R.plurals.screen_room_member_list_banned_header_title, memberCount, memberCount)
                    },
                    isCritical = true,
                )
                roomMemberListSectionItems(
                    members = roomMembers.banned,
                    onMemberSelected = { onSelectUser(it) }
                )
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

private fun LazyListScope.roomMemberListSectionHeader(
    text: @Composable (() -> String),
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
) {
    item {
        Text(
            modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = text(),
            style = ElementTheme.typography.fontBodyLgMedium,
            color = if (isCritical) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textPrimary,
        )
    }
}

private fun LazyListScope.roomMemberListSectionItems(
    members: ImmutableList<RoomMemberWithIdentityState>?,
    onMemberSelected: (RoomMember) -> Unit,
) {
    items(members.orEmpty()) { matrixUser ->
        RoomMemberListItem(
            modifier = Modifier.fillMaxWidth(),
            roomMemberWithIdentity = matrixUser,
            onClick = { onMemberSelected(matrixUser.roomMember) }
        )
    }
}

private fun LazyListScope.emptySearchItem(searchQuery: String) {
    item {
        IconTitleSubtitleMolecule(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            iconStyle = BigIcon.Style.Default(
                vectorIcon = CompoundIcons.Search(),
                contentDescription = null,
            ),
            title = stringResource(R.string.screen_room_member_list_empty_search_title, searchQuery),
            subTitle = stringResource(R.string.screen_room_member_list_empty_search_subtitle),
        )
    }
}

@Composable
private fun RoomMemberListItem(
    roomMemberWithIdentity: RoomMemberWithIdentityState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val member = roomMemberWithIdentity.roomMember
    val roleText = when (member.role) {
        RoomMember.Role.Admin -> stringResource(R.string.screen_room_member_list_role_administrator)
        RoomMember.Role.Moderator -> stringResource(R.string.screen_room_member_list_role_moderator)
        is RoomMember.Role.Owner -> stringResource(R.string.screen_room_member_list_role_owner)
        else -> null
    }

    MatrixUserRow(
        modifier = modifier.clickable(onClick = onClick),
        matrixUser = roomMemberWithIdentity.roomMember.toMatrixUser(),
        avatarSize = AvatarSize.UserListItem,
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (roomMemberWithIdentity.identityState) {
                    IdentityState.Verified -> {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.Verified(),
                            contentDescription = stringResource(CommonStrings.common_verified),
                            tint = ElementTheme.colors.iconSuccessPrimary
                        )
                    }
                    IdentityState.VerificationViolation -> {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            imageVector = CompoundIcons.ErrorSolid(),
                            contentDescription = stringResource(
                                CommonStrings.crypto_identity_change_profile_pin_violation,
                                roomMemberWithIdentity.roomMember.getBestName()
                            ),
                            tint = ElementTheme.colors.iconCriticalPrimary
                        )
                    }
                    else -> Unit
                }

                roleText?.let {
                    Text(
                        text = it,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
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
        titleStr = stringResource(CommonStrings.common_people),
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

@PreviewsDayNight
@Composable
internal fun RoomMemberListViewPreview(@PreviewParameter(RoomMemberListStateProvider::class) state: RoomMemberListState) = ElementPreview {
    RoomMemberListView(
        state = state,
        navigator = object : RoomMemberListNavigator {},
    )
}
