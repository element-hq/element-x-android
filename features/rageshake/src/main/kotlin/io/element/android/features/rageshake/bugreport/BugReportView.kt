/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.rageshake.bugreport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.LabelledCheckbox
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun BugReportView(
    state: BugReportState,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = { },
) {
    LogCompositions(tag = "Rageshake", msg = "Root")
    val eventSink = state.eventSink
    if (state.sending is Async.Success) {
        LaunchedEffect(state.sending) {
            eventSink(BugReportEvents.ResetAll)
            onDone()
        }
        return
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState,
                )
                .padding(horizontal = 16.dp),
        ) {
            val isError = state.sending is Async.Failure
            val isFormEnabled = state.sending !is Async.Loading
            // Title
            Text(
                text = stringResource(id = StringR.string.send_bug_report),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            // Form
            Text(
                text = stringResource(id = StringR.string.send_bug_report_description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                )
            var descriptionFieldState by textFieldState(
                stateValue = state.formState.description
            )
            Column(
                // modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    value = descriptionFieldState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = isFormEnabled,
                    label = {
                        Text(text = stringResource(id = StringR.string.send_bug_report_placeholder))
                    },
                    supportingText = {
                        Text(text = stringResource(id = StringR.string.send_bug_report_description_in_english))
                    },
                    onValueChange = {
                        descriptionFieldState = it
                        eventSink(BugReportEvents.SetDescription(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    // TODO Error text too short
                )
            }
            LabelledCheckbox(
                checked = state.formState.sendLogs,
                onCheckedChange = { eventSink(BugReportEvents.SetSendLog(it)) },
                enabled = isFormEnabled,
                text = stringResource(id = StringR.string.send_bug_report_include_logs)
            )
            if (state.hasCrashLogs) {
                LabelledCheckbox(
                    checked = state.formState.sendCrashLogs,
                    onCheckedChange = { eventSink(BugReportEvents.SetSendCrashLog(it)) },
                    enabled = isFormEnabled,
                    text = stringResource(id = StringR.string.send_bug_report_include_crash_logs)
                )
            }
            LabelledCheckbox(
                checked = state.formState.canContact,
                onCheckedChange = { eventSink(BugReportEvents.SetCanContact(it)) },
                enabled = isFormEnabled,
                text = stringResource(id = StringR.string.you_may_contact_me)
            )
            if (state.screenshotUri != null) {
                LabelledCheckbox(
                    checked = state.formState.sendScreenshot,
                    onCheckedChange = { eventSink(BugReportEvents.SetSendScreenshot(it)) },
                    enabled = isFormEnabled,
                    text = stringResource(id = StringR.string.send_bug_report_include_screenshot)
                )
                if (state.formState.sendScreenshot) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        val model = ImageRequest.Builder(context)
                            .data(state.screenshotUri)
                            .build()
                        AsyncImage(
                            modifier = Modifier.fillMaxWidth(fraction = 0.5f),
                            model = model,
                            contentDescription = null
                        )
                    }
                }
            }
            // Submit
            Button(
                onClick = { eventSink(BugReportEvents.SendBugReport) },
                enabled = state.submitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                Text(text = stringResource(id = StringR.string.action_send))
            }
        }
        when (state.sending) {
            is Async.Loading -> {
                CircularProgressIndicator(
                    progress = state.sendingProgress,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is Async.Failure -> ErrorDialog(
                content = state.sending.error.toString(),
            )
            else -> Unit
        }
    }
}

@Preview
@Composable
fun BugReportViewLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun BugReportViewDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    BugReportView(
        state = aBugReportState(),
    )
}
