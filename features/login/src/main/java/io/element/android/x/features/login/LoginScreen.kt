@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import timber.log.Timber

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = mavericksViewModel(),
    onChangeServer: () -> Unit = { },
    onLoginWithSuccess: () -> Unit = { },
) {
    val state: LoginViewState by viewModel.collectAsState()
    LaunchedEffect(key1 = Unit) {
        Timber.d("resume")
        viewModel.onResume()
    }
    LoginContent(
        state = state,
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
                                .padding(top = 8.dp, end = 8.dp),
                            content = {
                                Text(text = "Change")
                            }
                        )
                    }
                    OutlinedTextField(
                        value = state.login,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        label = {
                            Text(text = "Email or username")
                        },
                        onValueChange = onLoginChanged,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                        ),
                    )
                    OutlinedTextField(
                        value = state.password,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        onValueChange = onPasswordChanged,
                        label = {
                            Text(text = "Password")
                        },
                        isError = isError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Send,
                        ),
                    )
                    if (isError) {
                        Text(
                            text = (state.isLoggedIn as? Fail)?.toString().orEmpty(),
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
private fun LoginContentPreview() {
    ElementXTheme(darkTheme = false) {
        LoginContent(
            state = LoginViewState(
                homeserver = "matrix.org",
            ),
        )
    }
}
