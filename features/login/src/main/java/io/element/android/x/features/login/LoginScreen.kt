@file:OptIn(ExperimentalMaterial3Api::class)

package io.element.android.x.features.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.designsystem.components.VectorButton

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = mavericksViewModel(),
    onLoginWithSuccess: () -> Unit = { }
) {
    val state: LoginViewState by viewModel.collectAsState()
    LoginContent(
        state = state,
        onHomeserverChanged = { viewModel.handle(LoginActions.SetHomeserver(it)) },
        onLoginChanged = { viewModel.handle(LoginActions.SetLogin(it)) },
        onPasswordChanged = { viewModel.handle(LoginActions.SetPassword(it)) },
        onSubmitClicked = { viewModel.handle(LoginActions.Submit) },
        onLoginWithSuccess = onLoginWithSuccess
    )
}


@Composable
fun LoginContent(
    state: LoginViewState,
    onHomeserverChanged: (String) -> Unit,
    onLoginChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmitClicked: () -> Unit,
    onLoginWithSuccess: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                val isError = state.isLoggedIn is Fail
                Image(
                    painterResource(id = R.drawable.element_logo_green),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(40.dp)
                )
                OutlinedTextField(
                    value = state.homeserver,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = {
                        onHomeserverChanged(it)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                    ),
                )
                OutlinedTextField(
                    value = state.login,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    onValueChange = {
                        onLoginChanged(it)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                    ),
                )
                OutlinedTextField(
                    value = state.password,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    onValueChange = {
                        onPasswordChanged(it)
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
                VectorButton(
                    text = "Submit",
                    onClick = {
                        onSubmitClicked()
                    },
                    enabled = state.submitEnabled,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 16.dp)
                )
                if (state.isLoggedIn is Loading) {
                    // FIXME This does not work, we never enter this if block
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                if (state.isLoggedIn is Success) {
                    onLoginWithSuccess()
                }
            }
        }
    }
}