package io.element.android.x.features.login.changeserver

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.Uninitialized
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.AppScope
import io.element.android.x.matrix.Matrix
import kotlinx.coroutines.launch

@ContributesViewModel(AppScope::class)
class ChangeServerViewModel @AssistedInject constructor(
    private val matrix: Matrix,
    @Assisted initialState: ChangeServerViewState
) :
    MavericksViewModel<ChangeServerViewState>(initialState) {

    companion object :
        MavericksViewModelFactory<ChangeServerViewModel, ChangeServerViewState> by daggerMavericksViewModelFactory()

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
            }.execute {
                copy(changeServerAction = it)
            }
        }
    }
}