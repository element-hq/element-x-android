/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.startchat.impl.root

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.startchat.api.ConfirmingStartDmWithMatrixUser
import io.element.android.features.startchat.impl.R
import io.element.android.features.startchat.impl.components.UserListView
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ListSectionHeader
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.components.CreateDmConfirmationBottomSheet
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.persistentListOf

@Composable
fun StartChatView(
    state: StartChatState,
    onCloseClick: () -> Unit,
    onNewRoomClick: () -> Unit,
    onOpenDM: (RoomId) -> Unit,
    onInviteFriendsClick: () -> Unit,
    onJoinByAddressClick: () -> Unit,
    onRoomDirectorySearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxWidth(),
        topBar = {
            if (!state.userListState.isSearchActive) {
                CreateRoomRootViewTopBar(onCloseClick = onCloseClick)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UserListView(
                modifier = Modifier.fillMaxWidth(),
                // Do not render suggestions in this case, the suggestion will be rendered
                // by CreateRoomActionButtonsList
                state = state.userListState.copy(
                    recentDirectRooms = persistentListOf(),
                ),
                onSelectUser = {
                    state.eventSink(StartChatEvents.StartDM(it))
                },
                onDeselectUser = { },
            )

            if (!state.userListState.isSearchActive) {
                CreateRoomActionButtonsList(
                    state = state,
                    onNewRoomClick = onNewRoomClick,
                    onInvitePeopleClick = onInviteFriendsClick,
                    onJoinByAddressClick = onJoinByAddressClick,
                    onRoomDirectorySearchClick = onRoomDirectorySearchClick,
                    onDmClick = onOpenDM,
                )
            }
        }
    }

    AsyncActionView(
        async = state.startDmAction,
        progressDialog = {
            AsyncActionViewDefaults.ProgressDialog(
                progressText = stringResource(CommonStrings.common_starting_chat),
            )
        },
        onSuccess = { onOpenDM(it) },
        errorMessage = { stringResource(R.string.screen_start_chat_error_starting_chat) },
        onRetry = {
            state.userListState.selectedUsers.firstOrNull()
                ?.let { state.eventSink(StartChatEvents.StartDM(it)) }
            // Cancel start DM if there is no more selected user (should not happen)
                ?: state.eventSink(StartChatEvents.CancelStartDM)
        },
        onErrorDismiss = { state.eventSink(StartChatEvents.CancelStartDM) },
        confirmationDialog = { data ->
            if (data is ConfirmingStartDmWithMatrixUser) {
                CreateDmConfirmationBottomSheet(
                    matrixUser = data.matrixUser,
                    onSendInvite = {
                        state.eventSink(StartChatEvents.StartDM(data.matrixUser))
                    },
                    onDismiss = {
                        state.eventSink(StartChatEvents.CancelStartDM)
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRoomRootViewTopBar(
    onCloseClick: () -> Unit,
) {
    TopAppBar(
        titleStr = stringResource(id = CommonStrings.action_start_chat),
        navigationIcon = {
            BackButton(
                imageVector = CompoundIcons.Close(),
                onClick = onCloseClick,
            )
        }
    )
}

@Composable
private fun CreateRoomActionButtonsList(
    state: StartChatState,
    onNewRoomClick: () -> Unit,
    onInvitePeopleClick: () -> Unit,
    onJoinByAddressClick: () -> Unit,
    onRoomDirectorySearchClick: () -> Unit,
    onDmClick: (RoomId) -> Unit,
) {
    LazyColumn {
        item {
            CreateRoomActionButton(
                iconRes = CompoundDrawables.ic_compound_plus,
                text = stringResource(id = R.string.screen_create_room_action_create_room),
                onClick = onNewRoomClick,
            )
        }
        if (state.isRoomDirectorySearchEnabled) {
            item {
                CreateRoomActionButton(
                    iconRes = CompoundDrawables.ic_compound_list_bulleted,
                    text = stringResource(id = R.string.screen_room_directory_search_title),
                    onClick = onRoomDirectorySearchClick,
                )
            }
        }
        item {
            CreateRoomActionButton(
                iconRes = CompoundDrawables.ic_compound_share_android,
                text = stringResource(id = CommonStrings.action_invite_friends_to_app, state.applicationName),
                onClick = onInvitePeopleClick,
            )
        }
        item {
            CreateRoomActionButton(
                iconRes = CompoundDrawables.ic_compound_room,
                text = stringResource(R.string.screen_start_chat_join_room_by_address_action),
                onClick = onJoinByAddressClick,
            )
        }
        if (state.userListState.recentDirectRooms.isNotEmpty()) {
            item {
                ListSectionHeader(
                    title = stringResource(id = CommonStrings.common_suggestions),
                    hasDivider = false,
                )
            }
            state.userListState.recentDirectRooms.forEach { recentDirectRoom ->
                item {
                    MatrixUserRow(
                        modifier = Modifier.clickable(
                            onClick = {
                                onDmClick(recentDirectRoom.roomId)
                            }
                        ),
                        matrixUser = recentDirectRoom.matrixUser,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateRoomActionButton(
    @DrawableRes iconRes: Int,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            tint = ElementTheme.colors.iconSecondary,
            resourceId = iconRes,
            contentDescription = null,
        )
        Text(
            text = text,
            style = ElementTheme.typography.fontBodyLgRegular,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun StartChatViewPreview(@PreviewParameter(StartChatStateProvider::class) state: StartChatState) =
    ElementPreview {
        StartChatView(
            state = state,
            onCloseClick = {},
            onNewRoomClick = {},
            onOpenDM = {},
            onJoinByAddressClick = {},
            onInviteFriendsClick = {},
            onRoomDirectorySearchClick = {},
        )
    }
