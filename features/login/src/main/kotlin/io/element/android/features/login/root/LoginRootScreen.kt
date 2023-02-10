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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.features.login.error.loginError
import io.element.android.libraries.designsystem.components.form.textFieldState
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.OutlinedTextField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.core.SessionId
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.R as StringR

@Composable
fun LoginRootScreen(
    state: LoginRootState,
    modifier: Modifier = Modifier,
    onChangeServer: () -> Unit = {},
    onLoginWithSuccess: (SessionId) -> Unit = {},
) {
    val eventSink = state.eventSink
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
    ) {
        val scrollState = rememberScrollState()
        var loginFieldState by textFieldState(stateValue = state.formState.login)
        var passwordFieldState by textFieldState(stateValue = state.formState.password)

        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState,
                )
                .padding(horizontal = 16.dp),
        ) {
            val isError = state.loggedInState is LoggedInState.ErrorLoggingIn
            // Title
            Text(
                text = stringResource(id = StringR.string.ftue_auth_welcome_back_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 48.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            // Form
            Column(
                // modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.homeserver,
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = { /* no op */ },
                        enabled = false,
                        label = {
                            Text(text = "Server")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                        ),
                    )
                    Button(
                        onClick = onChangeServer,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .testTag(TestTags.loginChangeServer)
                            .padding(top = 8.dp, end = 8.dp),
                        content = {
                            Text(text = "Change")
                        }
                    )
                }
                OutlinedTextField(
                    value = loginFieldState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.loginEmailUsername)
                        .padding(top = 60.dp),
                    label = {
                        Text(text = stringResource(id = StringR.string.login_signin_username_hint))
                    },
                    onValueChange = {
                        loginFieldState = it
                        eventSink(LoginRootEvents.SetLogin(it))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                )
                var passwordVisible by remember { mutableStateOf(false) }
                if (state.loggedInState is LoggedInState.LoggingIn) {
                    // Ensure password is hidden when user submits the form
                    passwordVisible = false
                }
                OutlinedTextField(
                    value = passwordFieldState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.loginPassword)
                        .padding(top = 24.dp),
                    onValueChange = {
                        passwordFieldState = it
                        eventSink(LoginRootEvents.SetPassword(it))
                    },
                    label = {
                        Text(text = "Password")
                    },
                    isError = isError,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description =
                            if (passwordVisible) "Hide password" else "Show password"

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
                )
                if (state.loggedInState is LoggedInState.ErrorLoggingIn) {
                    Text(
                        text = loginError(state.formState, state.loggedInState.failure),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            // Submit
            Button(
                onClick = { eventSink(LoginRootEvents.Submit) },
                enabled = state.submitEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.loginContinue)
                    .padding(vertical = 32.dp)
            ) {
                Text(text = "Continue")
            }
            when (val loggedInState = state.loggedInState) {
                is LoggedInState.LoggedIn -> onLoginWithSuccess(loggedInState.sessionId)
                else -> Unit
            }
        }
        if (state.loggedInState is LoggedInState.LoggingIn) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Preview
@Composable
fun LoginRootScreenLightPreview() = ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
fun LoginRootScreenDarkPreview() = ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    LoginRootScreen(
        state = aLoginRootState().copy(
            homeserver = "matrix.org",
        ),
    )
}
