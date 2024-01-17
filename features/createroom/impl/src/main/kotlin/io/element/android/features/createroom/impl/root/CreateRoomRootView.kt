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

package io.element.android.features.createroom.impl.root

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.createroom.impl.R
import io.element.android.features.createroom.impl.components.UserListView
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.async.AsyncActionViewDefaults
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun CreateRoomRootView(
    state: CreateRoomRootState,
    onClosePressed: () -> Unit,
    onNewRoomClicked: () -> Unit,
    onOpenDM: (RoomId) -> Unit,
    onInviteFriendsClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxWidth(),
        topBar = {
            if (!state.userListState.isSearchActive) {
                CreateRoomRootViewTopBar(onClosePressed = onClosePressed)
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
                state = state.userListState,
                onUserSelected = {
                    state.eventSink(CreateRoomRootEvents.StartDM(it))
                },
                onUserDeselected = { },
            )

            if (!state.userListState.isSearchActive) {
                CreateRoomActionButtonsList(
                    state = state,
                    onNewRoomClicked = onNewRoomClicked,
                    onInvitePeopleClicked = onInviteFriendsClicked,
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
                ?.let { state.eventSink(CreateRoomRootEvents.StartDM(it)) }
                // Cancel start DM if there is no more selected user (should not happen)
                ?: state.eventSink(CreateRoomRootEvents.CancelStartDM)
        },
        onErrorDismiss = { state.eventSink(CreateRoomRootEvents.CancelStartDM) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRoomRootViewTopBar(
    onClosePressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(id = CommonStrings.action_start_chat),
                style = ElementTheme.typography.aliasScreenTitle,
            )
        },
        navigationIcon = {
            BackButton(
                imageVector = CompoundIcons.Close,
                onClick = onClosePressed,
            )
        }
    )
}

@Composable
private fun CreateRoomActionButtonsList(
    state: CreateRoomRootState,
    onNewRoomClicked: () -> Unit,
    onInvitePeopleClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CreateRoomActionButton(
            iconRes = CompoundDrawables.ic_plus,
            text = stringResource(id = R.string.screen_create_room_action_create_room),
            onClick = onNewRoomClicked,
        )
        CreateRoomActionButton(
            iconRes = CompoundDrawables.ic_share_android,
            text = stringResource(id = CommonStrings.action_invite_friends_to_app, state.applicationName),
            onClick = onInvitePeopleClicked,
        )
    }
}

@Composable
private fun CreateRoomActionButton(
    @DrawableRes iconRes: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.secondary,
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
internal fun CreateRoomRootViewPreview(@PreviewParameter(CreateRoomRootStateProvider::class) state: CreateRoomRootState) =
    ElementPreview {
        CreateRoomRootView(
            state = state,
            onClosePressed = {},
            onNewRoomClicked = {},
            onOpenDM = {},
            onInviteFriendsClicked = {},
        )
    }
