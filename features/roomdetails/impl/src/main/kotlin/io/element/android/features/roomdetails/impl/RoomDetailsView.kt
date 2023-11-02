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
import androidx.compose.material.icons.outlined.Person
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
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.roomdetails.impl.blockuser.BlockUserDialogs
import io.element.android.features.roomdetails.impl.blockuser.BlockUserSection
import io.element.android.features.roomdetails.impl.members.details.RoomMemberHeaderSection
import io.element.android.features.roomdetails.impl.members.details.RoomMemberMainActionsSection
import io.element.android.libraries.composeutils.annotations.PreviewWithLargeHeight
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.MainActionButton
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.theme.ElementTheme
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
                        roomAlias = state.roomAlias
                    )
                    MainActionsSection(
                        state = state,
                        onShareRoom = onShareRoom
                    )
                }

                is RoomDetailsType.Dm -> {
                    val member = state.roomType.roomMember
                    RoomMemberHeaderSection(
                        avatarUrl = state.roomAvatarUrl ?: member.avatarUrl,
                        userId = member.userId.value,
                        userName = state.roomName
                    )
                    RoomMemberMainActionsSection(onShareUser = ::onShareMember)
                }
            }
            Spacer(Modifier.height(18.dp))

            if (state.roomTopic !is RoomTopicState.Hidden) {
                TopicSection(
                    roomTopic = state.roomTopic,
                    onActionClicked = onActionClicked,
                )
            }

            if (state.canShowNotificationSettings && state.roomNotificationSettings != null) {
                NotificationSection(
                    isDefaultMode = state.roomNotificationSettings.isDefault,
                    openRoomNotificationSettings = openRoomNotificationSettings
                )
            }

            if (state.roomType is RoomDetailsType.Room) {
                MembersSection(
                    memberCount = state.memberCount,
                    openRoomMemberList = openRoomMemberList,
                )

                if (state.canInvite) {
                    InviteSection(
                        invitePeople = invitePeople
                    )
                }
            }

            if (state.isEncrypted) {
                SecuritySection()
            }

            if (state.roomType is RoomDetailsType.Dm && state.roomMemberDetailsState != null) {
                val roomMemberState = state.roomMemberDetailsState
                BlockUserSection(roomMemberState)
                BlockUserDialogs(roomMemberState)
            }

            OtherActionsSection(onLeaveRoom = {
                state.eventSink(RoomDetailsEvent.LeaveRoom)
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomDetailsTopBar(
    goBack: () -> Unit,
    onActionClicked: (RoomDetailsAction) -> Unit,
    showEdit: Boolean,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        modifier = modifier,
        title = { },
        navigationIcon = { BackButton(onClick = goBack) },
        actions = {
            if (showEdit) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, "")
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
private fun MainActionsSection(state: RoomDetailsState, onShareRoom: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        val roomNotificationSettings = state.roomNotificationSettings
        if (state.canShowNotificationSettings && roomNotificationSettings != null) {
            if (roomNotificationSettings.mode == RoomNotificationMode.MUTE) {
                MainActionButton(
                    title = stringResource(CommonStrings.common_unmute),
                    iconResourceId = CommonDrawables.ic_compound_notifications_off,
                    onClick = {
                        state.eventSink(RoomDetailsEvent.UnmuteNotification)
                    },
                )
            } else {
                MainActionButton(
                    title = stringResource(CommonStrings.common_mute),
                    iconResourceId = CommonDrawables.ic_compound_notifications,
                    onClick = {
                        state.eventSink(RoomDetailsEvent.MuteNotification)
                    },
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        MainActionButton(
            title = stringResource(R.string.screen_room_details_share_room_title),
            iconResourceId = CommonDrawables.ic_compound_share_android,
            onClick = onShareRoom
        )
    }
}

@Composable
private fun RoomHeaderSection(
    avatarUrl: String?,
    roomId: String,
    roomName: String,
    roomAlias: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Avatar(
            avatarData = AvatarData(roomId, roomName, avatarUrl, AvatarSize.RoomHeader),
            modifier = Modifier.size(70.dp)
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
                text = roomAlias,
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
    modifier: Modifier = Modifier
) {
    PreferenceCategory(title = stringResource(CommonStrings.common_topic), modifier = modifier) {
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
private fun NotificationSection(
    isDefaultMode: Boolean,
    openRoomNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtitle = if (isDefaultMode) {
        stringResource(R.string.screen_room_details_notification_mode_default)
    } else {
        stringResource(R.string.screen_room_details_notification_mode_custom)
    }
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_notification_title),
            subtitle = subtitle,
            iconResourceId = CommonDrawables.ic_compound_notifications,
            onClick = openRoomNotificationSettings,
        )
    }
}

@Composable
private fun MembersSection(
    memberCount: Long,
    openRoomMemberList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(CommonStrings.common_people),
            icon = Icons.Outlined.Person,
            currentValue = memberCount.toString(),
            onClick = openRoomMemberList,
        )
    }
}

@Composable
private fun InviteSection(
    invitePeople: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferenceCategory(modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_invite_people_title),
            iconResourceId = CommonDrawables.ic_compound_user_add,
            onClick = invitePeople,
        )
    }
}

@Composable
private fun SecuritySection(modifier: Modifier = Modifier) {
    PreferenceCategory(title = stringResource(R.string.screen_room_details_security_title), modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_encryption_enabled_title),
            subtitle = stringResource(R.string.screen_room_details_encryption_enabled_subtitle),
            iconResourceId = CommonDrawables.ic_compound_lock,
        )
    }
}

@Composable
private fun OtherActionsSection(onLeaveRoom: () -> Unit, modifier: Modifier = Modifier) {
    PreferenceCategory(showDivider = false, modifier = modifier) {
        PreferenceText(
            title = stringResource(R.string.screen_room_details_leave_room_title),
            iconResourceId = CommonDrawables.ic_compound_leave,
            tintColor = MaterialTheme.colorScheme.error,
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
    )
}
