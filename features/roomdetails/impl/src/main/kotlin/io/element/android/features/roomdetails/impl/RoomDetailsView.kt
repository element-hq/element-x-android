/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.userprofile.shared.blockuser.BlockUserDialogs
import io.element.android.features.userprofile.shared.blockuser.BlockUserSection
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.atomic.atoms.MatrixBadgeAtom
import io.element.android.libraries.designsystem.atomic.molecules.MatrixBadgeRowMolecule
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.CompositeAvatar
import io.element.android.libraries.designsystem.components.avatar.DmAvatars
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.MainActionButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.getBestName
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.compose.LocalAnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun RoomDetailsView(
    state: RoomDetailsState,
    goBack: () -> Unit,
    onActionClick: (RoomDetailsAction) -> Unit,
    onShareRoom: () -> Unit,
    openRoomMemberList: () -> Unit,
    openRoomNotificationSettings: () -> Unit,
    invitePeople: () -> Unit,
    openAvatarPreview: (name: String, url: String) -> Unit,
    openPollHistory: () -> Unit,
    openAdminSettings: () -> Unit,
    onJoinCallClick: () -> Unit,
    onPinnedMessagesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomDetailsTopBar(
                goBack = goBack,
                showEdit = state.canEdit,
                onActionClick = onActionClick
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .consumeWindowInsets(padding)
        ) {
            LeaveRoomView(state = state.leaveRoomState)

            when (state.roomType) {
                RoomDetailsType.Room -> {
                    RoomHeaderSection(
                        avatarUrl = state.roomAvatarUrl,
                        roomId = state.roomId,
                        roomName = state.roomName,
                        roomAlias = state.roomAlias,
                        heroes = state.heroes,
                        openAvatarPreview = { avatarUrl ->
                            openAvatarPreview(state.roomName, avatarUrl)
                        },
                    )
                }
                is RoomDetailsType.Dm -> {
                    DmHeaderSection(
                        me = state.roomType.me,
                        otherMember = state.roomType.otherMember,
                        roomName = state.roomName,
                        openAvatarPreview = { name, avatarUrl ->
                            openAvatarPreview(name, avatarUrl)
                        },
                    )
                }
            }
            BadgeList(
                roomBadge = state.roomBadges,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(32.dp))
            MainActionsSection(
                state = state,
                onShareRoom = onShareRoom,
                onInvitePeople = invitePeople,
                onCall = onJoinCallClick,
            )
            Spacer(Modifier.height(12.dp))

            if (state.roomTopic !is RoomTopicState.Hidden) {
                TopicSection(
                    roomTopic = state.roomTopic,
                    onActionClick = onActionClick,
                )
            }

            PreferenceCategory {
                if (state.canShowNotificationSettings && state.roomNotificationSettings != null) {
                    NotificationItem(
                        isDefaultMode = state.roomNotificationSettings.isDefault,
                        openRoomNotificationSettings = openRoomNotificationSettings
                    )
                }

                FavoriteItem(
                    isFavorite = state.isFavorite,
                    onFavoriteChanges = {
                        state.eventSink(RoomDetailsEvent.SetFavorite(it))
                    }
                )

                if (state.canShowPinnedMessages) {
                    PinnedMessagesItem(
                        pinnedMessagesCount = state.pinnedMessagesCount,
                        onPinnedMessagesClick = onPinnedMessagesClick
                    )
                }

                if (state.displayRolesAndPermissionsSettings) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.screen_room_details_roles_and_permissions)) },
                        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Admin())),
                        onClick = openAdminSettings,
                    )
                }
            }

            val displayMemberListItem = state.roomType is RoomDetailsType.Room
            if (displayMemberListItem) {
                PreferenceCategory {
                    MembersItem(
                        memberCount = state.memberCount,
                        openRoomMemberList = openRoomMemberList,
                    )
                }
            }

            PollsSection(
                openPollHistory = openPollHistory
            )

            if (state.isEncrypted) {
                SecuritySection()
            }

            if (state.roomType is RoomDetailsType.Dm && state.roomMemberDetailsState != null) {
                val roomMemberState = state.roomMemberDetailsState
                BlockUserSection(roomMemberState)
                BlockUserDialogs(roomMemberState)
            }

            OtherActionsSection(
                isDm = state.roomType is RoomDetailsType.Dm,
                onLeaveRoom = { state.eventSink(RoomDetailsEvent.LeaveRoom) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomDetailsTopBar(
    goBack: () -> Unit,
    onActionClick: (RoomDetailsAction) -> Unit,
    showEdit: Boolean,
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { },
        navigationIcon = { BackButton(onClick = goBack) },
        actions = {
            if (showEdit) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, stringResource(id = CommonStrings.a11y_user_menu))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = CommonStrings.action_edit)) },
                        onClick = {
                            // Explicitly close the menu before handling the action, as otherwise it stays open during the
                            // transition and renders really badly.
                            showMenu = false
                            onActionClick(RoomDetailsAction.Edit)
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun MainActionsSection(
    state: RoomDetailsState,
    onShareRoom: () -> Unit,
    onInvitePeople: () -> Unit,
    onCall: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        val roomNotificationSettings = state.roomNotificationSettings
        if (state.canShowNotificationSettings && roomNotificationSettings != null) {
            if (roomNotificationSettings.mode == RoomNotificationMode.MUTE) {
                MainActionButton(
                    title = stringResource(CommonStrings.common_unmute),
                    imageVector = CompoundIcons.NotificationsOff(),
                    onClick = {
                        state.eventSink(RoomDetailsEvent.UnmuteNotification)
                    },
                )
            } else {
                MainActionButton(
                    title = stringResource(CommonStrings.common_mute),
                    imageVector = CompoundIcons.Notifications(),
                    onClick = {
                        state.eventSink(RoomDetailsEvent.MuteNotification)
                    },
                )
            }
        }
        if (state.canCall) {
            MainActionButton(
                title = stringResource(CommonStrings.action_call),
                imageVector = CompoundIcons.VideoCall(),
                onClick = onCall,
            )
        }
        if (state.roomType is RoomDetailsType.Room) {
            if (state.canInvite) {
                MainActionButton(
                    title = stringResource(CommonStrings.action_invite),
                    imageVector = CompoundIcons.UserAdd(),
                    onClick = onInvitePeople,
                )
            }
            // Share CTA should be hidden for DMs
            MainActionButton(
                title = stringResource(CommonStrings.action_share),
                imageVector = CompoundIcons.ShareAndroid(),
                onClick = onShareRoom
            )
        }
    }
}

