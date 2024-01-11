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

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.cache.CircularCache
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.anInviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import org.junit.Test

class NotificationEventQueueTest {
    private val seenIdsCache = CircularCache.create<EventId>(5)

    @Test
    fun `given events when redacting some then marks matching event ids as redacted`() {
        val queue = givenQueue(
            listOf(
                aSimpleNotifiableEvent(eventId = EventId("\$redacted-id-1")),
                aNotifiableMessageEvent(eventId = EventId("\$redacted-id-2")),
                anInviteNotifiableEvent(eventId = EventId("\$redacted-id-3")),
                aSimpleNotifiableEvent(eventId = EventId("\$kept-id")),
            )
        )

        queue.markRedacted(listOf(EventId("\$redacted-id-1"), EventId("\$redacted-id-2"), EventId("\$redacted-id-3")))

        assertThat(queue.rawEvents()).isEqualTo(
            listOf(
                aSimpleNotifiableEvent(eventId = EventId("\$redacted-id-1"), isRedacted = true),
                aNotifiableMessageEvent(eventId = EventId("\$redacted-id-2"), isRedacted = true),
                anInviteNotifiableEvent(eventId = EventId("\$redacted-id-3"), isRedacted = true),
                aSimpleNotifiableEvent(eventId = EventId("\$kept-id"), isRedacted = false),
            )
        )
    }

    @Test
    fun `given invite event when leaving invited room and syncing then removes event`() {
        val queue = givenQueue(listOf(anInviteNotifiableEvent(roomId = A_ROOM_ID)))
        val roomsLeft = listOf(A_ROOM_ID)

        queue.syncRoomEvents(roomsLeft = roomsLeft, roomsJoined = emptyList())

        assertThat(queue.rawEvents()).isEmpty()
    }

    @Test
    fun `given invite event when joining invited room and syncing then removes event`() {
        val queue = givenQueue(listOf(anInviteNotifiableEvent(roomId = A_ROOM_ID)))
        val joinedRooms = listOf(A_ROOM_ID)

        queue.syncRoomEvents(roomsLeft = emptyList(), roomsJoined = joinedRooms)

        assertThat(queue.rawEvents()).isEmpty()
    }

    @Test
    fun `given message event when leaving message room and syncing then removes event`() {
        val queue = givenQueue(listOf(aNotifiableMessageEvent(roomId = A_ROOM_ID)))
        val roomsLeft = listOf(A_ROOM_ID)

        queue.syncRoomEvents(roomsLeft = roomsLeft, roomsJoined = emptyList())

        assertThat(queue.rawEvents()).isEmpty()
    }

    @Test
    fun `given events when syncing without rooms left or joined ids then does not change the events`() {
        val queue = givenQueue(
            listOf(
                aNotifiableMessageEvent(roomId = A_ROOM_ID),
                anInviteNotifiableEvent(roomId = A_ROOM_ID)
            )
        )

        queue.syncRoomEvents(roomsLeft = emptyList(), roomsJoined = emptyList())

        assertThat(queue.rawEvents()).isEqualTo(
            listOf(
                aNotifiableMessageEvent(roomId = A_ROOM_ID),
                anInviteNotifiableEvent(roomId = A_ROOM_ID)
            )
        )
    }

    @Test
    fun `given events then is not empty`() {
        val queue = givenQueue(listOf(aSimpleNotifiableEvent()))

        assertThat(queue.isEmpty()).isFalse()
    }

    @Test
    fun `given no events then is empty`() {
        val queue = givenQueue(emptyList())

        assertThat(queue.isEmpty()).isTrue()
    }

    @Test
    fun `given events when clearing and adding then removes previous events and adds only new events`() {
        val queue = givenQueue(listOf(aSimpleNotifiableEvent()))

        queue.clearAndAdd(listOf(anInviteNotifiableEvent()))

        assertThat(queue.rawEvents()).isEqualTo(listOf(anInviteNotifiableEvent()))
    }

