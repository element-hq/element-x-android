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

package io.element.android.features.messages.impl.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.ButtonWithProgress
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.CenterAlignedTopAppBar
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportMessageView(
    state: ReportMessageState,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isSending = state.result is Async.Loading
    when (state.result) {
        is Async.Success -> {
            LaunchedEffect(state.result) {
                onBackClicked()
            }
            return
        }
        is Async.Failure -> {
            ErrorDialog(
                content = stringResource(StringR.string.error_unknown),
                onDismiss = { state.eventSink(ReportMessageEvents.ClearError) }
            )
        }
        else -> Unit
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(StringR.string.action_report_content),
                        style = ElementTextStyles.Regular.callout,
                        fontWeight = FontWeight.Medium,
                    )
                },
                navigationIcon = {
                    BackButton(onClick = onBackClicked)
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

            OutlinedTextField(
                value = state.reason,
                onValueChange = { state.eventSink(ReportMessageEvents.UpdateReason(it)) },
                placeholder = { Text(stringResource(StringR.string.report_content_hint)) },
                enabled = !isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 90.dp)
            )
            Text(
                text = stringResource(StringR.string.report_content_explanation),
                style = ElementTextStyles.Regular.caption1,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(StringR.string.screen_report_content_block_user),
                        style = ElementTextStyles.Regular.callout,
                    )
                    Text(
                        text = stringResource(StringR.string.screen_report_content_block_user_hint),
                        style = ElementTextStyles.Regular.bodyMD,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Switch(
                    enabled = !isSending,
                    checked = state.blockUser,
                    onCheckedChange = { state.eventSink(ReportMessageEvents.ToggleBlockUser) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            ButtonWithProgress(
                text = stringResource(StringR.string.action_send),
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

@Preview
@Composable
fun ReportMessageViewLightPreview(@PreviewParameter(ReportMessageStateProvider::class) state: ReportMessageState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun ReportMessageViewDarkPreview(@PreviewParameter(ReportMessageStateProvider::class) state: ReportMessageState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ReportMessageState) {
    ReportMessageView(
        onBackClicked = {},
        state = state,
    )
}
