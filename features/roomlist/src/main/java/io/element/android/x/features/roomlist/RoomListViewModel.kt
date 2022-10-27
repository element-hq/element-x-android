package io.element.android.x.features.roomlist

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import io.element.android.x.core.data.tryOrNull
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.Room
import org.matrix.rustcomponents.sdk.StoppableSpawn
import org.matrix.rustcomponents.sdk.UpdateSummary

class RoomListViewModel(initialState: RoomListViewState) :
    MavericksViewModel<RoomListViewState>(initialState), MatrixClient.SlidingSyncListener {

    private var sync: StoppableSpawn? = null
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
            setState {
                copy(
                    user = MatrixUser(
                        tryOrNull { client.username() } ?: "Room list",
                        tryOrNull { client.avatarUrl() } ?: "https://previews.123rf.com/images/lkeskinen/lkeskinen1802/lkeskinen180208322/95731150-exemple-de-tampon-%C3%A9tiquette-typographique-timbre-ou-ic%C3%B4ne.jpg",
                    )
                )
            }
            sync = client.slidingSync(listener = this@RoomListViewModel)
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

    override fun onSyncUpdate(
        summary: UpdateSummary,
        rooms: List<Room>
    ) = withState { state ->
        val list = state.rooms().orEmpty().toMutableList()
        rooms.forEach { room ->
            // Either replace or add the room
            val idx = list.indexOfFirst { it.id() == room.id() }
            if (idx == -1) {
                list.add(room)
            } else {
                list[idx] = room
            }
        }

        setState {
            copy(
                rooms = Success(list),
                summary = Success(summary)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sync?.cancel()
    }
}