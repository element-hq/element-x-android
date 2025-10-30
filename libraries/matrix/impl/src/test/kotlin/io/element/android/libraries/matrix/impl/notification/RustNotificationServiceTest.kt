/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.notification

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.exception.NotificationResolverException
import io.element.android.libraries.matrix.api.notification.NotificationContent
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustBatchNotificationResult
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustNotificationEventTimeline
import io.element.android.libraries.matrix.impl.fixtures.factories.aRustNotificationItem
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiNotificationClient
import io.element.android.libraries.matrix.impl.fixtures.fakes.FakeFfiTimelineEvent
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Ignore
import org.junit.Test
import org.matrix.rustcomponents.sdk.NotificationClient
import org.matrix.rustcomponents.sdk.NotificationStatus
import org.matrix.rustcomponents.sdk.TimelineEventType

class RustNotificationServiceTest {
    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun test() = runTest {
        val notificationClient = FakeFfiNotificationClient(
            notificationItemResult = mapOf(AN_EVENT_ID.value to aRustBatchNotificationResult()),
        )
        val sut = createRustNotificationService(
            notificationClient = notificationClient,
        )
        val result = sut.getNotifications(mapOf(A_ROOM_ID to listOf(AN_EVENT_ID))).getOrThrow()[AN_EVENT_ID]!!.getOrThrow()
        assertThat(result.isEncrypted).isTrue()
        assertThat(result.content).isEqualTo(
            NotificationContent.MessageLike.RoomMessage(
                senderId = A_USER_ID_2,
                messageType = TextMessageType(
                    body = A_MESSAGE,
                    formatted = null,
                )
            )
        )
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `test mapping invalid item only drops that item`() = runTest {
        val error = IllegalStateException("This event type is not supported")
        val faultyEvent = object : FakeFfiTimelineEvent() {
            override fun eventType(): TimelineEventType {
                throw error
            }
        }
        val notificationClient = FakeFfiNotificationClient(
            notificationItemResult = mapOf(
                AN_EVENT_ID.value to aRustBatchNotificationResult(
                    notificationStatus = NotificationStatus.Event(aRustNotificationItem(aRustNotificationEventTimeline(faultyEvent)))
                ),
                AN_EVENT_ID_2.value to aRustBatchNotificationResult()
            ),
        )
        val sut = createRustNotificationService(
            notificationClient = notificationClient,
        )
        val result = sut.getNotifications(mapOf(A_ROOM_ID to listOf(AN_EVENT_ID, AN_EVENT_ID_2))).getOrThrow()
        val exception = result[AN_EVENT_ID]!!.exceptionOrNull()
        assertThat(exception).isEqualTo(error)

        val successfulResult = result[AN_EVENT_ID_2]
        assertThat(successfulResult?.isSuccess).isTrue()
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `test unable to resolve event`() = runTest {
        val notificationClient = FakeFfiNotificationClient(
            notificationItemResult = emptyMap(),
        )
        val sut = createRustNotificationService(
            notificationClient = notificationClient,
        )
        val exception = sut.getNotifications(mapOf(A_ROOM_ID to listOf(AN_EVENT_ID))).getOrThrow()[AN_EVENT_ID]!!.exceptionOrNull()
        assertThat(exception).isInstanceOf(NotificationResolverException::class.java)
    }

    @Ignore("JNA direct mapping has broken unit tests with FFI fakes")
    @Test
    fun `close should invoke the close method of the service`() = runTest {
        val closeResult = lambdaRecorder<Unit> { }
        val notificationClient = FakeFfiNotificationClient(
            closeResult = closeResult,
        )
        val sut = createRustNotificationService(
            notificationClient = notificationClient,
        )
        sut.close()
        closeResult.assertions().isCalledOnce()
    }

    private fun TestScope.createRustNotificationService(
        notificationClient: NotificationClient = FakeFfiNotificationClient(),
        clock: SystemClock = FakeSystemClock(),
    ) =
        RustNotificationService(
            sessionId = A_SESSION_ID,
            notificationClient = notificationClient,
            dispatchers = testCoroutineDispatchers(),
            clock = clock,
        )
}
