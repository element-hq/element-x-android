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
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SPACE_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.push.impl.notifications.fake.MockkOutdatedEventDetector
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.anInviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.NavigationState
import io.element.android.services.appnavstate.test.FakeAppNavigationStateService
import io.element.android.services.appnavstate.test.aNavigationState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

private val NOT_VIEWING_A_ROOM = aNavigationState()
private val VIEWING_A_ROOM = aNavigationState(A_SESSION_ID, A_SPACE_ID, A_ROOM_ID)
private val VIEWING_A_THREAD = aNavigationState(A_SESSION_ID, A_SPACE_ID, A_ROOM_ID, A_THREAD_ID)

class NotifiableEventProcessorTest {
    private val mockkOutdatedDetector = MockkOutdatedEventDetector()

    @Test
    fun `given simple events when processing then keep simple events`() {
        val events = listOf(
            aSimpleNotifiableEvent(eventId = AN_EVENT_ID),
            aSimpleNotifiableEvent(eventId = AN_EVENT_ID_2)
        )
        val eventProcessor = createProcessor(navigationState = NOT_VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.KEEP to events[0],
                ProcessedEvent.Type.KEEP to events[1]
            )
        )
    }

    @Test
    fun `given redacted simple event when processing then remove redaction event`() {
        val events = listOf(aSimpleNotifiableEvent(eventId = AN_EVENT_ID, type = EventType.REDACTION))
        val eventProcessor = createProcessor(navigationState = NOT_VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.REMOVE to events[0]
            )
        )
    }

    @Test
    fun `given invites are not auto accepted when processing then keep invitation events`() {
        val events = listOf(
            anInviteNotifiableEvent(roomId = A_ROOM_ID),
            anInviteNotifiableEvent(roomId = A_ROOM_ID_2)
        )
        val eventProcessor = createProcessor(navigationState = NOT_VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.KEEP to events[0],
                ProcessedEvent.Type.KEEP to events[1]
            )
        )
    }

    @Test
    fun `given out of date message event when processing then removes message event`() {
        val events = listOf(aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID))
        mockkOutdatedDetector.givenEventIsOutOfDate(events[0])

        val eventProcessor = createProcessor(navigationState = NOT_VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.REMOVE to events[0],
            )
        )
    }

    @Test
    fun `given in date message event when processing then keep message event`() {
        val events = listOf(aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID))
        mockkOutdatedDetector.givenEventIsInDate(events[0])
        val eventProcessor = createProcessor(navigationState = NOT_VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.KEEP to events[0],
            )
        )
    }

    @Test
    fun `given viewing the same room main timeline when processing main timeline message event then removes message`() {
        val events = listOf(aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID, threadId = null))
        events.forEach { mockkOutdatedDetector.givenEventIsOutOfDate(it) }
        val eventProcessor = createProcessor(isInForeground = true, navigationState = VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.REMOVE to events[0],
            )
        )
    }

    @Test
    fun `given viewing the same thread timeline when processing thread message event then removes message`() {
        val events = listOf(aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID, threadId = A_THREAD_ID))
        events.forEach { mockkOutdatedDetector.givenEventIsOutOfDate(it) }
        val eventProcessor = createProcessor(isInForeground = true, navigationState = VIEWING_A_THREAD)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.REMOVE to events[0],
            )
        )
    }

    @Test
    fun `given viewing main timeline of the same room when processing thread timeline message event then keep message`() {
        val events = listOf(aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID, threadId = A_THREAD_ID))
        mockkOutdatedDetector.givenEventIsInDate(events[0])
        val eventProcessor = createProcessor(isInForeground = true, navigationState = VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.KEEP to events[0],
            )
        )
    }

    @Test
    fun `given viewing thread timeline of the same room when processing main timeline message event then keep message`() {
        val events = listOf(aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID))
        mockkOutdatedDetector.givenEventIsInDate(events[0])
        val eventProcessor = createProcessor(isInForeground = true, navigationState = VIEWING_A_THREAD)

        val result = eventProcessor.process(events, renderedEvents = emptyList())

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.KEEP to events[0],
            )
        )
    }

    @Test
    fun `given events are different to rendered events when processing then removes difference`() {
        val events = listOf(aSimpleNotifiableEvent(eventId = AN_EVENT_ID))
        val renderedEvents = listOf<ProcessedEvent<NotifiableEvent>>(
            ProcessedEvent(ProcessedEvent.Type.KEEP, events[0]),
            ProcessedEvent(ProcessedEvent.Type.KEEP, anInviteNotifiableEvent(eventId = AN_EVENT_ID_2))
        )
        val eventProcessor = createProcessor(navigationState = NOT_VIEWING_A_ROOM)

        val result = eventProcessor.process(events, renderedEvents = renderedEvents)

        assertThat(result).isEqualTo(
            listOfProcessedEvents(
                ProcessedEvent.Type.REMOVE to renderedEvents[1].event,
                ProcessedEvent.Type.KEEP to renderedEvents[0].event
            )
        )
    }

    private fun listOfProcessedEvents(vararg event: Pair<ProcessedEvent.Type, NotifiableEvent>) = event.map {
        ProcessedEvent(it.first, it.second)
    }

    private fun createProcessor(
        isInForeground: Boolean = false,
        navigationState: NavigationState
    ): NotifiableEventProcessor {
        return NotifiableEventProcessor(
            outdatedDetector = mockkOutdatedDetector.instance,
            appNavigationStateService = FakeAppNavigationStateService(MutableStateFlow(AppNavigationState(navigationState, isInForeground))),
        )
    }
}
