package io.element.android.x.features.roomlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.x.core.coroutine.parallelMap
import io.element.android.x.designsystem.components.avatar.AvatarData
import io.element.android.x.designsystem.components.avatar.AvatarSize
import io.element.android.x.features.roomlist.model.MatrixUser
import io.element.android.x.features.roomlist.model.RoomListEvents
import io.element.android.x.features.roomlist.model.RoomListRoomSummary
import io.element.android.x.features.roomlist.model.RoomListRoomSummaryPlaceholders
import io.element.android.x.features.roomlist.model.RoomListState
import io.element.android.x.matrix.MatrixClient
import io.element.android.x.matrix.media.MediaResolver
import io.element.android.x.matrix.room.RoomSummary
import io.element.android.x.presentation.Presenter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val extendedRangeSize = 40

class RoomListPresenter @Inject constructor(
    private val client: MatrixClient,
    private val lastMessageFormatter: LastMessageFormatter = LastMessageFormatter(),
) : Presenter<RoomListState, RoomListEvents> {

    @Composable
    override fun present(events: Flow<RoomListEvents>): RoomListState {
        val matrixUser: MutableState<MatrixUser?> = remember {
            mutableStateOf(null)
        }
        var filter by rememberSaveable { mutableStateOf("") }
        val isLoginOut = rememberSaveable { mutableStateOf(false) }
        val roomSummaries by client
            .roomSummaryDataSource()
            .roomSummaries()
            .collectAsState(initial = null)

        val filteredRoomSummaries: MutableState<ImmutableList<RoomListRoomSummary>> = remember {
            mutableStateOf(persistentListOf())
        }
        LaunchedEffect(Unit) {
            initialLoad(matrixUser)
            events.collect { event ->
                when (event) {
                    RoomListEvents.Logout -> logout(isLoginOut)
                    is RoomListEvents.UpdateFilter -> filter = event.newFilter
                    is RoomListEvents.UpdateVisibleRange -> updateVisibleRange(event.range)
                }
            }
        }
        LaunchedEffect(roomSummaries, filter) {
            filteredRoomSummaries.value = updateFilteredRoomSummaries(roomSummaries, filter)
        }
        return RoomListState(
            matrixUser = matrixUser.value,
            roomList = filteredRoomSummaries.value,
            filter = filter,
            isLoginOut = isLoginOut.value
        )
    }

    private suspend fun updateFilteredRoomSummaries(roomSummaries: List<RoomSummary>?, filter: String): ImmutableList<RoomListRoomSummary> {
        if (roomSummaries.isNullOrEmpty()) {
            return RoomListRoomSummaryPlaceholders.createFakeList(16).toImmutableList()
        }
        val mappedRoomSummaries = mapRoomSummaries(roomSummaries)
        return if (filter.isEmpty()) {
            mappedRoomSummaries
        } else {
            mappedRoomSummaries.filter { it.name.contains(filter, ignoreCase = true) }
        }.toImmutableList()
    }

    private suspend fun initialLoad(matrixUser: MutableState<MatrixUser?>) {
        val userAvatarUrl = client.loadUserAvatarURLString().getOrNull()
        val userDisplayName = client.loadUserDisplayName().getOrNull()
        val avatarData =
            loadAvatarData(
                userDisplayName ?: client.userId().value,
                userAvatarUrl,
                AvatarSize.SMALL
            )
        matrixUser.value = MatrixUser(
            username = userDisplayName ?: client.userId().value,
            avatarUrl = userAvatarUrl,
            avatarData = avatarData,
        )
    }

    private suspend fun logout(isLoginOut: MutableState<Boolean>) {
        isLoginOut.value = true
        delay(2000)
        client.logout()
        isLoginOut.value = false
    }

    private fun updateVisibleRange(range: IntRange) {
        if (range.isEmpty()) return
        val midExtendedRangeSize = extendedRangeSize / 2
        val extendedRangeStart = (range.first - midExtendedRangeSize).coerceAtLeast(0)
        // Safe to give bigger size than room list
        val extendedRangeEnd = range.last + midExtendedRangeSize
        val extendedRange = IntRange(extendedRangeStart, extendedRangeEnd)
        client.roomSummaryDataSource().setSlidingSyncRange(extendedRange)
    }

    private suspend fun mapRoomSummaries(
        roomSummaries: List<RoomSummary>
    ): List<RoomListRoomSummary> {
        return roomSummaries.parallelMap { roomSummary ->
            when (roomSummary) {
                is RoomSummary.Empty -> RoomListRoomSummaryPlaceholders.create(roomSummary.identifier)
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
