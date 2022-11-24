package io.element.android.x.features.login.changeserver

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Uninitialized
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.launch

class ChangeServerViewModel(initialState: ChangeServerViewState) :
    MavericksViewModel<ChangeServerViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()

    init {
        setState {
            copy(
                homeserver = matrix.getHomeserverOrDefault()
            )
        }
    }

    fun setServer(server: String) {
        setState {
            copy(
                homeserver = server,
                changeServerAction = Uninitialized,
            )
        }
    }

    fun setServerSubmit() {
        viewModelScope.launch {
            suspend {
                val state = awaitState()
                matrix.setHomeserver(state.homeserver)
            }.execute { it ->
                copy(changeServerAction = it)
            }
        }
    }
}