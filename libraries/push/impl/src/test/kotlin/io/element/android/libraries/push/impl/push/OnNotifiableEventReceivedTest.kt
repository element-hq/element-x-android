/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.test.notifications.conversations.FakeNotificationConversationService
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OnNotifiableEventReceivedTest {
    @Test
    fun `pushes a shortcut for a new incoming message`() = runTest {
        val onMessageReceived = lambdaRecorder<SessionId, RoomId, String?, Boolean, String?, Unit> { _, _, _, _, _ -> }
        val service = FakeNotificationConversationService(onMessageReceivedLambda = onMessageReceived)

        service.pushShortcutsForIncomingMessages(listOf(aNotifiableMessageEvent(roomId = A_ROOM_ID)))

        onMessageReceived.assertions().isCalledOnce().with(
            value(A_SESSION_ID),
            value(A_ROOM_ID),
            value(A_ROOM_NAME),
            value(false),
            value(null),
        )
    }

    @Test
    fun `skips outgoing messages`() = runTest {
        val onMessageReceived = lambdaRecorder<SessionId, RoomId, String?, Boolean, String?, Unit> { _, _, _, _, _ -> }
        val service = FakeNotificationConversationService(onMessageReceivedLambda = onMessageReceived)
        val outgoing = aNotifiableMessageEvent(roomId = A_ROOM_ID).copy(outGoingMessage = true)

        service.pushShortcutsForIncomingMessages(listOf(outgoing))

        onMessageReceived.assertions().isNeverCalled()
    }

    @Test
    fun `skips thread messages`() = runTest {
        val onMessageReceived = lambdaRecorder<SessionId, RoomId, String?, Boolean, String?, Unit> { _, _, _, _, _ -> }
        val service = FakeNotificationConversationService(onMessageReceivedLambda = onMessageReceived)
        val threadEvent = aNotifiableMessageEvent(roomId = A_ROOM_ID, threadId = ThreadId("\$a-thread-id"))

        service.pushShortcutsForIncomingMessages(listOf(threadEvent))

        onMessageReceived.assertions().isNeverCalled()
    }

    @Test
    fun `dedupes multiple events for the same room`() = runTest {
        val onMessageReceived = lambdaRecorder<SessionId, RoomId, String?, Boolean, String?, Unit> { _, _, _, _, _ -> }
        val service = FakeNotificationConversationService(onMessageReceivedLambda = onMessageReceived)

        service.pushShortcutsForIncomingMessages(
            listOf(
                aNotifiableMessageEvent(roomId = A_ROOM_ID),
                aNotifiableMessageEvent(roomId = A_ROOM_ID),
                aNotifiableMessageEvent(roomId = A_ROOM_ID_2),
            )
        )

        onMessageReceived.assertions().isCalledExactly(2)
    }
}
