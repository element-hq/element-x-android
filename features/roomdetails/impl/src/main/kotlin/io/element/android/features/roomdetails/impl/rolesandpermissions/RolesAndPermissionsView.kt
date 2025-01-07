/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.sheetStateForPreview
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RolesAndPermissionsView(
    state: RolesAndPermissionsState,
    rolesAndPermissionsNavigator: RolesAndPermissionsNavigator,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        title = stringResource(R.string.screen_room_roles_and_permissions_title),
        onBackClick = rolesAndPermissionsNavigator::onBackClick,
    ) {
        ListSectionHeader(title = stringResource(R.string.screen_room_roles_and_permissions_roles_header), hasDivider = false)
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_admins)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Admin())),
            trailingContent = ListItemContent.Text("${state.adminCount}"),
            onClick = { rolesAndPermissionsNavigator.openAdminList() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_moderators)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ChatProblem())),
            trailingContent = ListItemContent.Text("${state.moderatorCount}"),
            onClick = { rolesAndPermissionsNavigator.openModeratorList() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_change_my_role)) },
            onClick = { state.eventSink(RolesAndPermissionsEvents.ChangeOwnRole) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Edit()))
        )
        ListSectionHeader(title = stringResource(R.string.screen_room_roles_and_permissions_permissions_header), hasDivider = true)
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_room_details)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Info())),
            onClick = { rolesAndPermissionsNavigator.openEditRoomDetailsPermissions() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_messages_and_content)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Chat())),
            onClick = { rolesAndPermissionsNavigator.openMessagesAndContentPermissions() },
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_member_moderation)) },
            leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.User())),
            onClick = { rolesAndPermissionsNavigator.openModerationPermissions() },
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_reset)) },
            onClick = { state.eventSink(RolesAndPermissionsEvents.ResetPermissions) },
            style = ListItemStyle.Destructive,
        )
    }

    AsyncActionView(
        async = state.resetPermissionsAction,
        confirmationDialog = {
            ConfirmationDialog(
                title = stringResource(R.string.screen_room_roles_and_permissions_reset_confirm_title),
                content = stringResource(R.string.screen_room_roles_and_permissions_reset_confirm_description),
                submitText = stringResource(CommonStrings.action_reset),
                destructiveSubmit = true,
                onSubmitClick = { state.eventSink(RolesAndPermissionsEvents.ResetPermissions) },
                onDismiss = { state.eventSink(RolesAndPermissionsEvents.CancelPendingAction) },
            )
        },
        onSuccess = { state.eventSink(RolesAndPermissionsEvents.CancelPendingAction) },
        onErrorDismiss = { state.eventSink(RolesAndPermissionsEvents.CancelPendingAction) }
    )

    when (state.changeOwnRoleAction) {
        is AsyncAction.Confirming -> {
            ChangeOwnRoleBottomSheet(
                eventSink = state.eventSink,
            )
        }
        is AsyncAction.Loading -> {
            ProgressDialog()
        }
        is AsyncAction.Failure -> {
            ErrorDialog(
                content = stringResource(CommonStrings.error_unknown),
                onSubmit = { state.eventSink(RolesAndPermissionsEvents.CancelPendingAction) }
            )
        }
        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeOwnRoleBottomSheet(
    eventSink: (RolesAndPermissionsEvents) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = if (LocalInspectionMode.current) {
        sheetStateForPreview()
    } else {
        rememberModalBottomSheetState(skipPartiallyExpanded = true)
    }
    fun dismiss() {
        sheetState.hide(coroutineScope) {
            eventSink(RolesAndPermissionsEvents.CancelPendingAction)
        }
    }
    ModalBottomSheet(
        modifier = Modifier
            .systemBarsPadding()
            .navigationBarsPadding(),
        sheetState = sheetState,
        onDismissRequest = ::dismiss,
    ) {
        Text(
            modifier = Modifier.padding(14.dp),
            text = stringResource(R.string.screen_room_roles_and_permissions_change_my_role),
            style = ElementTheme.typography.fontBodyLgMedium,
            color = ElementTheme.colors.textPrimary,
        )
        Text(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 16.dp),
            text = stringResource(R.string.screen_room_change_role_confirm_demote_self_description),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textPrimary,
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_change_role_demote_to_moderator)) },
            onClick = {
                sheetState.hide(coroutineScope) {
                    eventSink(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.MODERATOR))
                }
            },
            style = ListItemStyle.Destructive,
        )
        ListItem(
            headlineContent = { Text(stringResource(R.string.screen_room_roles_and_permissions_change_role_demote_to_member)) },
            onClick = {
                sheetState.hide(coroutineScope) {
                    eventSink(RolesAndPermissionsEvents.DemoteSelfTo(RoomMember.Role.USER))
                }
            },
            style = ListItemStyle.Destructive,
        )
        ListItem(
            headlineContent = { Text(stringResource(CommonStrings.action_cancel)) },
            onClick = ::dismiss,
            style = ListItemStyle.Primary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RolesAndPermissionsViewPreview(@PreviewParameter(RolesAndPermissionsStateProvider::class) state: RolesAndPermissionsState) {
    ElementPreview {
        RolesAndPermissionsView(
            state = state,
            rolesAndPermissionsNavigator = object : RolesAndPermissionsNavigator {},
        )
    }
}
