package io.element.android.x.features.logout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.element.android.x.architecture.Async
import io.element.android.x.architecture.Presenter
import io.element.android.x.architecture.execute
import io.element.android.x.matrix.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LogoutPreferencePresenter @Inject constructor(private val matrixClient: MatrixClient) : Presenter<LogoutPreferenceState> {

    @Composable
    override fun present(): LogoutPreferenceState {
        val localCoroutineScope = rememberCoroutineScope()
        val logoutAction: MutableState<Async<Unit>> = remember {
            mutableStateOf(Async.Uninitialized)
        }

        fun handleEvents(event: LogoutPreferenceEvents) {
            when (event) {
                LogoutPreferenceEvents.Logout -> localCoroutineScope.logout(logoutAction)
            }
        }

        return LogoutPreferenceState(
            logoutAction = logoutAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.logout(logoutAction: MutableState<Async<Unit>>) = launch {
        suspend {
            matrixClient.logout()
        }.execute(logoutAction)
    }
}
