/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.addpeople

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.createroom.impl.R
import io.element.android.features.invitepeople.api.InvitePeopleEvents
import io.element.android.features.invitepeople.api.InvitePeopleState
import io.element.android.features.invitepeople.api.InvitePeopleStateProvider
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AddPeopleView(
    state: InvitePeopleState,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    invitePeopleView: @Composable () -> Unit,
) {
    HeaderFooterPage(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        topBar = {
            AddPeopleTopBar(onSkipClick = onFinish)
        },
        footer = {
            Button(
                text = stringResource(CommonStrings.action_finish),
                onClick = {
                    state.eventSink(InvitePeopleEvents.SendInvites)
                    onFinish()
                },
                enabled = state.canInvite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        },
        content = invitePeopleView
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPeopleTopBar(
    onSkipClick: () -> Unit,
) {
    TopAppBar(
        titleStr = stringResource(R.string.screen_create_room_add_people_title),
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_skip),
                onClick = onSkipClick,
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun AddPeopleViewPreview(@PreviewParameter(InvitePeopleStateProvider::class) state: InvitePeopleState) = ElementPreview {
    AddPeopleView(
        state = state,
        invitePeopleView = {},
        onFinish = {},
    )
}
