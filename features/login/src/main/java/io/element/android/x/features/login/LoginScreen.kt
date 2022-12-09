@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import timber.log.Timber

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = mavericksViewModel(),
    onChangeServer: () -> Unit = { },
    onLoginWithSuccess: () -> Unit = { },
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginContent(
    state: LoginViewState,
    formState: LoginFormState,
    onChangeServer: () -> Unit = {},
    onLoginChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onSubmitClicked: () -> Unit = {},
    onLoginWithSuccess: () -> Unit = {},
) {
    Surface(color = MaterialTheme.colorScheme.background) {
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
                val isError = state.isLoggedIn is Fail
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
                    //modifier = Modifier.weight(1f),
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
                                .padding(top = 8.dp, end = 8.dp)
                                .semantics { testTag = "login-change_server"; testTagsAsResourceId = true }
                            ,
                            content = {
                                Text(text = "Change")
                            }
                        )
                    }
                    OutlinedTextField(
                        value = formState.login,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp)
                            .semantics { testTag = "login-email_username"; testTagsAsResourceId = true }
                        ,
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
                    if (state.isLoggedIn is Loading) {
                        // Ensure password is hidden when user submits the form
                        passwordVisible = false
                    }
                    OutlinedTextField(
                        value = formState.password,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .semantics { testTag = "login-password"; testTagsAsResourceId = true }
                        ,
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
                    if (state.isLoggedIn is Fail) {
                        Text(
                            text = loginError(state.formState, state.isLoggedIn.error),
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
                        .semantics { testTag = "login-continue"; testTagsAsResourceId = true }

                ) {
                    Text(text = "Continue")
                }
                if (state.isLoggedIn is Success) {
                    onLoginWithSuccess()
                }
            }
            if (state.isLoggedIn is Loading) {
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
