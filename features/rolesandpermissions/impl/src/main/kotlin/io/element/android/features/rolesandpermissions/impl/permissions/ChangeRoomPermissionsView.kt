/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.permissions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.features.rolesandpermissions.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.SaveChangesDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceDropdown
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeRoomPermissionsView(
    state: ChangeRoomPermissionsState,
    onComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        state.eventSink(ChangeRoomPermissionsEvent.Exit)
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_room_roles_and_permissions_permissions_header),
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
            state.itemsBySection.onEachIndexed { index, (section, items) ->
                item {
                    ListSectionHeader(titleForSection(section), hasDivider = index > 0)
                }
                for (permissionType in items) {
                    item {
                        PreferenceDropdown(
                            title = titleForType(permissionType),
                            selectedOption = state.selectedRoleForType(permissionType),
                            options = SelectableRole.entries.toImmutableList(),
                            onSelectOption = { role ->
                                state.eventSink(
                                    ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(
                                        action = permissionType,
                                        role = role
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    AsyncActionView(
        async = state.saveAction,
        onSuccess = { onComplete(it) },
        confirmationDialog = { confirming ->
            when (confirming) {
                is AsyncAction.ConfirmingCancellation -> {
                    SaveChangesDialog(
                        onSaveClick = { state.eventSink(ChangeRoomPermissionsEvent.Save) },
                        onDiscardClick = { state.eventSink(ChangeRoomPermissionsEvent.Exit) },
                        onDismiss = { state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions) },
                    )
                }
            }
        },
        onErrorDismiss = { state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions) }
    )
}

@Composable
private fun titleForSection(section: RoomPermissionsSection): String = when (section) {
    RoomPermissionsSection.SpaceDetails -> stringResource(R.string.screen_room_roles_and_permissions_space_details)
    RoomPermissionsSection.RoomDetails -> stringResource(R.string.screen_room_roles_and_permissions_room_details)
    RoomPermissionsSection.MessagesAndContent -> stringResource(R.string.screen_room_roles_and_permissions_messages_and_content)
    RoomPermissionsSection.MembershipModeration -> stringResource(R.string.screen_room_roles_and_permissions_member_moderation)
}

@Composable
private fun titleForType(type: RoomPermissionType): String = when (type) {
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
            onComplete = {},
        )
    }
}
