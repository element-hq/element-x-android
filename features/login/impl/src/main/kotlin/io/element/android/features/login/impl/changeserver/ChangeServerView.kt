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

package io.element.android.features.login.impl.changeserver

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.login.impl.R
import io.element.android.features.login.impl.error.changeServerError
import io.element.android.features.login.impl.util.LoginConstants
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.LinkColor
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.ButtonWithProgress
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.LocalColors
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.theme.components.onTabOrEnterKeyFocusNext
import io.element.android.libraries.matrix.api.auth.AuthenticationException
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun ChangeServerView(
    state: ChangeServerState,
    onLearnMoreClicked: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onChangeServerSuccess: () -> Unit = {},
) {
    val eventSink = state.eventSink
    val scrollState = rememberScrollState()
    val isLoading by remember(state.changeServerAction) {
        derivedStateOf {
            state.changeServerAction is Async.Loading
        }
    }
    val focusManager = LocalFocusManager.current

    fun submit() {
        // Clear focus to prevent keyboard issues with textfields
        focusManager.clearFocus(force = true)

        eventSink(ChangeServerEvents.Submit)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(onClick = onBackPressed, enabled = isLoading) }
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(
                        state = scrollState,
                    )
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(42.dp))
                Box(
                    modifier = Modifier
                        .size(width = 70.dp, height = 70.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            color = LocalColors.current.quinary,
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(width = 32.dp, height = 32.dp),
                        tint = MaterialTheme.colorScheme.secondary,
                        resourceId = R.drawable.ic_homeserver,
                        contentDescription = "",
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = StringR.string.ftue_auth_choose_server_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    style = ElementTextStyles.Bold.title2,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(id = StringR.string.ex_choose_server_subtitle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center,
                    style = ElementTextStyles.Regular.subheadline,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(StringR.string.hs_url),
                    style = ElementTextStyles.Regular.formHeader,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                var homeserverFieldState by textFieldState(stateValue = state.homeserver)
                TextField(
                    value = homeserverFieldState,
                    readOnly = isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.changeServerServer)
                        .onTabOrEnterKeyFocusNext(focusManager),
                    onValueChange = {
                        homeserverFieldState = it
                        eventSink(ChangeServerEvents.SetServer(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit() }
                    ),
                    singleLine = true,
                    maxLines = 1,
                    trailingIcon = if (homeserverFieldState.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                homeserverFieldState = ""
                            }, enabled = isLoading) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(StringR.string.a11y_clear))
                            }
                        }
                    } else null,
                )
                if (state.changeServerAction is Async.Failure) {
                    if (state.changeServerAction.error is AuthenticationException.SlidingSyncNotAvailable) {
                        SlidingSyncNotSupportedDialog(onLearnMoreClicked = {
                            onLearnMoreClicked()
                            eventSink(ChangeServerEvents.ClearError)
                        }, onDismiss = {
                            eventSink(ChangeServerEvents.ClearError)
                        })
                    } else {
                        ChangeServerErrorDialog(
                            error = state.changeServerAction.error,
                            onDismiss = {
                                eventSink(ChangeServerEvents.ClearError)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                val footerMessage = stringResource(StringR.string.server_selection_server_footer)
                val footerAction = stringResource(StringR.string.server_selection_server_footer_action)
                val footerText = buildAnnotatedString {
                    val defaultColor = MaterialTheme.colorScheme.tertiary
                    withStyle(ParagraphStyle(textAlign = TextAlign.Start)) {
                        withStyle(SpanStyle(color = defaultColor)) {
                            append(footerMessage)
                            append(" ")
                        }
                        val start = length
                        withStyle(SpanStyle(color = LinkColor)) {
                            append(footerAction)
                        }
                        addUrlAnnotation(UrlAnnotation(LoginConstants.SLIDING_SYNC_READ_MORE_URL), start, length)
                    }
                }
                ClickableLinkText(
                    text = footerText,
                    interactionSource = MutableInteractionSource(),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = ElementTextStyles.Regular.caption1,
                )
                Spacer(Modifier.height(32.dp))
                ButtonWithProgress(
                    text = stringResource(id = StringR.string.login_continue),
                    showProgress = isLoading,
                    onClick = ::submit,
                    enabled = state.submitEnabled || isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.changeServerContinue)
                )
                if (state.changeServerAction is Async.Success) {
                    onChangeServerSuccess()
                }
            }
        }
    }
}

@Composable
internal fun ChangeServerErrorDialog(error: Throwable, onDismiss: () -> Unit) {
    ErrorDialog(
        content = stringResource(changeServerError(error)),
        onDismiss = onDismiss
    )
}

@Composable
internal fun SlidingSyncNotSupportedDialog(onLearnMoreClicked: () -> Unit, onDismiss: () -> Unit) {
    ConfirmationDialog(
        onDismiss = onDismiss,
        submitText = stringResource(StringR.string.action_learn_more),
        onSubmitClicked = onLearnMoreClicked,
        onCancelClicked = onDismiss,
        emphasizeSubmitButton = true,
        title = stringResource(StringR.string.server_selection_sliding_sync_alert_title),
        content = stringResource(StringR.string.server_selection_sliding_sync_alert_message),
    )
}

@Preview
@Composable
internal fun ChangeServerViewLightPreview(@PreviewParameter(ChangeServerStateProvider::class) state: ChangeServerState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun ChangeServerViewDarkPreview(@PreviewParameter(ChangeServerStateProvider::class) state: ChangeServerState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ChangeServerState) {
    ChangeServerView(state = state, onBackPressed = {}, onLearnMoreClicked = {})
}
