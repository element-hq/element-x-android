package io.element.android.x.features.roomlist

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

class RoomListViewModel(initialState: RoomListViewState) :
    MavericksViewModel<RoomListViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()

    init {
        handleInit()
    }

    fun handle(action: RoomListActions) {
        when (action) {
            RoomListActions.LoadMore -> TODO()
            RoomListActions.Logout -> handleLogout()
        }
    }

    private fun handleInit() {
        viewModelScope.launch {
            val client = getClient()
            client.startSync()
            val userAvatarUrl = client.loadUserAvatarURLString().getOrNull()
            val userDisplayName = client.loadUserDisplayName().getOrNull()
            val avatarData = userAvatarUrl?.let {
                mediaSourceFromUrl(it)
            }?.let {
                client.loadMediaContentForSource(it)
            }
            setState {
                copy(
                    user = MatrixUser(
                        username = userDisplayName,
                        avatarUrl = userAvatarUrl,
                        avatarData = avatarData?.getOrNull()
                    )
                )
            }
            client.roomSummaryDataSource().roomSummaries().execute {
                copy(rooms = it)
            }
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

    override fun onCleared() {
        super.onCleared()
    }
}