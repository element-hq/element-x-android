package io.element.android.x

import com.airbnb.mvrx.MavericksState
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.AppScope
import io.element.android.x.features.messages.MessagesViewModel
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.matrix.Matrix
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MainState(val fake: Boolean = false) : MavericksState

@ContributesViewModel(AppScope::class)
class MainViewModel @AssistedInject constructor(
    private val matrix: Matrix,
    @Assisted initialState: MainState
) : MavericksViewModel<MainState>(initialState) {

    companion object : MavericksViewModelFactory<MainViewModel, MainState> by daggerMavericksViewModelFactory()

    suspend fun isLoggedIn(): Boolean {
        return matrix.isLoggedIn().first()
    }

    fun startSyncIfLogged() {
        viewModelScope.launch {
            if (!isLoggedIn()) return@launch
            matrix.activeClient().startSync()
        }
    }

    fun stopSyncIfLogged() {
        viewModelScope.launch {
            if (!isLoggedIn()) return@launch
            matrix.activeClient().stopSync()
        }
    }

    suspend fun restoreSession() {
        matrix.restoreSession()
        matrix.activeClient().startSync()
    }
}