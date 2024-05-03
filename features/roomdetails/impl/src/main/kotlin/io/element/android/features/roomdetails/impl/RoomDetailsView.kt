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

package io.element.android.features.roomdetails.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.userprofile.shared.UserProfileHeaderSection
import io.element.android.features.userprofile.shared.UserProfileMainActionsSection
import io.element.android.features.userprofile.shared.blockuser.BlockUserDialogs
import io.element.android.features.userprofile.shared.blockuser.BlockUserSection
import io.element.android.libraries.architecture.coverage.ExcludeFromCoverage
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.MainActionButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.preview.PreviewWithLargeHeight
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
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomDetailsView(
    state: RoomDetailsState,
    goBack: () -> Unit,
    onActionClicked: (RoomDetailsAction) -> Unit,
    onShareRoom: () -> Unit,
    onShareMember: (RoomMember) -> Unit,
    openRoomMemberList: () -> Unit,
    openRoomNotificationSettings: () -> Unit,
    invitePeople: () -> Unit,
    openAvatarPreview: (name: String, url: String) -> Unit,
    openPollHistory: () -> Unit,
    openAdminSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onShareMember() {
        onShareMember((state.roomType as RoomDetailsType.Dm).roomMember)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            RoomDetailsTopBar(
                goBack = goBack,
                showEdit = state.canEdit,
                onActionClicked = onActionClicked
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
                        openAvatarPreview = { avatarUrl ->
                            openAvatarPreview(state.roomName, avatarUrl)
                        },
                    )
                    MainActionsSection(
                        state = state,
                        onShareRoom = onShareRoom
                    )
                }

                is RoomDetailsType.Dm -> {
                    val member = state.roomType.roomMember
                    UserProfileHeaderSection(
                        avatarUrl = state.roomAvatarUrl ?: member.avatarUrl,
                        userId = member.userId,
                        userName = state.roomName,
                        openAvatarPreview = { avatarUrl ->
                            openAvatarPreview(member.getBestName(), avatarUrl)
                        },
                    )
                    UserProfileMainActionsSection(onShareUser = ::onShareMember)
                }
            }
            Spacer(Modifier.height(18.dp))

            if (state.roomTopic !is RoomTopicState.Hidden) {
                TopicSection(
                    roomTopic = state.roomTopic,
                    onActionClicked = onActionClicked,
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

                if (state.displayRolesAndPermissionsSettings) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.screen_room_details_roles_and_permissions)) },
                        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Admin())),
                        onClick = openAdminSettings,
                    )
                }
            }

            val displayMemberListItem = state.roomType is RoomDetailsType.Room
            val displayInviteMembersItem = state.canInvite
            if (displayMemberListItem || displayInviteMembersItem) {
                PreferenceCategory {
                    if (displayMemberListItem) {
                        MembersItem(
                            memberCount = state.memberCount,
                            openRoomMemberList = openRoomMemberList,
                        )
                    }
                    if (displayInviteMembersItem) {
                        InviteItem(
                            invitePeople = invitePeople
                        )
                    }
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
    onActionClicked: (RoomDetailsAction) -> Unit,
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
                            onActionClicked(RoomDetailsAction.Edit)
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
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
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
        Spacer(modifier = Modifier.width(20.dp))
        MainActionButton(
            title = stringResource(R.string.screen_room_details_share_room_title),
            imageVector = CompoundIcons.ShareAndroid(),
            onClick = onShareRoom
        )
    }
}

@Composable
private fun RoomHeaderSection(
    avatarUrl: String?,
    roomId: RoomId,
    roomName: String,
    roomAlias: RoomAlias?,
    openAvatarPreview: (url: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(
            avatarData = AvatarData(roomId.value, roomName, avatarUrl, AvatarSize.RoomHeader),
            modifier = Modifier
                .size(70.dp)
                .clickable(enabled = avatarUrl != null) { openAvatarPreview(avatarUrl!!) }
                .testTag(TestTags.roomDetailAvatar)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = roomName,
            style = ElementTheme.typography.fontHeadingLgBold,
            textAlign = TextAlign.Center,
        )
        if (roomAlias != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = roomAlias.value,
                style = ElementTheme.typography.fontBodyLgRegular,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun TopicSection(
    roomTopic: RoomTopicState,
    onActionClicked: (RoomDetailsAction) -> Unit,
) {
    PreferenceCategory(title = stringResource(CommonStrings.common_topic)) {
        if (roomTopic is RoomTopicState.CanAddTopic) {
            PreferenceText(
                title = stringResource(R.string.screen_room_details_add_topic_title),
                icon = Icons.Outlined.Add,
                onClick = { onActionClicked(RoomDetailsAction.AddTopic) },
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
private fun InviteItem(
    invitePeople: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.screen_room_details_invite_people_title)) },
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.UserAdd())),
        onClick = invitePeople,
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
    PreferenceCategory(showDivider = false) {
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
        onActionClicked = {},
        onShareRoom = {},
        onShareMember = {},
        openRoomMemberList = {},
        openRoomNotificationSettings = {},
        invitePeople = {},
        openAvatarPreview = { _, _ -> },
        openPollHistory = {},
        openAdminSettings = {},
    )
}
