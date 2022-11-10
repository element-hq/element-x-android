package io.element.android.x.features.roomlist

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import io.element.android.x.core.data.parallelMap
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.features.roomlist.model.RoomListViewState
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.MatrixInstance
import io.element.android.x.matrix.media.MediaResolver
import io.element.android.x.matrix.room.RoomSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RoomListViewModel(
    private val client: MatrixClient,
    initialState: RoomListViewState
) :
    MavericksViewModel<RoomListViewState>(initialState) {

    companion object : MavericksViewModelFactory<RoomListViewModel, RoomListViewState> {

        override fun create(
            viewModelContext: ViewModelContext,
            state: RoomListViewState
        ): RoomListViewModel {
            val matrix = MatrixInstance.getInstance()
            val client = matrix.activeClient()
            return RoomListViewModel(
                client,
                state
            )
        }
    }

    private val lastMessageFormatter = LastMessageFormatter()

    init {
        handleInit()
    }

    fun logout() {
        viewModelScope.launch {
            suspend {
                delay(2000)
                client.logout()
            }.execute {
                copy(logoutAction = it)
            }
        }
    }

    private fun handleInit() {
        suspend {
            val userAvatarUrl = client.loadUserAvatarURLString().getOrNull()
            val userDisplayName = client.loadUserDisplayName().getOrNull()
            val avatarData =
                loadAvatarData(
                    userDisplayName ?: client.userId().value,
                    userAvatarUrl,
                    AvatarSize.SMALL
                )
            MatrixUser(
                username = userDisplayName ?: client.userId().value,
                avatarUrl = userAvatarUrl,
                avatarData = avatarData,
            )
        }.execute {
            copy(user = it)
        }

        client.roomSummaryDataSource().roomSummaries()
            .map(::mapRoomSummaries)
            .flowOn(Dispatchers.Default)
            .execute {
                copy(rooms = it)
            }
    }

    private suspend fun mapRoomSummaries(
        roomSummaries: List<RoomSummary>
    ): List<RoomListRoomSummary> {
        return roomSummaries.parallelMap { roomSummary ->
            when (roomSummary) {
                is RoomSummary.Empty -> RoomListRoomSummary.placeholder(roomSummary.identifier)
                is RoomSummary.Filled -> {
                    val avatarData = loadAvatarData(
                        roomSummary.details.name,
                        roomSummary.details.avatarURLString
                    )
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

    private suspend fun loadAvatarData(
        name: String,
        url: String?,
        size: AvatarSize = AvatarSize.MEDIUM
    ): AvatarData {
        val model = client.mediaResolver()
            .resolve(url, kind = MediaResolver.Kind.Thumbnail(size.value))
        return AvatarData(name, model, size)
    }

}