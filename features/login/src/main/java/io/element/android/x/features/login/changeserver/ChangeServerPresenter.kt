package io.element.android.x.features.login.changeserver

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.x.architecture.Async
import io.element.android.x.architecture.Presenter
import io.element.android.x.architecture.execute
import io.element.android.x.matrix.Matrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeServerPresenter @Inject constructor(private val matrix: Matrix) : Presenter<ChangeServerState> {

    @Composable
    override fun present(): ChangeServerState {
        val localCoroutineScope = rememberCoroutineScope()
        val homeserver = rememberSaveable {
            mutableStateOf(matrix.getHomeserverOrDefault())
        }
        val changeServerAction: MutableState<Async<Unit>> = remember {
            mutableStateOf(Async.Uninitialized)
        }

        fun handleEvents(event: ChangeServerEvents) {
            when (event) {
                is ChangeServerEvents.SetServer -> homeserver.value = event.server
                ChangeServerEvents.Submit -> localCoroutineScope.submit(homeserver.value, changeServerAction)
            }
        }

        return ChangeServerState(
            homeserver = homeserver.value,
            changeServerAction = changeServerAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.submit(homeserver: String, changeServerAction: MutableState<Async<Unit>>) = launch {
        suspend {
            matrix.setHomeserver(homeserver)
        }.execute(changeServerAction)
    }
}
