package io.element.android.x.ui.screen.roomlist

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import io.element.android.x.sdk.matrix.MatrixClient
import io.element.android.x.sdk.matrix.MatrixInstance
import kotlinx.coroutines.launch

class RoomListViewModel(initialState: RoomListViewState) :
    MavericksViewModel<RoomListViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()

    fun handle(action: RoomListActions) {
        when (action) {
            RoomListActions.LoadMore -> TODO()
            RoomListActions.Logout -> handleLogout()
        }
    }

    private fun handleLogout() {
        viewModelScope.launch {
            setState { copy(logoutAction = Loading()) }
            try {
                getClient().logout()
                setState { copy(logoutAction = Success(Unit)) }
            } catch (throwable: Throwable) {
                setState { copy(logoutAction = Fail(throwable)) }
            }
        }
    }

    private suspend fun getClient(): MatrixClient {
        return matrix.restoreSession()!!
    }
}