@Composable
private fun RoomHeaderSection(
    avatarUrl: String?,
    roomId: RoomId,
    roomName: String,
    roomAlias: RoomAlias?,
    heroes: ImmutableList<MatrixUser>,
    openAvatarPreview: (url: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositeAvatar(
            avatarData = AvatarData(roomId.value, roomName, avatarUrl, AvatarSize.RoomHeader),
            heroes = heroes.map { user ->
                user.getAvatarData(size = AvatarSize.RoomHeader)
            }.toPersistentList(),
            modifier = Modifier
                .clickable(enabled = avatarUrl != null) { openAvatarPreview(avatarUrl!!) }
                .testTag(TestTags.roomDetailAvatar)
        )
        TitleAndSubtitle(title = roomName, subtitle = roomAlias?.value)
    }
}

@Composable
private fun DmHeaderSection(
    me: RoomMember,
    otherMember: RoomMember,
    roomName: String,
    openAvatarPreview: (name: String, url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DmAvatars(
            userAvatarData = me.getAvatarData(size = AvatarSize.DmCluster),
            otherUserAvatarData = otherMember.getAvatarData(size = AvatarSize.DmCluster),
            openAvatarPreview = { url -> openAvatarPreview(me.getBestName(), url) },
            openOtherAvatarPreview = { url -> openAvatarPreview(roomName, url) },
        )
        TitleAndSubtitle(
            title = roomName,
            subtitle = otherMember.userId.value,
        )
    }
}

@Composable
private fun ColumnScope.TitleAndSubtitle(
    title: String,
    subtitle: String?,
) {
    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = title,
        style = ElementTheme.typography.fontHeadingLgBold,
        textAlign = TextAlign.Center,
    )
    if (subtitle != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BadgeList(
    roomBadge: ImmutableList<RoomBadge>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (roomBadge.isNotEmpty()) {
            MatrixBadgeRowMolecule(
                data = roomBadge.map {
                    it.toMatrixBadgeData()
                }.toImmutableList(),
            )
        }
    }
}

@Composable
private fun RoomBadge.toMatrixBadgeData(): MatrixBadgeAtom.MatrixBadgeData {
    return when (this) {
        RoomBadge.ENCRYPTED -> {
            MatrixBadgeAtom.MatrixBadgeData(
                text = stringResource(R.string.screen_room_details_badge_encrypted),
                icon = CompoundIcons.LockSolid(),
                type = MatrixBadgeAtom.Type.Positive,
            )
        }
        RoomBadge.NOT_ENCRYPTED -> {
            MatrixBadgeAtom.MatrixBadgeData(
                text = stringResource(R.string.screen_room_details_badge_not_encrypted),
                icon = CompoundIcons.LockOff(),
                type = MatrixBadgeAtom.Type.Neutral,
            )
        }
        RoomBadge.PUBLIC -> {
            MatrixBadgeAtom.MatrixBadgeData(
                text = stringResource(R.string.screen_room_details_badge_public),
                icon = CompoundIcons.Public(),
                type = MatrixBadgeAtom.Type.Neutral,
            )
        }
    }
}

