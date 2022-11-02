package io.element.android.x.matrix.room

import io.element.android.x.core.data.CoroutineDispatchers
import io.element.android.x.matrix.core.RoomId
import io.element.android.x.matrix.room.message.RoomMessageFactory
import io.element.android.x.matrix.sync.roomListDiff
import io.element.android.x.matrix.sync.state
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.*
import org.matrix.rustcomponents.sdk.*
import timber.log.Timber
import java.io.Closeable
import java.util.*

interface RoomSummaryDataSource {
    fun roomSummaries(): Flow<List<RoomSummary>>
}

internal class RustRoomSummaryDataSource(
    private val slidingSync: SlidingSync,
    private val slidingSyncView: SlidingSyncView,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val roomMessageFactory: RoomMessageFactory = RoomMessageFactory(),
) : RoomSummaryDataSource, Closeable {

    private val coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatchers.io)

    private val roomSummaries = MutableStateFlow<List<RoomSummary>>(emptyList())
    private val state = MutableStateFlow(SlidingSyncState.COLD)

    init {
        slidingSyncView.roomListDiff()
            .buffer(50)
            .onEach { diff ->
                updateRoomSummaries {
                    applyDiff(diff)
                }
            }.launchIn(coroutineScope)

        slidingSyncView.state()
            .onEach { slidingSyncState ->
                Timber.v("New sliding sync state: $slidingSyncState")
                state.value = slidingSyncState
            }.launchIn(coroutineScope)
    }

    fun stopSync() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    override fun close() {
        coroutineScope.cancel()
    }

    override fun roomSummaries(): Flow<List<RoomSummary>> {
        return roomSummaries.sample(100)
    }

    internal fun updateRoomsWithIdentifiers(identifiers: List<String>) {
        Timber.v("UpdateRooms with identifiers: $identifiers")
        if (state.value != SlidingSyncState.LIVE) {
            return
        }
        updateRoomSummaries {
            for (identifier in identifiers) {
                val index = indexOfFirst { it.identifier() == identifier }
                if (index == -1) {
                    continue
                }
                val updatedRoomSummary = buildRoomSummaryForIdentifier(identifier)
                set(index, updatedRoomSummary)
            }
        }
    }

    private fun MutableList<RoomSummary>.applyDiff(diff: SlidingSyncViewRoomsListDiff) {
        Timber.v("ApplyDiff: $diff")
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
                name = room.name() ?: identifier,
                isDirect = room.isDm() ?: false,
                avatarURLString = room.fullRoom()?.avatarUrl(),
                unreadNotificationCount = room.unreadNotifications().notificationCount().toInt(),
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
