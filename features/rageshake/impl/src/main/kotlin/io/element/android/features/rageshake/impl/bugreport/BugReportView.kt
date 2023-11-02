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

package io.element.android.features.rageshake.impl.bugreport

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.element.android.features.rageshake.impl.R
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.components.preferences.PreferenceRow
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.composeutils.annotations.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.debugPlaceholderBackground
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.LogCompositions
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun BugReportView(
    state: BugReportState,
    onDone: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
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

    Box(modifier = modifier) {
        PreferencePage(
            title = stringResource(id = CommonStrings.common_report_a_bug),
            onBackPressed = onBackPressed
        ) {
            val isFormEnabled = state.sending !is Async.Loading
            var descriptionFieldState by textFieldState(
                stateValue = state.formState.description
            )
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceRow {
                OutlinedTextField(
                    value = descriptionFieldState,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormEnabled,
                    label = {
                        Text(text = stringResource(id = R.string.screen_bug_report_editor_placeholder))
                    },
                    supportingText = {
                        Text(text = stringResource(id = R.string.screen_bug_report_editor_description))
                    },
                    onValueChange = {
                        descriptionFieldState = it
                        eventSink(BugReportEvents.SetDescription(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    minLines = 3,
                    // TODO Error text too short
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceSwitch(
                isChecked = state.formState.sendLogs,
                onCheckedChange = { eventSink(BugReportEvents.SetSendLog(it)) },
                enabled = isFormEnabled,
                title = stringResource(id = R.string.screen_bug_report_include_logs),
                subtitle = stringResource(id = R.string.screen_bug_report_logs_description),
            )
            PreferenceSwitch(
                isChecked = state.formState.canContact,
                onCheckedChange = { eventSink(BugReportEvents.SetCanContact(it)) },
                enabled = isFormEnabled,
                title = stringResource(id = R.string.screen_bug_report_contact_me_title),
                subtitle = stringResource(id = R.string.screen_bug_report_contact_me),
            )
            if (state.screenshotUri != null) {
                PreferenceSwitch(
                    isChecked = state.formState.sendScreenshot,
                    onCheckedChange = { eventSink(BugReportEvents.SetSendScreenshot(it)) },
                    enabled = isFormEnabled,
                    title = stringResource(id = R.string.screen_bug_report_include_screenshot)
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
                            contentDescription = null,
                            placeholder = debugPlaceholderBackground(),
                        )
                    }
                }
            }
            // Submit
            PreferenceRow {
                Button(
                    text = stringResource(id = CommonStrings.action_send),
                    onClick = { eventSink(BugReportEvents.SendBugReport) },
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp)
                )
            }
        }

        when (state.sending) {
            is Async.Loading -> {
                // Indeterminate indicator, to avoid the freeze effect if the connection takes time to initialize.
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is Async.Failure -> ErrorDialog(
                content = state.sending.error.toString(),
                onDismiss = { state.eventSink(BugReportEvents.ClearError) }
            )
            else -> Unit
        }
    }
}

@PreviewsDayNight
@Composable
internal fun BugReportViewPreview(@PreviewParameter(BugReportStateProvider::class) state: BugReportState) = ElementPreview {
    BugReportView(
        state = state,
        onDone = {},
        onBackPressed = {},
    )
}
