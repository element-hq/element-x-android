package io.element.android.x.features.login.changeserver

import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
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
            copy(homeserver = server)
        }
    }

    fun setServerSubmit() = withState { state ->
        setState {
            copy(changeServerAction = Loading())
        }

        viewModelScope.launch {
            suspend {
                matrix.setHomeserver(state.homeserver)
            }.execute { it ->
                copy(changeServerAction = it)
            }
        }
    }
}