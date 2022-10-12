package io.element.android.x.ui.screen.login

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import io.element.android.x.ui.theme.ElementXTheme
import io.element.android.x.ui.theme.components.VectorButton

class LoginActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ElementXTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            val viewModel: LoginViewModel = mavericksViewModel()
                            val state by viewModel.collectAsState()
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
                                    viewModel.handle(LoginActions.SetHomeserver(it))
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
                                    viewModel.handle(LoginActions.SetLogin(it))
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
                                    viewModel.handle(LoginActions.SetPassword(it))
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
                                    viewModel.handle(LoginActions.Submit)
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
                                openRoomList()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openRoomList() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
