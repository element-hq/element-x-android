@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.login

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.designsystem.ElementXTheme
import io.element.android.x.features.login.error.loginError
import io.element.android.x.matrix.core.SessionId
import timber.log.Timber

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = mavericksViewModel(),
    onChangeServer: () -> Unit = { },
    onLoginWithSuccess: (SessionId) -> Unit = { },
) {
    val state: LoginViewState by viewModel.collectAsState()
    val formState: LoginFormState by viewModel.formState
    LaunchedEffect(key1 = Unit) {
        Timber.d("resume")
        viewModel.onResume()
    }
    LoginContent(
        state = state,
        formState = formState,
        onChangeServer = onChangeServer,
        onLoginChanged = viewModel::onSetName,
        onPasswordChanged = viewModel::onSetPassword,
        onSubmitClicked = viewModel::onSubmit,
        onLoginWithSuccess = onLoginWithSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContent(
    state: LoginViewState,
    formState: LoginFormState,
    modifier: Modifier = Modifier,
    onChangeServer: () -> Unit = {},
    onLoginChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onSubmitClicked: () -> Unit = {},
    onLoginWithSuccess: (SessionId) -> Unit = {},
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
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
                val isError = state.loggedInSessionId is Fail
                // Title
                Text(
                    text = "Welcome back",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 48.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
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
                                .padding(top = 8.dp, end = 8.dp),
                            content = {
                                Text(text = "Change")
                            }
                        )
                    }
                    OutlinedTextField(
                        value = formState.login,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        label = {
                            Text(text = "Email or username")
                        },
                        onValueChange = onLoginChanged,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                    )
                    var passwordVisible by remember { mutableStateOf(false) }
                    if (state.loggedInSessionId is Loading) {
                        // Ensure password is hidden when user submits the form
                        passwordVisible = false
                    }
                    OutlinedTextField(
                        value = formState.password,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        onValueChange = onPasswordChanged,
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
                            onDone = { onSubmitClicked() }
                        ),
                    )
                    if (state.loggedInSessionId is Fail) {
                        Text(
                            text = loginError(state.formState, state.loggedInSessionId.error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                // Submit
                Button(
                    onClick = onSubmitClicked,
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                ) {
                    Text(text = "Continue")
                }
                when (val loggedInSessionId = state.loggedInSessionId) {
                    is Success -> onLoginWithSuccess(loggedInSessionId())
                    else -> Unit
                }
            }
            if (state.loggedInSessionId is Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
@Preview
fun LoginContentPreview() {
    ElementXTheme(darkTheme = false) {
        LoginContent(
            state = LoginViewState(
                homeserver = "matrix.org",
            ),
            formState = LoginFormState("", "")
        )
    }
}
