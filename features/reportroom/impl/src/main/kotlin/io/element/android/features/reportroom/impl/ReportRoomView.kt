/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

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
fun ReportRoomView(
    state: ReportRoomState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val isReporting = state.reportAction is AsyncAction.Loading
    AsyncActionView(
        async = state.reportAction,
        onSuccess = { onBackClick() },
        errorTitle = { failure ->
            when (failure) {
                is ReportRoom.Exception.LeftRoomFailed -> stringResource(R.string.screen_report_room_leave_failed_alert_title)
                else -> stringResource(CommonStrings.dialog_title_error)
            }
        },
        errorMessage = { failure ->
            when (failure) {
                is ReportRoom.Exception.LeftRoomFailed -> stringResource(R.string.screen_report_room_leave_failed_alert_message)
                else -> stringResource(CommonStrings.error_unknown)
            }
        },
        onRetry = {
            state.eventSink(ReportRoomEvents.Report)
        },
        onErrorDismiss = { state.eventSink(ReportRoomEvents.ClearReportAction) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_report_room_title),
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
            TextField(
                value = state.reason,
                onValueChange = { state.eventSink(ReportRoomEvents.UpdateReason(it)) },
                placeholder = stringResource(R.string.screen_report_room_reason_placeholder),
                minLines = 3,
                enabled = !isReporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 90.dp),
                supportingText = stringResource(R.string.screen_report_room_reason_footer),
            )

            Spacer(modifier = Modifier.height(24.dp))

            ListItem(
                modifier = Modifier.padding(end = 8.dp),
                headlineContent = {
                    Text(text = stringResource(CommonStrings.action_leave_room))
                },
                onClick = {
                    state.eventSink(ReportRoomEvents.ToggleLeaveRoom)
                },
                trailingContent = ListItemContent.Switch(checked = state.leaveRoom)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                text = stringResource(CommonStrings.action_report),
                enabled = state.canReport && !isReporting,
                destructive = true,
                showProgress = isReporting,
                onClick = {
                    focusManager.clearFocus(force = true)
                    state.eventSink(ReportRoomEvents.Report)
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
internal fun ReportRoomViewPreview(
    @PreviewParameter(ReportRoomStateProvider::class) state: ReportRoomState
) = ElementPreview {
    ReportRoomView(
        state = state,
        onBackClick = {},
    )
}