@Composable
private fun TopicSection(
    roomTopic: RoomTopicState,
    onActionClick: (RoomDetailsAction) -> Unit,
) {
    PreferenceCategory(
        title = stringResource(CommonStrings.common_topic),
        showTopDivider = false,
    ) {
        if (roomTopic is RoomTopicState.CanAddTopic) {
            PreferenceText(
                title = stringResource(R.string.screen_room_details_add_topic_title),
                icon = Icons.Outlined.Add,
                onClick = { onActionClick(RoomDetailsAction.AddTopic) },
            )
        } else if (roomTopic is RoomTopicState.ExistingTopic) {
            ClickableLinkText(
                text = roomTopic.topic,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                interactionSource = remember { MutableInteractionSource() },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.tertiary,
                ),
            )
        }
    }
}

@Composable
private fun NotificationItem(
    isDefaultMode: Boolean,
    openRoomNotificationSettings: () -> Unit,
) {
    val subtitle = if (isDefaultMode) {
        stringResource(R.string.screen_room_details_notification_mode_default)
    } else {
        stringResource(R.string.screen_room_details_notification_mode_custom)
    }
    ListItem(
        headlineContent = { Text(text = stringResource(R.string.screen_room_details_notification_title)) },
        supportingContent = { Text(text = subtitle) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Notifications())),
        onClick = openRoomNotificationSettings,
    )
}

@Composable
private fun FavoriteItem(
    isFavorite: Boolean,
    onFavoriteChanges: (Boolean) -> Unit,
) {
    PreferenceSwitch(
        icon = CompoundIcons.Favourite(),
        title = stringResource(id = CommonStrings.common_favourite),
        isChecked = isFavorite,
        onCheckedChange = onFavoriteChanges
    )
}

@Composable
private fun MembersItem(
    memberCount: Long,
    openRoomMemberList: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(CommonStrings.common_people)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.User())),
        trailingContent = ListItemContent.Text(memberCount.toString()),
        onClick = openRoomMemberList,
    )
}

@Composable
private fun PinnedMessagesItem(
    pinnedMessagesCount: Int?,
    onPinnedMessagesClick: () -> Unit,
) {
    val analyticsService = LocalAnalyticsService.current
    ListItem(
        headlineContent = { Text(stringResource(CommonStrings.screen_room_details_pinned_events_row_title)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Pin())),
        trailingContent =
        if (pinnedMessagesCount == null) {
            ListItemContent.Custom {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }
        } else {
            ListItemContent.Text(pinnedMessagesCount.toString())
        },
        onClick = {
            analyticsService.captureInteraction(Interaction.Name.PinnedMessageRoomInfoButton)
            onPinnedMessagesClick()
        }
    )
}

@Composable
private fun PollsSection(
    openPollHistory: () -> Unit,
) {
    PreferenceCategory {
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_polls_history_title)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Polls())),
            onClick = openPollHistory,
        )
    }
}

@Composable
private fun SecuritySection() {
    PreferenceCategory(title = stringResource(R.string.screen_room_details_security_title)) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_details_encryption_enabled_title)) },
            supportingContent = { Text(stringResource(R.string.screen_room_details_encryption_enabled_subtitle)) },
            leadingContent = ListItemContent.Icon(IconSource.Resource(CommonDrawables.ic_encryption_enabled)),
        )
    }
}

@Composable
private fun OtherActionsSection(isDm: Boolean, onLeaveRoom: () -> Unit) {
    PreferenceCategory(showTopDivider = true) {
        ListItem(
            headlineContent = {
                val leaveText = stringResource(
                    id = if (isDm) {
                        R.string.screen_room_details_leave_conversation_title
                    } else {
                        R.string.screen_room_details_leave_room_title
                    }
                )
                Text(leaveText)
            },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Leave())),
            style = ListItemStyle.Destructive,
            onClick = onLeaveRoom,
        )
    }
}

@PreviewWithLargeHeight
@Composable
internal fun RoomDetailsPreview(@PreviewParameter(RoomDetailsStateProvider::class) state: RoomDetailsState) =
    ElementPreviewLight { ContentToPreview(state) }

@PreviewWithLargeHeight
@Composable
internal fun RoomDetailsDarkPreview(@PreviewParameter(RoomDetailsStateProvider::class) state: RoomDetailsState) =
    ElementPreviewDark { ContentToPreview(state) }

@ExcludeFromCoverage
@Composable
private fun ContentToPreview(state: RoomDetailsState) {
    RoomDetailsView(
        state = state,
        goBack = {},
        onActionClick = {},
        onShareRoom = {},
        openRoomMemberList = {},
        openRoomNotificationSettings = {},
        invitePeople = {},
        openAvatarPreview = { _, _ -> },
        openPollHistory = {},
        openAdminSettings = {},
        onJoinCallClick = {},
        onPinnedMessagesClick = {},
    )
}
