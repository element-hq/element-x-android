package io.element.android.x.features.roomlist

import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.Success
import io.element.android.x.core.data.parallelMap
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.features.roomlist.model.RoomListViewState
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import io.element.android.x.matrix.room.RoomSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.matrix.rustcomponents.sdk.mediaSourceFromUrl

class RoomListViewModel(initialState: RoomListViewState) :
    MavericksViewModel<RoomListViewState>(initialState) {

    private val matrix = MatrixInstance.getInstance()
    private val lastMessageFormatter = LastMessageFormatter()

    init {
        handleInit()
    }

    fun handle(action: RoomListActions) {
        when (action) {
            RoomListActions.Logout -> handleLogout()
        }
    }

    fun logout() {
        handleLogout()
    }

    private fun handleInit() {
        viewModelScope.launch {
            val client = getClient()
            client.startSync()
            val userAvatarUrl = client.loadUserAvatarURLString().getOrNull()
            val userDisplayName = client.loadUserDisplayName().getOrNull()
            val avatarData = loadAvatarData(client, userAvatarUrl)
            setState {
                copy(
                    user = MatrixUser(
                        username = userDisplayName,
                        avatarUrl = userAvatarUrl,
                        avatarData = avatarData,
                    )
                )
            }
            client.roomSummaryDataSource().roomSummaries()
                .map { roomSummaries ->
                    mapRoomSummaries(client, roomSummaries)
                }
                .flowOn(Dispatchers.Default)
                .execute {
                    copy(rooms = it)
                }
        }
    }

    private suspend fun mapRoomSummaries(
        client: MatrixClient,
        roomSummaries: List<RoomSummary>
    ): List<RoomListRoomSummary> {
        return roomSummaries.parallelMap { roomSummary ->
            when (roomSummary) {
                is RoomSummary.Empty -> RoomListRoomSummary(
                    id = roomSummary.identifier,
                    isPlaceholder = true
                )
                is RoomSummary.Filled -> {
                    val avatarData = loadAvatarData(client, roomSummary.details.avatarURLString)
                    RoomListRoomSummary(
                        id = roomSummary.identifier(),
                        name = roomSummary.details.name,
                        hasUnread = roomSummary.details.unreadNotificationCount > 0,
                        timestamp = lastMessageFormatter.format(roomSummary.details.lastMessageTimestamp),
                        lastMessage = roomSummary.details.lastMessage,
                        avatarData = avatarData,
                    )
                }
            }
        }
    }

    private suspend fun loadAvatarData(client: MatrixClient, url: String?, size: Long = 48): ByteArray? {
        val mediaContent = url?.let {
            val mediaSource = mediaSourceFromUrl(it)
            client.loadMediaThumbnailForSource(mediaSource, size, size)
        }
        return mediaContent?.fold(
            { it },
            { null }
        )
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