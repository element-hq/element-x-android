/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.addpeople

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.features.invitepeople.api.InvitePeopleEvents
import io.element.android.features.invitepeople.api.InvitePeopleState
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AddPeopleView(
    state: InvitePeopleState,
    invitePeopleView: @Composable () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        topBar = {
            AddPeopleTopBar(onSkipClick = onFinish)
        },
        footer = {
            Button(
                text = "Finish",
                onClick = { state.eventSink(InvitePeopleEvents.SendInvites) },
                enabled = state.canInvite,
                modifier = Modifier.padding(bottom = 16.dp)
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
        titleStr = "Invite people",
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_skip),
                onClick = onSkipClick,
            )
        }
    )
}
