package io.element.android.x.ui.screen.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.x.ui.theme.ElementXTheme
import io.element.android.x.ui.theme.components.VectorButton
import io.element.android.x.ui.theme.components.VectorTextField

class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()

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
                        val state = viewModel.state.collectAsState().value
                        VectorTextField(
                            value = state.login,
                            onValueChange = {
                                viewModel.handle(LoginActions.SetLogin(it))
                            })
                        VectorTextField(
                            value = state.password,
                            onValueChange = {
                                viewModel.handle(LoginActions.SetPassword(it))
                            }
                        )
                        VectorButton(
                            text = "Submit",
                            onClick = {
                                viewModel.handle(LoginActions.Submit)
                            },
                            enabled = state.submitEnabled,
                        )
                    }
                }
            }
        }
    }
}
