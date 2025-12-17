/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.element.android.features.rageshake.impl.R
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.components.async.AsyncActionView
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.components.preferences.PreferenceDivider
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceRow
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.modifiers.onTabOrEnterKeyFocusNext
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TextFieldValidity
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun BugReportView(
    state: BugReportState,
    onViewLogs: () -> Unit,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eventSink = state.eventSink

    Box(modifier = modifier) {
        PreferencePage(
            title = stringResource(id = CommonStrings.common_report_a_problem),
            onBackClick = onBackClick
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            val isFormEnabled = state.sending !is AsyncAction.Loading
            var descriptionFieldState by textFieldState(
                stateValue = state.formState.description
            )
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceRow {
                TextField(
                    value = descriptionFieldState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onTabOrEnterKeyFocusNext(LocalFocusManager.current),
                    enabled = isFormEnabled,
                    placeholder = stringResource(id = R.string.screen_bug_report_editor_placeholder),
                    supportingText = stringResource(id = R.string.screen_bug_report_editor_description),
                    onValueChange = {
                        descriptionFieldState = it
                        eventSink(BugReportEvents.SetDescription(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        keyboardController?.hide()
                    }),
                    minLines = 3,
                    validity = if (state.isDescriptionInError) TextFieldValidity.Invalid else TextFieldValidity.None,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            PreferenceDivider()
            ListItem(
                headlineContent = {
                    Text(stringResource(id = R.string.screen_bug_report_view_logs))
                },
                enabled = isFormEnabled,
                onClick = onViewLogs,
            )
            PreferenceDivider()
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
                        )
                    }
                }
            }
            PreferenceSwitch(
                isChecked = state.formState.sendPushRules,
                onCheckedChange = { eventSink(BugReportEvents.SetSendPushRules(it)) },
                enabled = isFormEnabled,
                title = stringResource(R.string.screen_bug_report_send_notification_settings_title),
                subtitle = stringResource(R.string.screen_bug_report_send_notification_settings_description),
            )
            // Submit
            PreferenceRow {
                Button(
                    text = stringResource(id = CommonStrings.action_send),
                    onClick = { eventSink(BugReportEvents.SendBugReport) },
                    enabled = state.submitEnabled,
                    showProgress = state.sending.isLoading(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 16.dp)
                )
            }
        }

        AsyncActionView(
            async = state.sending,
            progressDialog = { },
            onSuccess = {
                eventSink(BugReportEvents.ResetAll)
                onSuccess()
            },
            errorMessage = { error ->
                when (error) {
                    BugReportFormError.DescriptionTooShort -> stringResource(id = R.string.screen_bug_report_error_description_too_short)
                    else -> error.message ?: error.toString()
                }
            },
            onErrorDismiss = { eventSink(BugReportEvents.ClearError) },
        )
    }
}

@Preview(heightDp = 1000)
@Composable
internal fun BugReportViewDayPreview(@PreviewParameter(BugReportStateProvider::class) state: BugReportState) = ElementPreview {
    BugReportView(
        state = state,
        onSuccess = {},
        onBackClick = {},
        onViewLogs = {},
    )
}

@Preview(heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BugReportViewNightPreview(@PreviewParameter(BugReportStateProvider::class) state: BugReportState) = ElementPreview {
    BugReportView(
        state = state,
        onSuccess = {},
        onBackClick = {},
        onViewLogs = {},
    )
}
