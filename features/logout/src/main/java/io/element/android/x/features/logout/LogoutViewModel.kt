package io.element.android.x.features.logout

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.x.anvilannotations.ContributesViewModel
import io.element.android.x.core.di.daggerMavericksViewModelFactory
import io.element.android.x.di.SessionScope
import io.element.android.x.matrix.MatrixClient
import kotlinx.coroutines.launch

@ContributesViewModel(SessionScope::class)
class LogoutViewModel @AssistedInject constructor(
    private val client: MatrixClient,
    @Assisted initialState: LogoutViewState
) : MavericksViewModel<LogoutViewState>(initialState) {

    companion object : MavericksViewModelFactory<LogoutViewModel, LogoutViewState> by daggerMavericksViewModelFactory()

    fun logout() {
        viewModelScope.launch {
            suspend {
                client.logout()
            }.execute {
                copy(logoutAction = it)
            }
        }
    }
}
