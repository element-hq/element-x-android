/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportMessageView(
    state: ReportMessageState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isSending = state.result is AsyncAction.Loading
    AsyncActionView(
        async = state.result,
        progressDialog = {},
        onSuccess = { onBackClick() },
        errorMessage = { stringResource(CommonStrings.error_unknown) },
        onErrorDismiss = { state.eventSink(ReportMessageEvents.ClearError) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                titleStr = stringResource(CommonStrings.action_report_content),
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
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = state.reason,
                onValueChange = { state.eventSink(ReportMessageEvents.UpdateReason(it)) },
                placeholder = stringResource(R.string.screen_report_content_hint),
                minLines = 3,
                enabled = !isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp),
                supportingText = stringResource(R.string.screen_report_content_explanation),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.screen_report_content_block_user),
                        style = ElementTheme.typography.fontBodyLgRegular,
                    )
                    Text(
                        text = stringResource(R.string.screen_report_content_block_user_hint),
                        style = ElementTheme.typography.fontBodyMdRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
                Switch(
                    enabled = !isSending,
                    checked = state.blockUser,
                    onCheckedChange = { state.eventSink(ReportMessageEvents.ToggleBlockUser) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                text = stringResource(CommonStrings.action_send),
                enabled = state.reason.isNotBlank() && !isSending,
                showProgress = isSending,
                onClick = {
                    focusManager.clearFocus(force = true)
                    state.eventSink(ReportMessageEvents.Report)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ReportMessageViewPreview(@PreviewParameter(ReportMessageStateProvider::class) state: ReportMessageState) = ElementPreview {
    ReportMessageView(
        onBackClick = {},
        state = state,
    )
}
