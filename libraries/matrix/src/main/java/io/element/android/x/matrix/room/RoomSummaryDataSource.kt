package io.element.android.x.matrix.room

import io.element.android.x.core.data.CoroutineDispatchers
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.room.message.RoomMessageFactory
import io.element.android.x.matrix.sync.roomListDiff
import io.element.android.x.matrix.sync.state
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.matrix.rustcomponents.sdk.*
import java.util.*

interface RoomSummaryDataSource {
    fun roomSummaries(): Flow<List<RoomSummary>>
}

internal class RustRoomSummaryDataSource(
    private val slidingSync: SlidingSync,
    private val slidingSyncView: SlidingSyncView,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomMessageFactory: RoomMessageFactory = RoomMessageFactory(),
) : RoomSummaryDataSource {

    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatchers.io)

    private val roomSummaries = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val state = MutableStateFlow(SlidingSyncState.COLD)

    init {
        slidingSyncView.roomListDiff()
            .onEach { diff ->
                updateRoomSummaries {
                    applyDiff(diff)
                }
            }.launchIn(coroutineScope)

        slidingSyncView.state()
            .onEach { newRoomState ->
                state.value = newRoomState
            }.launchIn(coroutineScope)
    }

    override fun roomSummaries(): Flow<List<RoomSummary>> {
        return roomSummaries
    }

    internal fun updateRoomsWithIdentifiers(identifiers: List<String>) {
        if (state.value != SlidingSyncState.LIVE) {
            return
        }
        val roomSummaryList = roomSummaries.value.toMutableList()
        for (identifier in identifiers) {
            val index = roomSummaryList.indexOfFirst { it.identifier() == identifier }
            if (index == -1) {
                continue
            }
            val updatedRoomSummary = buildRoomSummaryForIdentifier(identifier)
            roomSummaryList[index] = updatedRoomSummary
        }
        roomSummaries.value = roomSummaryList
    }

    private fun MutableList<RoomSummary>.applyDiff(diff: SlidingSyncViewRoomsListDiff) {
        if (diff.isInvalidation()) {
            return
        }
        when (diff) {
            is SlidingSyncViewRoomsListDiff.Push -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.UpdateAt -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                set(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.InsertAt -> {
                val roomSummary = buildSummaryForRoomListEntry(diff.value)
                add(diff.index.toInt(), roomSummary)
            }
            is SlidingSyncViewRoomsListDiff.Move -> {
                Collections.swap(this, diff.oldIndex.toInt(), diff.newIndex.toInt())
            }
            is SlidingSyncViewRoomsListDiff.RemoveAt -> {
                removeAt(diff.index.toInt())
            }
            is SlidingSyncViewRoomsListDiff.Replace -> {
                clear()
                addAll(diff.values.map { buildSummaryForRoomListEntry(it) })
            }
        }
    }

    private fun buildSummaryForRoomListEntry(entry: RoomListEntry): RoomSummary {
        return when (entry) {
            RoomListEntry.Empty -> RoomSummary.Empty(UUID.randomUUID().toString())
            is RoomListEntry.Invalidated -> buildRoomSummaryForIdentifier(entry.roomId)
            is RoomListEntry.Filled -> buildRoomSummaryForIdentifier(entry.roomId)
        }
    }

    private fun buildRoomSummaryForIdentifier(identifier: String): RoomSummary {
        val room = slidingSync.getRoom(identifier) ?: return RoomSummary.Empty(identifier)
        val latestRoomMessage = room.latestRoomMessage()?.let {
            roomMessageFactory.create(it)
        }
        return RoomSummary.Filled(
            details = RoomSummaryDetails(
                roomId = RoomId(identifier),
                name = room.name(),
                isDirect = room.isDm() ?: false,
                avatarURLString = room.fullRoom()?.avatarUrl(),
                unreadNotificationCount = room.unreadNotifications().notificationCount(),
                lastMessage = latestRoomMessage?.body,
                lastMessageTimestamp = latestRoomMessage?.originServerTs
            )
        )
    }

    private fun updateRoomSummaries(block: MutableList<RoomSummary>.() -> Unit) {
        val mutableRoomSummaries = roomSummaries.value.toMutableList()
        block(mutableRoomSummaries)
        roomSummaries.value = mutableRoomSummaries
    }

}

fun SlidingSyncViewRoomsListDiff.isInvalidation(): Boolean {
    return when (this) {
        is SlidingSyncViewRoomsListDiff.InsertAt -> this.value is RoomListEntry.Invalidated
        is SlidingSyncViewRoomsListDiff.UpdateAt -> this.value is RoomListEntry.Invalidated
        is SlidingSyncViewRoomsListDiff.Push -> this.value is RoomListEntry.Invalidated
        else -> false
    }
}
