/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.invite.impl.declineandblock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.invite.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeclineAndBlockView(
    state: DeclineAndBlockState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val isDeclining = state.declineAction is AsyncAction.Loading
    AsyncActionView(
        async = state.declineAction,
        onSuccess = { onBackClick() },
        errorMessage = { stringResource(CommonStrings.error_unknown) },
        onRetry = { state.eventSink(DeclineAndBlockEvents.Decline) },
        onErrorDismiss = { state.eventSink(DeclineAndBlockEvents.ClearDeclineAction) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_decline_and_block_title),
                navigationIcon = {
                    BackButton(onClick = onBackClick)
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            ListItem(
                modifier = Modifier.padding(end = 8.dp),
                headlineContent = {
                    Text(text = stringResource(R.string.screen_decline_and_block_block_user_option_title))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.screen_decline_and_block_block_user_option_description))
                },
                onClick = {
                    state.eventSink(DeclineAndBlockEvents.ToggleBlockUser)
                },
                trailingContent = ListItemContent.Switch(checked = state.blockUser)
            )

            Spacer(modifier = Modifier.height(24.dp))
            ListItem(
                modifier = Modifier.padding(end = 8.dp),
                headlineContent = {
                    Text(text = stringResource(CommonStrings.action_report_room))
                },
                supportingContent = {
                    Text(text = stringResource(R.string.screen_decline_and_block_report_user_option_description))
                },
                onClick = {
                    state.eventSink(DeclineAndBlockEvents.ToggleReportRoom)
                },
                trailingContent = ListItemContent.Switch(checked = state.reportRoom)
            )

            if (state.reportRoom) {
                Spacer(modifier = Modifier.height(24.dp))
                TextField(
                    value = state.reportReason,
                    onValueChange = { state.eventSink(DeclineAndBlockEvents.UpdateReportReason(it)) },
                    placeholder = stringResource(R.string.screen_decline_and_block_report_user_reason_placeholder),
                    minLines = 3,
                    enabled = !isDeclining,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 90.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                text = stringResource(CommonStrings.action_decline),
                destructive = true,
                showProgress = isDeclining,
                enabled = !isDeclining && state.canDecline,
                onClick = {
                    focusManager.clearFocus(force = true)
                    state.eventSink(DeclineAndBlockEvents.Decline)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun DeclineAndBlockViewPreview(
    @PreviewParameter(DeclineAndBlockStateProvider::class) state: DeclineAndBlockState
) = ElementPreview {
    DeclineAndBlockView(
        state = state,
        onBackClick = {},
    )
}
