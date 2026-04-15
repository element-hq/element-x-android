/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.threads.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentFactory
import io.element.android.features.messages.impl.utils.messagesummary.MessageSummaryFormatter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.threads.ThreadListPaginationStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

@Inject
class ThreadsListPresenter(
    private val room: JoinedRoom,
    private val timelineItemContentFactory: TimelineItemContentFactory,
    private val messageSummaryFormatter: MessageSummaryFormatter,
    private val dateFormatter: DateFormatter,
) : Presenter<ThreadsListState> {
    @Composable
    override fun present(): ThreadsListState {
        val coroutineScope = rememberCoroutineScope()
        val threadsListService = room.threadsListService

        val threads by produceState(initialValue = persistentListOf(), key1 = threadsListService) {
            threadsListService.subscribeToItemUpdates()
                .onStart { threadsListService.paginate() }
                .collect { items ->
                    Timber.d("Received thread list update with ${items.size} items")
                    value = items.map { item ->
                        val rootTimelineEvent = item.rootEvent.content?.let {
                            timelineItemContentFactory.create(
                                itemContent = it,
                                eventId = item.rootEvent.eventId,
                                isEditable = false,
                                sender = item.rootEvent.senderId,
                                senderProfile = item.rootEvent.senderProfile,
                            )
                        }
                        val rootEventText = rootTimelineEvent?.let { messageSummaryFormatter.format(it) }

                        val latestTimelineEvent = item.latestEvent?.content?.let {
                            timelineItemContentFactory.create(
                                itemContent = it,
                                eventId = item.latestEvent!!.eventId,
                                isEditable = false,
                                sender = item.latestEvent!!.senderId,
                                senderProfile = item.latestEvent!!.senderProfile,
                            )
                        }
                        val latestEventText = latestTimelineEvent?.let { messageSummaryFormatter.format(it) }

                        val formattedTimestamp = dateFormatter.format(
                            timestamp = item.latestEvent?.timestamp ?: item.rootEvent.timestamp,
                            mode = DateFormatterMode.TimeOrDate,
                            useRelative = true,
                        )

                        ThreadListRowItem(
                            item = item,
                            rootEventText = rootEventText,
                            latestEventText = latestEventText,
                            formattedTimestamp = formattedTimestamp,
                        )
                    }.toImmutableList()
                }
        }

        val paginationStatus by produceState<ThreadListPaginationStatus>(
            initialValue = ThreadListPaginationStatus.Idle(hasMoreToLoad = true),
            key1 = threadsListService
        ) {
            threadsListService
                .subscribeToPaginationUpdates()
                .collect { value = it }
        }

        val roomInfo by room.roomInfoFlow.collectAsState()

        DisposableEffect(Unit) {
            onDispose {
                threadsListService.destroy()
            }
        }

        fun handleEvent(event: ThreadsListEvents) {
            when (event) {
                ThreadsListEvents.Paginate -> if ((paginationStatus as? ThreadListPaginationStatus.Idle)?.hasMoreToLoad == true) {
                    coroutineScope.launch {
                        Timber.d("Paginating thread list: $paginationStatus")
                        threadsListService.paginate()
                    }
                } else {
                    Timber.d("Not paginating since there is nothing else to load, current status: $paginationStatus")
                }
            }
        }

        return ThreadsListState(
            threads = threads,
            roomId = room.roomId,
            roomName = roomInfo.name ?: room.roomId.value,
            roomAvatarUrl = roomInfo.avatarUrl,
            isRoomTombstoned = roomInfo.successorRoom != null,
            eventSink = ::handleEvent,
        )
    }
}

data class ThreadsListState(
    val roomId: RoomId,
    val roomName: String,
    val roomAvatarUrl: String?,
    val isRoomTombstoned: Boolean,
    val threads: ImmutableList<ThreadListRowItem>,
    val eventSink: (ThreadsListEvents) -> Unit,
)

sealed interface ThreadsListEvents {
    data object Paginate : ThreadsListEvents
}
