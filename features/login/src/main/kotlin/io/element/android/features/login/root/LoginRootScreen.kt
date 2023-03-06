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

package io.element.android.features.login.root

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.ElementTextStyles
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.BackButton
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.theme.components.onTabOrEnterKeyFocusNext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.R as StringR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRootScreen(
    state: LoginRootState,
    modifier: Modifier = Modifier,
    onChangeServer: () -> Unit = {},
    onLoginWithSuccess: (SessionId) -> Unit = {},
    onBackPressed: () -> Unit,
) {
    val interactionEnabled by remember(state.loggedInState) {
        derivedStateOf {
            state.loggedInState != LoggedInState.LoggingIn
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { BackButton(action = onBackPressed, enabled = interactionEnabled) },
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalScroll(state = scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(16.dp))
                // Title
                Text(
                    text = stringResource(id = StringR.string.ftue_auth_welcome_back_title),
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = ElementTextStyles.Bold.title1,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(32.dp))

                ChangeServerSection(
                    interactionEnabled = interactionEnabled,
                    homeserver = state.homeserverDetails.url,
                    onChangeServer = onChangeServer
                )

                Spacer(Modifier.height(32.dp))

                LoginForm(state = state, interactionEnabled = interactionEnabled)

                Spacer(modifier = Modifier.height(32.dp))

            }
            when (val loggedInState = state.loggedInState) {
                is LoggedInState.LoggedIn -> onLoginWithSuccess(loggedInState.sessionId)
                else -> Unit
            }

            if (state.loggedInState is LoggedInState.LoggingIn) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
internal fun ChangeServerSection(
    interactionEnabled: Boolean,
    homeserver: String,
    onChangeServer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            text = stringResource(id = StringR.string.ftue_auth_sign_in_choose_server_header),
            style = ElementTextStyles.Regular.formHeader,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag(TestTags.loginChangeServer)
                .clickable {
                    if (interactionEnabled) {
                        onChangeServer()
                    }
                },
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = homeserver,
                style = ElementTextStyles.Bold.body,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = {
                    if (interactionEnabled) {
                        onChangeServer()
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
internal fun LoginForm(
    state: LoginRootState,
    interactionEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var loginFieldState by textFieldState(stateValue = state.formState.login)
    var passwordFieldState by textFieldState(stateValue = state.formState.password)

    val focusManager = LocalFocusManager.current
    val eventSink = state.eventSink
    Column(modifier) {
        Text(
            text = stringResource(StringR.string.login_form_title),
            modifier = Modifier.padding(start = 16.dp),
            style = ElementTextStyles.Regular.formHeader
        )

        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = loginFieldState,
            readOnly = !interactionEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.loginEmailUsername)
                .onTabOrEnterKeyFocusNext(focusManager),
            label = {
                Text(text = stringResource(StringR.string.ex_login_username_hint))
            },
            onValueChange = {
                loginFieldState = it
                eventSink(LoginRootEvents.SetLogin(it))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            singleLine = true,
            maxLines = 1,
            trailingIcon = if (loginFieldState.isNotEmpty()) {
                {
                    IconButton(onClick = {
                        loginFieldState = ""
                    }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(StringR.string.action_clear))
                    }
                }
            } else null,
        )

        var passwordVisible by remember { mutableStateOf(false) }
        if (state.loggedInState is LoggedInState.LoggingIn) {
            // Ensure password is hidden when user submits the form
            passwordVisible = false
        }
        Spacer(Modifier.height(20.dp))
        TextField(
            value = passwordFieldState,
            readOnly = !interactionEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.loginPassword)
                .onTabOrEnterKeyFocusNext(focusManager),
            onValueChange = {
                passwordFieldState = it
                eventSink(LoginRootEvents.SetPassword(it))
            },
            label = {
                Text(text = stringResource(StringR.string.login_signup_password_hint))
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image =
                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description =
                    if (passwordVisible) stringResource(StringR.string.login_hide_password) else stringResource(StringR.string.login_show_password)

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { eventSink(LoginRootEvents.Submit) }
            ),
            singleLine = true,
            maxLines = 1,
        )

        if (state.loggedInState is LoggedInState.ErrorLoggingIn) {
            LoginErrorDialog(error = state.loggedInState.failure, onDismiss = {
                eventSink(LoginRootEvents.ClearError)
            })
        }
        Spacer(Modifier.height(28.dp))

        // Submit
        Button(
            onClick = { eventSink(LoginRootEvents.Submit) },
            enabled = interactionEnabled && state.submitEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.loginContinue)
        ) {
            Text(text = stringResource(StringR.string.login_continue), style = ElementTextStyles.Button)
        }
    }
}

@Composable
internal fun LoginErrorDialog(error: Throwable, onDismiss: () -> Unit) {
    ErrorDialog(
        content = error.localizedMessage ?: stringResource(id = StringR.string.unknown_error),
        onDismiss = onDismiss
    )
}

@Preview
@Composable
internal fun LoginRootScreenLightPreview(@PreviewParameter(LoginRootStateProvider::class) state: LoginRootState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
internal fun LoginRootScreenDarkPreview(@PreviewParameter(LoginRootStateProvider::class) state: LoginRootState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: LoginRootState) {
    LoginRootScreen(
        state = state,
        onBackPressed = {}
    )
}
