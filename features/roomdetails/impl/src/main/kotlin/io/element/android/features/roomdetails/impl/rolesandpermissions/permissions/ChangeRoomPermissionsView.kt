/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.core.bool.orFalse
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
import io.element.android.libraries.matrix.api.room.powerlevels.MatrixRoomPowerLevels
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoomPermissionsView(
    state: ChangeRoomPermissionsState,
    onBackClick: () -> Unit,
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
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            for ((index, permissionItem) in state.items.withIndex()) {
                item {
                    ListSectionHeader(titleForSection(item = permissionItem), hasDivider = index > 0)
                    SelectRoleItem(
                        permissionsItem = permissionItem,
                        role = RoomMember.Role.ADMIN,
                        currentPermissions = state.currentPermissions
                    ) { item, role ->
                        state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(item, role))
                    }
                    SelectRoleItem(
                        permissionsItem = permissionItem,
                        role = RoomMember.Role.MODERATOR,
                        currentPermissions = state.currentPermissions
                    ) { item, role ->
                        state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(item, role))
                    }
                    SelectRoleItem(
                        permissionsItem = permissionItem,
                        role = RoomMember.Role.USER,
                        currentPermissions = state.currentPermissions
                    ) { item, role ->
                        state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(item, role))
                    }
                }
            }
        }
    }

    AsyncActionView(
        async = state.saveAction,
        onSuccess = { onBackClick() },
        onErrorDismiss = { state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions) }
    )

    AsyncActionView(
        async = state.confirmExitAction,
        onSuccess = { onBackClick() },
        confirmationDialog = {
            ConfirmationDialog(
                title = stringResource(R.string.screen_room_change_role_unsaved_changes_title),
                content = stringResource(R.string.screen_room_change_role_unsaved_changes_description),
                submitText = stringResource(CommonStrings.action_save),
                cancelText = stringResource(CommonStrings.action_discard),
                onSubmitClick = { state.eventSink(ChangeRoomPermissionsEvent.Save) },
                onDismiss = { state.eventSink(ChangeRoomPermissionsEvent.Exit) }
            )
        },
        onErrorDismiss = {},
    )
}

@Composable
private fun SelectRoleItem(
    permissionsItem: RoomPermissionType,
    role: RoomMember.Role,
    currentPermissions: MatrixRoomPowerLevels?,
    onClick: (RoomPermissionType, RoomMember.Role) -> Unit
) {
    val title = when (role) {
        RoomMember.Role.ADMIN -> stringResource(R.string.screen_room_change_permissions_administrators)
        RoomMember.Role.MODERATOR -> stringResource(R.string.screen_room_change_permissions_moderators)
        RoomMember.Role.USER -> stringResource(R.string.screen_room_change_permissions_everyone)
    }
    ListItem(
        headlineContent = { Text(text = title) },
        trailingContent = if (currentPermissions?.isSelected(permissionsItem, role).orFalse()) {
            ListItemContent.Icon(IconSource.Vector(CompoundIcons.Check()))
        } else {
            null
        },
        style = ListItemStyle.Primary,
        onClick = { onClick(permissionsItem, role) },
    )
}

private fun MatrixRoomPowerLevels.isSelected(item: RoomPermissionType, role: RoomMember.Role): Boolean {
    return when (item) {
        RoomPermissionType.BAN -> RoomMember.Role.forPowerLevel(ban) == role
        RoomPermissionType.INVITE -> RoomMember.Role.forPowerLevel(invite) == role
        RoomPermissionType.KICK -> RoomMember.Role.forPowerLevel(kick) == role
        RoomPermissionType.SEND_EVENTS -> RoomMember.Role.forPowerLevel(sendEvents) == role
        RoomPermissionType.REDACT_EVENTS -> RoomMember.Role.forPowerLevel(redactEvents) == role
        RoomPermissionType.ROOM_NAME -> RoomMember.Role.forPowerLevel(roomName) == role
        RoomPermissionType.ROOM_AVATAR -> RoomMember.Role.forPowerLevel(roomAvatar) == role
        RoomPermissionType.ROOM_TOPIC -> RoomMember.Role.forPowerLevel(roomTopic) == role
    }
}

@Composable
private fun titleForSection(item: RoomPermissionType): String = when (item) {
    RoomPermissionType.INVITE -> stringResource(R.string.screen_room_change_permissions_invite_people)
    RoomPermissionType.KICK -> stringResource(R.string.screen_room_change_permissions_remove_people)
    RoomPermissionType.BAN -> stringResource(R.string.screen_room_change_permissions_ban_people)
    RoomPermissionType.SEND_EVENTS -> stringResource(R.string.screen_room_change_permissions_send_messages)
    RoomPermissionType.REDACT_EVENTS -> stringResource(R.string.screen_room_change_permissions_delete_messages)
    RoomPermissionType.ROOM_NAME -> stringResource(R.string.screen_room_change_permissions_room_name)
    RoomPermissionType.ROOM_AVATAR -> stringResource(R.string.screen_room_change_permissions_room_avatar)
    RoomPermissionType.ROOM_TOPIC -> stringResource(R.string.screen_room_change_permissions_room_topic)
}

@PreviewsDayNight
@Composable
internal fun ChangeRoomPermissionsViewPreview(@PreviewParameter(ChangeRoomPermissionsStateProvider::class) state: ChangeRoomPermissionsState) {
    ElementPreview {
        ChangeRoomPermissionsView(
            state = state,
            onBackClick = {},
        )
    }
}
