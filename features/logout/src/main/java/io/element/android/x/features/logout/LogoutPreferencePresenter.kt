package io.element.android.x.features.logout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.x.architecture.Async
import io.element.android.x.architecture.Presenter
import io.element.android.x.architecture.execute
import io.element.android.x.matrix.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogoutPreferencePresenter @Inject constructor(private val matrixClient: MatrixClient) : Presenter<LogoutPreferenceState, LogoutPreferenceEvents> {

    @Composable
    override fun present(events: Flow<LogoutPreferenceEvents>): LogoutPreferenceState {
        val logoutAction: MutableState<Async<Unit>> = remember {
            mutableStateOf(Async.Uninitialized)
        }
        LaunchedEffect(Unit) {
            events.collect { event ->
                when (event) {
                    LogoutPreferenceEvents.Logout -> logout(logoutAction)
                }
            }
        }
        return LogoutPreferenceState(
            logoutAction = logoutAction.value
        )
    }

    private fun CoroutineScope.logout(logoutAction: MutableState<Async<Unit>>) = launch {
        suspend {
            matrixClient.logout()
        }.execute(logoutAction)
    }
}
