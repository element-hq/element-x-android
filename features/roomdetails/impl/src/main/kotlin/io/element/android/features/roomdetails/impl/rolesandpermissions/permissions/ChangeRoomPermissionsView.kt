/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoomPermissionsView(
    state: ChangeRoomPermissionsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        state.eventSink(ChangeRoomPermissionsEvent.Exit)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            val title = when (state.section) {
                ChangeRoomPermissionsSection.RoomDetails -> stringResource(R.string.screen_room_change_permissions_room_details)
                ChangeRoomPermissionsSection.MessagesAndContent -> stringResource(R.string.screen_room_change_permissions_messages_and_content)
                ChangeRoomPermissionsSection.MembershipModeration -> stringResource(R.string.screen_room_change_permissions_member_moderation)
            }
            TopAppBar(
                title = { Text(text = title, style = ElementTheme.typography.aliasScreenTitle) },
                navigationIcon = {
                    BackButton(onClick = { state.eventSink(ChangeRoomPermissionsEvent.Exit) })
                },
                actions = {
                    TextButton(
                        text = stringResource(CommonStrings.action_save),
                        onClick = { state.eventSink(ChangeRoomPermissionsEvent.Save) },
                        enabled = state.hasChanges,
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            for ((index, permissionItem) in state.items.withIndex()) {
                ListSectionHeader(titleForSection(item = permissionItem), hasDivider = index > 0)
                SelectRoleItem(
                    permissionsItem = permissionItem,
                    role = RoomMember.Role.ADMIN,
                    currentPermissions = state.currentPermissions
                ) { item, role ->
                    state.eventSink(ChangeRoomPermissionsEvent.ChangeRole(item, role))
                }
                SelectRoleItem(
                    permissionsItem = permissionItem,
                    role = RoomMember.Role.MODERATOR,
                    currentPermissions = state.currentPermissions
                ) { item, role ->
                    state.eventSink(ChangeRoomPermissionsEvent.ChangeRole(item, role))
                }
                SelectRoleItem(
                    permissionsItem = permissionItem,
                    role = RoomMember.Role.USER,
                    currentPermissions = state.currentPermissions
                ) { item, role ->
                    state.eventSink(ChangeRoomPermissionsEvent.ChangeRole(item, role))
                }
            }
        }
    }

    AsyncActionView(
        async = state.saveAction,
        onSuccess = { state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions) },
        onErrorDismiss = { state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions) }
    )

    AsyncActionView(
        async = state.confirmExitAction,
        onSuccess = { onBackPressed() },
        confirmationDialog = {
            ConfirmationDialog(
                title = stringResource(R.string.screen_room_change_role_unsaved_changes_title),
                content = stringResource(R.string.screen_room_change_role_unsaved_changes_description),
                submitText = stringResource(CommonStrings.action_save),
                cancelText = stringResource(CommonStrings.action_go_back),
                onSubmitClicked = { state.eventSink(ChangeRoomPermissionsEvent.Save) },
                onCancelClicked = { state.eventSink(ChangeRoomPermissionsEvent.Exit) },
                onDismiss = { state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions) }
            )
        },
        onErrorDismiss = {}
    )
}

@Composable
private fun SelectRoleItem(
    permissionsItem: RoomPermissionsItem,
    role: RoomMember.Role,
    currentPermissions: ImmutableMap<RoomPermissionsItem, RoomMember.Role>,
    onClick: (RoomPermissionsItem, RoomMember.Role) -> Unit
) {
    val title = when (role) {
        RoomMember.Role.ADMIN -> stringResource(R.string.screen_room_change_permissions_administrators)
        RoomMember.Role.MODERATOR -> stringResource(R.string.screen_room_change_permissions_moderators)
        RoomMember.Role.USER -> stringResource(R.string.screen_room_change_permissions_everyone)
    }
    ListItem(
        headlineContent = { Text(text = title) },
        trailingContent = if (currentPermissions[permissionsItem] == role) {
            ListItemContent.Icon(IconSource.Vector(CompoundIcons.Check()))
        } else {
            null
        },
        style = ListItemStyle.Primary,
        onClick = { onClick(permissionsItem, role) },
    )
}

@Composable
private fun titleForSection(item: RoomPermissionsItem): String = when (item) {
    RoomPermissionsItem.INVITE -> stringResource(R.string.screen_room_change_permissions_invite_people)
    RoomPermissionsItem.KICK -> stringResource(R.string.screen_room_change_permissions_remove_people)
    RoomPermissionsItem.BAN -> stringResource(R.string.screen_room_change_permissions_ban_people)
    RoomPermissionsItem.SEND_EVENTS -> stringResource(R.string.screen_room_change_permissions_send_messages)
    RoomPermissionsItem.REDACT_EVENTS -> stringResource(R.string.screen_room_change_permissions_delete_messages)
    RoomPermissionsItem.ROOM_NAME -> stringResource(R.string.screen_room_change_permissions_room_name)
    RoomPermissionsItem.ROOM_AVATAR -> stringResource(R.string.screen_room_change_permissions_room_avatar)
    RoomPermissionsItem.ROOM_TOPIC -> stringResource(R.string.screen_room_change_permissions_room_topic)
}

@PreviewsDayNight
@Composable
internal fun ChangeRoomPermissionsViewPreview(@PreviewParameter(ChangeRoomPermissionsStatePreviewProvider::class) state: ChangeRoomPermissionsState) {
    ElementPreview {
        ChangeRoomPermissionsView(
            state = state,
            onBackPressed = {},
        )
    }
}
