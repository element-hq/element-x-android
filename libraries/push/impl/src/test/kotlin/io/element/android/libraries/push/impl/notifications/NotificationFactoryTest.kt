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
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeImageLoader
import io.element.android.libraries.push.impl.notifications.fake.FakeRoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeSummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.anInviteNotifiableEvent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private val MY_AVATAR_URL: String? = null
private val AN_INVITATION_EVENT = anInviteNotifiableEvent(roomId = A_ROOM_ID)
private val A_SIMPLE_EVENT = aSimpleNotifiableEvent(eventId = AN_EVENT_ID)
private val A_MESSAGE_EVENT = aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID)

@RunWith(RobolectricTestRunner::class)
class NotificationFactoryTest {
    private val androidNotificationFactory = FakeNotificationCreator()
    private val roomGroupMessageCreator = FakeRoomGroupMessageCreator()
    private val summaryGroupMessageCreator = FakeSummaryGroupMessageCreator()

    private val notificationFactory = NotificationFactory(
        androidNotificationFactory.instance,
        roomGroupMessageCreator.instance,
        summaryGroupMessageCreator.instance
    )

    @Test
    fun `given a room invitation when mapping to notification then is Append`() = testWith(notificationFactory) {
        val expectedNotification = androidNotificationFactory.givenCreateRoomInvitationNotificationFor(AN_INVITATION_EVENT)
        val roomInvitation = listOf(ProcessedEvent(ProcessedEvent.Type.KEEP, AN_INVITATION_EVENT))

        val result = roomInvitation.toNotifications()

        assertThat(result).isEqualTo(
            listOf(
                OneShotNotification.Append(
                    notification = expectedNotification,
                    meta = OneShotNotification.Append.Meta(
                        key = A_ROOM_ID.value,
                        summaryLine = AN_INVITATION_EVENT.description,
                        isNoisy = AN_INVITATION_EVENT.noisy,
                        timestamp = AN_INVITATION_EVENT.timestamp
                    )
                )
            )
        )
    }

    @Test
    fun `given a missing event in room invitation when mapping to notification then is Removed`() = testWith(notificationFactory) {
        val missingEventRoomInvitation = listOf(ProcessedEvent(ProcessedEvent.Type.REMOVE, AN_INVITATION_EVENT))

        val result = missingEventRoomInvitation.toNotifications()

        assertThat(result).isEqualTo(
            listOf(
                OneShotNotification.Removed(
                    key = A_ROOM_ID.value
                )
            )
        )
    }

    @Test
    fun `given a simple event when mapping to notification then is Append`() = testWith(notificationFactory) {
        val expectedNotification = androidNotificationFactory.givenCreateSimpleInvitationNotificationFor(A_SIMPLE_EVENT)
        val roomInvitation = listOf(ProcessedEvent(ProcessedEvent.Type.KEEP, A_SIMPLE_EVENT))

        val result = roomInvitation.toNotifications()

        assertThat(result).isEqualTo(
            listOf(
                OneShotNotification.Append(
                    notification = expectedNotification,
                    meta = OneShotNotification.Append.Meta(
                        key = AN_EVENT_ID.value,
                        summaryLine = A_SIMPLE_EVENT.description,
                        isNoisy = A_SIMPLE_EVENT.noisy,
                        timestamp = AN_INVITATION_EVENT.timestamp
                    )
                )
            )
        )
    }

    @Test
    fun `given a missing simple event when mapping to notification then is Removed`() = testWith(notificationFactory) {
        val missingEventRoomInvitation = listOf(ProcessedEvent(ProcessedEvent.Type.REMOVE, A_SIMPLE_EVENT))

        val result = missingEventRoomInvitation.toNotifications()

        assertThat(result).isEqualTo(
            listOf(
                OneShotNotification.Removed(
                    key = AN_EVENT_ID.value
                )
            )
        )
    }

    @Test
    fun `given room with message when mapping to notification then delegates to room group message creator`() = testWith(notificationFactory) {
        val events = listOf(A_MESSAGE_EVENT)
        val expectedNotification = roomGroupMessageCreator.givenCreatesRoomMessageFor(
            MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            events,
            A_ROOM_ID
        )
        val roomWithMessage = mapOf(A_ROOM_ID to listOf(ProcessedEvent(ProcessedEvent.Type.KEEP, A_MESSAGE_EVENT)))

        val fakeImageLoader = FakeImageLoader()
        val result = roomWithMessage.toNotifications(
            currentUser = MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            imageLoader = fakeImageLoader.getImageLoader(),
        )

        assertThat(result).isEqualTo(listOf(expectedNotification))
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `given a room with no events to display when mapping to notification then is Empty`() = testWith(notificationFactory) {
        val events = listOf(ProcessedEvent(ProcessedEvent.Type.REMOVE, A_MESSAGE_EVENT))
        val emptyRoom = mapOf(A_ROOM_ID to events)

        val fakeImageLoader = FakeImageLoader()
        val result = emptyRoom.toNotifications(
            currentUser = MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            imageLoader = fakeImageLoader.getImageLoader(),
        )

        assertThat(result).isEqualTo(
            listOf(
                RoomNotification.Removed(
                    roomId = A_ROOM_ID
                )
            )
        )
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `given a room with only redacted events when mapping to notification then is Empty`() = testWith(notificationFactory) {
        val redactedRoom = mapOf(A_ROOM_ID to listOf(ProcessedEvent(ProcessedEvent.Type.KEEP, A_MESSAGE_EVENT.copy(isRedacted = true))))

        val fakeImageLoader = FakeImageLoader()
        val result = redactedRoom.toNotifications(
            currentUser = MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            imageLoader = fakeImageLoader.getImageLoader(),
        )

        assertThat(result).isEqualTo(
            listOf(
                RoomNotification.Removed(
                    roomId = A_ROOM_ID
                )
            )
        )
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `given a room with redacted and non redacted message events when mapping to notification then redacted events are removed`() = testWith(
        notificationFactory
    ) {
        val roomWithRedactedMessage = mapOf(
            A_ROOM_ID to listOf(
                ProcessedEvent(ProcessedEvent.Type.KEEP, A_MESSAGE_EVENT.copy(isRedacted = true)),
                ProcessedEvent(ProcessedEvent.Type.KEEP, A_MESSAGE_EVENT.copy(eventId = EventId("\$not-redacted")))
            )
        )
        val withRedactedRemoved = listOf(A_MESSAGE_EVENT.copy(eventId = EventId("\$not-redacted")))
        val expectedNotification = roomGroupMessageCreator.givenCreatesRoomMessageFor(
            MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            withRedactedRemoved,
            A_ROOM_ID,
        )

        val fakeImageLoader = FakeImageLoader()
        val result = roomWithRedactedMessage.toNotifications(
            currentUser = MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            imageLoader = fakeImageLoader.getImageLoader(),
        )

        assertThat(result).isEqualTo(listOf(expectedNotification))
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }
}

fun <T> testWith(receiver: T, block: suspend T.() -> Unit) {
    runTest {
        receiver.block()
    }
}
