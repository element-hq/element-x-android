/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.core.cache.CircularCache
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import timber.log.Timber

data class NotificationEventQueue(
    private val queue: MutableList<NotifiableEvent>,
    /**
     * An in memory FIFO cache of the seen events.
     * Acts as a notification debouncer to stop already dismissed push notifications from
     * displaying again when the /sync response is delayed.
     * TODO Should be per session, so the key must be Pair<SessionId, EventId>.
     */
    private val seenEventIds: CircularCache<EventId>
) {
    fun markRedacted(eventIds: List<EventId>) {
        eventIds.forEach { redactedId ->
            queue.replace(redactedId) {
                when (it) {
                    is InviteNotifiableEvent -> it.copy(isRedacted = true)
                    is NotifiableMessageEvent -> it.copy(isRedacted = true)
                    is SimpleNotifiableEvent -> it.copy(isRedacted = true)
                    is FallbackNotifiableEvent -> it.copy(isRedacted = true)
                }
            }
        }
    }

    // TODO EAx call this
    fun syncRoomEvents(roomsLeft: Collection<RoomId>, roomsJoined: Collection<RoomId>) {
        if (roomsLeft.isNotEmpty() || roomsJoined.isNotEmpty()) {
            queue.removeAll {
                when (it) {
                    is NotifiableMessageEvent -> roomsLeft.contains(it.roomId)
                    is InviteNotifiableEvent -> roomsLeft.contains(it.roomId) || roomsJoined.contains(it.roomId)
                    is SimpleNotifiableEvent -> false
                    is FallbackNotifiableEvent -> roomsLeft.contains(it.roomId)
                }
            }
        }
    }

    fun isEmpty() = queue.isEmpty()

    fun clearAndAdd(events: List<NotifiableEvent>) {
        queue.clear()
        queue.addAll(events)
    }

    fun clear() {
        queue.clear()
    }

    fun add(notifiableEvent: NotifiableEvent) {
        val existing = findExistingById(notifiableEvent)
        val edited = findEdited(notifiableEvent)
        when {
            existing != null -> {
                if (existing.canBeReplaced) {
                    // Use the event coming from the event stream as it may contains more info than
                    // the fcm one (like type/content/clear text) (e.g when an encrypted message from
                    // FCM should be update with clear text after a sync)
                    // In this case the message has already been notified, and might have done some noise
                    // So we want the notification to be updated even if it has already been displayed
                    // Use setOnlyAlertOnce to ensure update notification does not interfere with sound
                    // from first notify invocation as outlined in:
                    // https://developer.android.com/training/notify-user/build-notification#Updating
                    replace(replace = existing, with = notifiableEvent)
                } else {
                    // keep the existing one, do not replace
                }
            }
            edited != null -> {
                // Replace the existing notification with the new content
                replace(replace = edited, with = notifiableEvent)
            }
            seenEventIds.contains(notifiableEvent.eventId) -> {
                // we've already seen the event, lets skip
                Timber.d("onNotifiableEventReceived(): skipping event, already seen")
            }
            else -> {
                seenEventIds.put(notifiableEvent.eventId)
                queue.add(notifiableEvent)
            }
        }
    }

    private fun findExistingById(notifiableEvent: NotifiableEvent): NotifiableEvent? {
        return queue.firstOrNull { it.sessionId == notifiableEvent.sessionId && it.eventId == notifiableEvent.eventId }
    }

    private fun findEdited(notifiableEvent: NotifiableEvent): NotifiableEvent? {
        return notifiableEvent.editedEventId?.let { editedId ->
            queue.firstOrNull {
                it.eventId == editedId || it.editedEventId == editedId
            }
        }
    }

    private fun replace(replace: NotifiableEvent, with: NotifiableEvent) {
        queue.remove(replace)
        queue.add(
            when (with) {
                is InviteNotifiableEvent -> with.copy(isUpdated = true)
                is NotifiableMessageEvent -> with.copy(isUpdated = true)
                is SimpleNotifiableEvent -> with.copy(isUpdated = true)
                is FallbackNotifiableEvent -> with.copy(isUpdated = true)
            }
        )
    }

    fun clearEvent(sessionId: SessionId, eventId: EventId) {
        val isFallback = queue.firstOrNull { it.sessionId == sessionId && it.eventId == eventId } is FallbackNotifiableEvent
        if (isFallback) {
            Timber.d("Removing all the fallbacks")
            queue.removeAll { it.sessionId == sessionId && it is FallbackNotifiableEvent }
        } else {
            queue.removeAll { it.sessionId == sessionId && it.eventId == eventId }
        }
    }

    fun clearMembershipNotificationForSession(sessionId: SessionId) {
        Timber.d("clearMemberShipOfSession $sessionId")
        queue.removeAll { it is InviteNotifiableEvent && it.sessionId == sessionId }
    }

    fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId) {
        Timber.d("clearMemberShipOfRoom $sessionId, $roomId")
        queue.removeAll { it is InviteNotifiableEvent && it.sessionId == sessionId && it.roomId == roomId }
    }

    fun clearMessagesForSession(sessionId: SessionId) {
        Timber.d("clearMessagesForSession $sessionId")
        queue.removeAll { it is NotifiableMessageEvent && it.sessionId == sessionId }
    }

    fun clearAllForSession(sessionId: SessionId) {
        Timber.d("clearAllForSession $sessionId")
        queue.removeAll { it.sessionId == sessionId }
    }

    fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId) {
        Timber.d("clearMessageEventOfRoom $sessionId, $roomId")
        queue.removeAll { it is NotifiableMessageEvent && it.sessionId == sessionId && it.roomId == roomId }
    }

    fun clearMessagesForThread(sessionId: SessionId, roomId: RoomId, threadId: ThreadId) {
        Timber.d("clearMessageEventOfThread $sessionId, $roomId, $threadId")
        queue.removeAll { it is NotifiableMessageEvent && it.sessionId == sessionId && it.roomId == roomId && it.threadId == threadId }
    }

    fun rawEvents(): List<NotifiableEvent> = queue
}

private fun MutableList<NotifiableEvent>.replace(eventId: EventId, block: (NotifiableEvent) -> NotifiableEvent) {
    val indexToReplace = indexOfFirst { it.eventId == eventId }
    if (indexToReplace == -1) {
        return
    }
    set(indexToReplace, block(get(indexToReplace)))
}
