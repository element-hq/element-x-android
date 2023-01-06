package io.element.android.x.features.login.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Presenter
import io.element.android.x.matrix.Matrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoginRootPresenter @Inject constructor(private val matrix: Matrix) : Presenter<LoginRootState, LoginRootEvents> {

    @Composable
    override fun present(events: Flow<LoginRootEvents>): LoginRootState {
        val homeserver = rememberSaveable {
            mutableStateOf(matrix.getHomeserverOrDefault())
        }
        val loggedInState: MutableState<LoggedInState> = remember {
            mutableStateOf(LoggedInState.NotLoggedIn)
        }
        val formState = rememberSaveable {
            mutableStateOf(LoginFormState.Default)
        }

        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    LoginRootEvents.RefreshHomeServer -> refreshHomeServer(homeserver)
                    is LoginRootEvents.SetLogin -> updateFormState(formState) {
                        copy(login = event.login)
                    }
                    is LoginRootEvents.SetPassword -> updateFormState(formState) {
                        copy(password = event.password)
                    }
                    LoginRootEvents.Submit -> submit(homeserver.value, formState.value, loggedInState)
                }
            }
        }
        return LoginRootState(
            homeserver = homeserver.value,
            loggedInState = loggedInState.value,
            formState = formState.value
        )
    }

    private fun CoroutineScope.submit(homeserver: String, formState: LoginFormState, loggedInState: MutableState<LoggedInState>) = launch {
        loggedInState.value = LoggedInState.LoggingIn
        try {
            matrix.setHomeserver(homeserver)
            val sessionId = matrix.login(formState.login.trim(), formState.password.trim())
            loggedInState.value = LoggedInState.LoggedIn(sessionId)
        } catch (failure: Throwable) {
            loggedInState.value = LoggedInState.ErrorLoggingIn(failure)
        }
    }

    private fun updateFormState(formState: MutableState<LoginFormState>, updateLambda: LoginFormState.() -> LoginFormState) {
        formState.value = updateLambda(formState.value)
    }

    private fun refreshHomeServer(homeserver: MutableState<String>) {
        homeserver.value = matrix.getHomeserverOrDefault()
    }
}
