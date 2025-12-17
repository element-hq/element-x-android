/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.invite

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.invitepeople.api.InvitePeopleEvents
import io.element.android.features.invitepeople.api.InvitePeopleState
import io.element.android.features.invitepeople.api.InvitePeopleStateProvider
import io.element.android.features.roomdetails.impl.R
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomInviteMembersView(
    state: InvitePeopleState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    invitePeopleView: @Composable () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            RoomInviteMembersTopBar(
                onBackClick = {
                    if (state.isSearchActive) {
                        state.eventSink(InvitePeopleEvents.CloseSearch)
                    } else {
                        onBackClick()
                    }
                },
                onSubmitClick = {
                    state.eventSink(InvitePeopleEvents.SendInvites)
                },
                canSend = state.canInvite,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            invitePeopleView()
        }
    }

    if (state.sendInvitesAction.isLoading()) {
        InviteProgressDialog()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomInviteMembersTopBar(
    canSend: Boolean,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
) {
    TopAppBar(
        titleStr = stringResource(R.string.screen_room_details_invite_people_title),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_invite),
                onClick = onSubmitClick,
                enabled = canSend,
            )
        }
    )
}

@Composable
private fun InviteProgressDialog() {
    ProgressDialog {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.screen_room_details_invite_people_preparing),
            color = ElementTheme.colors.textPrimary,
            style = ElementTheme.typography.fontHeadingSmMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.screen_room_details_invite_people_dont_close),
            color = ElementTheme.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomInviteMembersViewPreview(@PreviewParameter(InvitePeopleStateProvider::class) state: InvitePeopleState) = ElementPreview {
    RoomInviteMembersView(
        state = state,
        invitePeopleView = {},
        onBackClick = {},
    )
}
