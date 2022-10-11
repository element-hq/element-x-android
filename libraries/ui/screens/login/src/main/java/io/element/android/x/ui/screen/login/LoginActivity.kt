package io.element.android.x.ui.screen.login

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.Success
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.ui.theme.ElementXTheme
import io.element.android.x.ui.theme.components.VectorButton
import io.element.android.x.ui.theme.components.VectorTextField

class LoginActivity : ComponentActivity() {

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
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val viewModel: LoginViewModel = mavericksViewModel()
                        val state by viewModel.collectAsState()
                        val isError = state.isLoggedIn is Fail
                        VectorTextField(value = state.homeserver,
                            onValueChange = {
                                viewModel.handle(LoginActions.SetHomeserver(it))
                            })
                        VectorTextField(
                            value = state.login,
                            onValueChange = {
                                viewModel.handle(LoginActions.SetLogin(it))
                            })
                        VectorTextField(
                            value = state.password,
                            onValueChange = {
                                viewModel.handle(LoginActions.SetPassword(it))
                            },
                            isError = isError
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
                            modifier = Modifier.align(Alignment.End)
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

    private fun openRoomList() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}