    @Test
    fun `when clearing then is empty`() {
        val queue = givenQueue(listOf(aSimpleNotifiableEvent()))

        queue.clear()

        assertThat(queue.rawEvents()).isEmpty()
    }

    @Test
    fun `given no events when adding then adds event`() {
        val queue = givenQueue(listOf())

        queue.add(aSimpleNotifiableEvent())

        assertThat(queue.rawEvents()).isEqualTo(listOf(aSimpleNotifiableEvent()))
    }

    @Test
    fun `given no events when adding already seen event then ignores event`() {
        val queue = givenQueue(listOf())
        val notifiableEvent = aSimpleNotifiableEvent()
        seenIdsCache.put(notifiableEvent.eventId)

        queue.add(notifiableEvent)

        assertThat(queue.rawEvents()).isEmpty()
    }

    @Test
    fun `given replaceable event when adding event with same id then updates existing event`() {
        val replaceableEvent = aSimpleNotifiableEvent(canBeReplaced = true)
        val updatedEvent = replaceableEvent.copy(title = "updated title", isUpdated = true)
        val queue = givenQueue(listOf(replaceableEvent))

        queue.add(updatedEvent)

        assertThat(queue.rawEvents()).isEqualTo(listOf(updatedEvent))
    }

    @Test
    fun `given non replaceable event when adding event with same id then ignores event`() {
        val nonReplaceableEvent = aSimpleNotifiableEvent(canBeReplaced = false)
        val updatedEvent = nonReplaceableEvent.copy(title = "updated title")
        val queue = givenQueue(listOf(nonReplaceableEvent))

        queue.add(updatedEvent)

        assertThat(queue.rawEvents()).isEqualTo(listOf(nonReplaceableEvent))
    }

    @Test
    fun `given event when adding new event with edited event id matching the existing event id then updates existing event`() {
        val editedEvent = aSimpleNotifiableEvent(eventId = EventId("\$id-to-edit"))
        val updatedEvent = editedEvent.copy(eventId = EventId("\$1"), editedEventId = EventId("\$id-to-edit"), title = "updated title", isUpdated = true)
        val queue = givenQueue(listOf(editedEvent))

        queue.add(updatedEvent)

        assertThat(queue.rawEvents()).isEqualTo(listOf(updatedEvent))
    }

    @Test
    fun `given event when adding new event with edited event id matching the existing event edited id then updates existing event`() {
        val editedEvent = aSimpleNotifiableEvent(eventId = EventId("\$0"), editedEventId = EventId("\$id-to-edit"))
        val updatedEvent = editedEvent.copy(eventId = EventId("\$1"), editedEventId = EventId("\$id-to-edit"), title = "updated title", isUpdated = true)
        val queue = givenQueue(listOf(editedEvent))

        queue.add(updatedEvent)

        assertThat(queue.rawEvents()).isEqualTo(listOf(updatedEvent))
    }

    @Test
    fun `when clearing membership notification then removes invite events with matching room id`() {
        val queue = givenQueue(
            listOf(
                anInviteNotifiableEvent(roomId = A_ROOM_ID),
                aNotifiableMessageEvent(roomId = A_ROOM_ID)
            )
        )

        queue.clearMembershipNotificationForRoom(A_SESSION_ID, A_ROOM_ID)

        assertThat(queue.rawEvents()).isEqualTo(listOf(aNotifiableMessageEvent(roomId = A_ROOM_ID)))
    }

    @Test
    fun `when clearing messages for room then removes message events with matching room id`() {
        val queue = givenQueue(
            listOf(
                anInviteNotifiableEvent(roomId = A_ROOM_ID),
                aNotifiableMessageEvent(roomId = A_ROOM_ID)
            )
        )

        queue.clearMessagesForRoom(A_SESSION_ID, A_ROOM_ID)

        assertThat(queue.rawEvents()).isEqualTo(listOf(anInviteNotifiableEvent(roomId = A_ROOM_ID)))
    }

    private fun givenQueue(events: List<NotifiableEvent>) = NotificationEventQueue(events.toMutableList(), seenEventIds = seenIdsCache)
}
