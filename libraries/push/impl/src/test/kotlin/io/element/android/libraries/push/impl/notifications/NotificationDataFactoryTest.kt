/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fake.FakeActiveNotificationsProvider
import io.element.android.libraries.push.impl.notifications.fake.FakeNotificationCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeRoomGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fake.FakeSummaryGroupMessageCreator
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aSimpleNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.anInviteNotifiableEvent
import io.element.android.libraries.push.test.notifications.FakeImageLoader
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private val MY_AVATAR_URL: String? = null

private val AN_INVITATION_EVENT = anInviteNotifiableEvent(roomId = A_ROOM_ID)
private val A_SIMPLE_EVENT = aSimpleNotifiableEvent(eventId = AN_EVENT_ID)
private val A_MESSAGE_EVENT = aNotifiableMessageEvent(eventId = AN_EVENT_ID, roomId = A_ROOM_ID)

@RunWith(RobolectricTestRunner::class)
class NotificationDataFactoryTest {
    private val notificationCreator = FakeNotificationCreator()
    private val fakeRoomGroupMessageCreator = FakeRoomGroupMessageCreator()
    private val fakeSummaryGroupMessageCreator = FakeSummaryGroupMessageCreator()
    private val activeNotificationsProvider = FakeActiveNotificationsProvider()

    private val notificationDataFactory = DefaultNotificationDataFactory(
        notificationCreator = notificationCreator,
        roomGroupMessageCreator = fakeRoomGroupMessageCreator,
        summaryGroupMessageCreator = fakeSummaryGroupMessageCreator,
        activeNotificationsProvider = activeNotificationsProvider,
        stringProvider = FakeStringProvider(),
    )

    @Test
    fun `given a room invitation when mapping to notification then it's added`() = testWith(notificationDataFactory) {
        val expectedNotification = notificationCreator.createRoomInvitationNotificationResult(AN_INVITATION_EVENT)
        val roomInvitation = listOf(AN_INVITATION_EVENT)

        val result = toNotifications(roomInvitation)

        assertThat(result).isEqualTo(
            listOf(
                OneShotNotification(
                    notification = expectedNotification,
                    key = A_ROOM_ID.value,
                    summaryLine = AN_INVITATION_EVENT.description,
                    isNoisy = AN_INVITATION_EVENT.noisy,
                    timestamp = AN_INVITATION_EVENT.timestamp
                )
            )
        )
    }

    @Test
    fun `given a simple event when mapping to notification then it's added`() = testWith(notificationDataFactory) {
        val expectedNotification = notificationCreator.createRoomInvitationNotificationResult(AN_INVITATION_EVENT)
        val roomInvitation = listOf(A_SIMPLE_EVENT)

        val result = toNotifications(roomInvitation)

        assertThat(result).isEqualTo(
            listOf(
                OneShotNotification(
                    notification = expectedNotification,
                    key = AN_EVENT_ID.value,
                    summaryLine = A_SIMPLE_EVENT.description,
                    isNoisy = A_SIMPLE_EVENT.noisy,
                    timestamp = AN_INVITATION_EVENT.timestamp
                )
            )
        )
    }

    @Test
    fun `given room with message when mapping to notification then delegates to room group message creator`() = testWith(notificationDataFactory) {
        val events = listOf(A_MESSAGE_EVENT)
        val expectedNotification = RoomNotification(
            notification = fakeRoomGroupMessageCreator.createRoomMessage(
                MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
                events,
                A_ROOM_ID,
                FakeImageLoader().getImageLoader(),
                null,
            ),
            roomId = A_ROOM_ID,
            summaryLine = "A room name: Bob Hello world!",
            messageCount = events.size,
            latestTimestamp = events.maxOf { it.timestamp },
            shouldBing = events.any { it.noisy }
        )
        val roomWithMessage = listOf(A_MESSAGE_EVENT)

        val fakeImageLoader = FakeImageLoader()
        val result = toNotifications(
            messages = roomWithMessage,
            currentUser = MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            imageLoader = fakeImageLoader.getImageLoader(),
        )

        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().isDataEqualTo(expectedNotification)).isTrue()
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `given a room with only redacted events when mapping to notification then is Empty`() = testWith(notificationDataFactory) {
        val redactedRoom = listOf(A_MESSAGE_EVENT.copy(isRedacted = true))

        val fakeImageLoader = FakeImageLoader()
        val result = toNotifications(
            messages = redactedRoom,
            currentUser = MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            imageLoader = fakeImageLoader.getImageLoader(),
        )

        assertThat(result).isEmpty()
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }

    @Test
    fun `given a room with redacted and non redacted message events when mapping to notification then redacted events are removed`() = testWith(
        notificationDataFactory
    ) {
        val roomWithRedactedMessage = listOf(
            A_MESSAGE_EVENT.copy(isRedacted = true),
            A_MESSAGE_EVENT.copy(eventId = EventId("\$not-redacted")),
        )
        val withRedactedRemoved = listOf(A_MESSAGE_EVENT.copy(eventId = EventId("\$not-redacted")))
        val expectedNotification = RoomNotification(
            notification = fakeRoomGroupMessageCreator.createRoomMessage(
                MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
                withRedactedRemoved,
                A_ROOM_ID,
                FakeImageLoader().getImageLoader(),
                null,
            ),
            roomId = A_ROOM_ID,
            summaryLine = "A room name: Bob Hello world!",
            messageCount = withRedactedRemoved.size,
            latestTimestamp = withRedactedRemoved.maxOf { it.timestamp },
            shouldBing = withRedactedRemoved.any { it.noisy }
        )

        val fakeImageLoader = FakeImageLoader()
        val result = toNotifications(
            messages = roomWithRedactedMessage,
            currentUser = MatrixUser(A_SESSION_ID, A_SESSION_ID.value, MY_AVATAR_URL),
            imageLoader = fakeImageLoader.getImageLoader(),
        )

        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().isDataEqualTo(expectedNotification)).isTrue()
        assertThat(fakeImageLoader.getCoilRequests().size).isEqualTo(0)
    }
}

fun <T> testWith(receiver: T, block: suspend T.() -> Unit) {
    runTest {
        receiver.block()
    }
}
