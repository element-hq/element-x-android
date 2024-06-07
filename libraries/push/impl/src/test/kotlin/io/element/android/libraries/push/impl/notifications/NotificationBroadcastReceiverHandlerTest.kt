/*
 * Copyright (c) 2024 New Vector Ltd
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

import android.content.Intent
import com.google.common.truth.Truth.assertThat
import io.element.android.features.preferences.api.store.SessionPreferencesStore
import io.element.android.features.preferences.api.store.SessionPreferencesStoreFactory
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.asEventId
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_MESSAGE
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.preferences.test.FakeSessionPreferencesStoreFactory
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.libraries.push.api.notifications.NotificationDrawerManager
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.push.FakeOnNotifiableEventReceived
import io.element.android.libraries.push.impl.push.OnNotifiableEventReceived
import io.element.android.libraries.push.test.notifications.FakeNotificationDrawerManager
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class NotificationBroadcastReceiverHandlerTest {
    private val actionIds = NotificationActionIds(aBuildMeta())

    @Test
    fun `When no sessionId, nothing happen`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.join,
                sessionId = null
            ),
        )
    }

    @Test
    fun `Test dismiss room without a roomId, nothing happen`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.dismissRoom,
            ),
        )
    }

    @Test
    fun `Test dismiss room`() = runTest {
        val clearMessagesForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
        val notificationDrawerManager = FakeNotificationDrawerManager(
            clearMessagesForRoomLambda = clearMessagesForRoomLambda,
        )
        val sut = createNotificationBroadcastReceiverHandler(
            notificationDrawerManager = notificationDrawerManager
        )
        sut.onReceive(
            createIntent(
                action = actionIds.dismissRoom,
                roomId = A_ROOM_ID,
            ),
        )
        runCurrent()
        clearMessagesForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
    }

    @Test
    fun `Test dismiss summary`() = runTest {
        val clearAllMessagesEventsLambda = lambdaRecorder<SessionId, Unit> { _ -> }
        val notificationDrawerManager = FakeNotificationDrawerManager(
            clearAllMessagesEventsLambda = clearAllMessagesEventsLambda,
        )
        val sut = createNotificationBroadcastReceiverHandler(
            notificationDrawerManager = notificationDrawerManager
        )
        sut.onReceive(
            createIntent(
                action = actionIds.dismissSummary,
            ),
        )
        clearAllMessagesEventsLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID))
    }

    @Test
    fun `Test dismiss Invite without room`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.dismissInvite,
            ),
        )
    }

    @Test
    fun `Test dismiss Invite`() = runTest {
        val clearMembershipNotificationForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
        val notificationDrawerManager = FakeNotificationDrawerManager(
            clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda,
        )
        val sut = createNotificationBroadcastReceiverHandler(
            notificationDrawerManager = notificationDrawerManager
        )
        sut.onReceive(
            createIntent(
                action = actionIds.dismissInvite,
                roomId = A_ROOM_ID,
            ),
        )
        clearMembershipNotificationForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
    }

    @Test
    fun `Test dismiss Event without event`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.dismissEvent,
            ),
        )
    }

    @Test
    fun `Test dismiss Event`() = runTest {
        val clearEventLambda = lambdaRecorder<SessionId, EventId, Unit> { _, _ -> }
        val notificationDrawerManager = FakeNotificationDrawerManager(
            clearEventLambda = clearEventLambda,
        )
        val sut = createNotificationBroadcastReceiverHandler(
            notificationDrawerManager = notificationDrawerManager
        )
        sut.onReceive(
            createIntent(
                action = actionIds.dismissEvent,
                eventId = AN_EVENT_ID,
            ),
        )
        clearEventLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(AN_EVENT_ID))
    }

    @Test
    fun `Test mark room as read without room`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.markRoomRead,
            ),
        )
    }

    @Test
    fun `Test mark room as read, send public RR`() {
        testMarkRoomAsRead(
            isSendPublicReadReceiptsEnabled = true,
            expectedReceiptType = ReceiptType.READ
        )
    }

    @Test
    fun `Test mark room as read, send private RR`() {
        testMarkRoomAsRead(
            isSendPublicReadReceiptsEnabled = false,
            expectedReceiptType = ReceiptType.READ_PRIVATE
        )
    }

    private fun testMarkRoomAsRead(
        isSendPublicReadReceiptsEnabled: Boolean,
        expectedReceiptType: ReceiptType,
    ) = runTest {
        val getLambda = lambdaRecorder<SessionId, CoroutineScope, SessionPreferencesStore> { _, _ ->
            InMemorySessionPreferencesStore(
                isSendPublicReadReceiptsEnabled = isSendPublicReadReceiptsEnabled
            )
        }
        val sessionPreferencesStore = FakeSessionPreferencesStoreFactory(
            getLambda = getLambda
        )
        val clearMessagesForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
        val matrixRoom = FakeMatrixRoom()
        val notificationDrawerManager = FakeNotificationDrawerManager(
            clearMessagesForRoomLambda = clearMessagesForRoomLambda,
        )
        val sut = createNotificationBroadcastReceiverHandler(
            sessionPreferencesStore = sessionPreferencesStore,
            matrixRoom = matrixRoom,
            notificationDrawerManager = notificationDrawerManager
        )
        sut.onReceive(
            createIntent(
                action = actionIds.markRoomRead,
                roomId = A_ROOM_ID,
            ),
        )
        runCurrent()
        clearMessagesForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
        assertThat(matrixRoom.markAsReadCalls).isEqualTo(listOf(expectedReceiptType))
    }

    @Test
    fun `Test join room without room`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.join,
            ),
        )
    }

    @Test
    fun `Test join room`() = runTest {
        val joinRoom = lambdaRecorder<RoomId, Result<Unit>> { _ -> Result.success(Unit) }
        val clearMembershipNotificationForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
        val notificationDrawerManager = FakeNotificationDrawerManager(
            clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda,
        )
        val sut = createNotificationBroadcastReceiverHandler(
            joinRoom = joinRoom,
            notificationDrawerManager = notificationDrawerManager,
        )
        sut.onReceive(
            createIntent(
                action = actionIds.join,
                roomId = A_ROOM_ID,
            ),
        )
        runCurrent()
        joinRoom.assertions()
            .isCalledOnce()
            .with(value(A_ROOM_ID))
        clearMembershipNotificationForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
    }

    @Test
    fun `Test reject room without room`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.reject,
            ),
        )
    }

    @Test
    fun `Test reject room`() = runTest {
        val leaveRoom = lambdaRecorder<Result<Unit>> { Result.success(Unit) }
        val matrixRoom = FakeMatrixRoom().apply {
            leaveRoomLambda = leaveRoom
        }
        val clearMembershipNotificationForRoomLambda = lambdaRecorder<SessionId, RoomId, Unit> { _, _ -> }
        val notificationDrawerManager = FakeNotificationDrawerManager(
            clearMembershipNotificationForRoomLambda = clearMembershipNotificationForRoomLambda,
        )
        val sut = createNotificationBroadcastReceiverHandler(
            matrixRoom = matrixRoom,
            notificationDrawerManager = notificationDrawerManager
        )
        sut.onReceive(
            createIntent(
                action = actionIds.reject,
                roomId = A_ROOM_ID,
            ),
        )
        runCurrent()
        clearMembershipNotificationForRoomLambda.assertions()
            .isCalledOnce()
            .with(value(A_SESSION_ID), value(A_ROOM_ID))
        leaveRoom.assertions()
            .isCalledOnce()
            .with()
    }

    @Test
    fun `Test send reply without room`() = runTest {
        val sut = createNotificationBroadcastReceiverHandler()
        sut.onReceive(
            createIntent(
                action = actionIds.smartReply,
            ),
        )
    }

    @Test
    fun `Test send reply`() = runTest {
        val sendMessage = lambdaRecorder<String, String?, List<Mention>, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val replyMessage = lambdaRecorder<EventId, String, String?, List<Mention>, Boolean, Result<Unit>> { _, _, _, _, _ -> Result.success(Unit) }
        val liveTimeline = FakeTimeline().apply {
            sendMessageLambda = sendMessage
            replyMessageLambda = replyMessage
        }
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        val onNotifiableEventReceivedResult = lambdaRecorder<NotifiableEvent, Unit> { _ -> }
        val onNotifiableEventReceived = FakeOnNotifiableEventReceived(onNotifiableEventReceivedResult = onNotifiableEventReceivedResult)
        val sut = createNotificationBroadcastReceiverHandler(
            matrixRoom = matrixRoom,
            onNotifiableEventReceived = onNotifiableEventReceived,
            replyMessageExtractor = FakeReplyMessageExtractor(A_MESSAGE)
        )
        sut.onReceive(
            createIntent(
                action = actionIds.smartReply,
                roomId = A_ROOM_ID,
            ),
        )
        runCurrent()
        sendMessage.assertions()
            .isCalledOnce()
            .with(value(A_MESSAGE), value(null), value(emptyList<Mention>()))
        onNotifiableEventReceivedResult.assertions()
            .isCalledOnce()
        replyMessage.assertions()
            .isNeverCalled()
    }

    @Test
    fun `Test send reply blank message`() = runTest {
        val sendMessage = lambdaRecorder<String, String?, List<Mention>, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val liveTimeline = FakeTimeline().apply {
            sendMessageLambda = sendMessage
        }
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        val sut = createNotificationBroadcastReceiverHandler(
            matrixRoom = matrixRoom,
            replyMessageExtractor = FakeReplyMessageExtractor("   "),
        )
        sut.onReceive(
            createIntent(
                action = actionIds.smartReply,
                roomId = A_ROOM_ID,
            ),
        )
        runCurrent()
        sendMessage.assertions()
            .isNeverCalled()
    }

    @Test
    fun `Test send reply to thread`() = runTest {
        val sendMessage = lambdaRecorder<String, String?, List<Mention>, Result<Unit>> { _, _, _ -> Result.success(Unit) }
        val replyMessage = lambdaRecorder<EventId, String, String?, List<Mention>, Boolean, Result<Unit>> { _, _, _, _, _ -> Result.success(Unit) }
        val liveTimeline = FakeTimeline().apply {
            sendMessageLambda = sendMessage
            replyMessageLambda = replyMessage
        }
        val matrixRoom = FakeMatrixRoom(
            liveTimeline = liveTimeline
        )
        val onNotifiableEventReceivedResult = lambdaRecorder<NotifiableEvent, Unit> { _ -> }
        val onNotifiableEventReceived = FakeOnNotifiableEventReceived(onNotifiableEventReceivedResult = onNotifiableEventReceivedResult)
        val sut = createNotificationBroadcastReceiverHandler(
            matrixRoom = matrixRoom,
            onNotifiableEventReceived = onNotifiableEventReceived,
            replyMessageExtractor = FakeReplyMessageExtractor(A_MESSAGE)
        )
        sut.onReceive(
            createIntent(
                action = actionIds.smartReply,
                roomId = A_ROOM_ID,
                threadId = A_THREAD_ID,
            ),
        )
        runCurrent()
        sendMessage.assertions()
            .isNeverCalled()
        onNotifiableEventReceivedResult.assertions()
            .isCalledOnce()
        replyMessage.assertions()
            .isCalledOnce()
            .with(value(A_THREAD_ID.asEventId()), value(A_MESSAGE), value(null), value(emptyList<Mention>()), value(true))
    }

    private fun createIntent(
        action: String,
        sessionId: SessionId? = A_SESSION_ID,
        roomId: RoomId? = null,
        eventId: EventId? = null,
        threadId: ThreadId? = null,
    ) = Intent(action).apply {
        putExtra(NotificationBroadcastReceiver.KEY_SESSION_ID, sessionId?.value)
        putExtra(NotificationBroadcastReceiver.KEY_ROOM_ID, roomId?.value)
        putExtra(NotificationBroadcastReceiver.KEY_THREAD_ID, threadId?.value)
        putExtra(NotificationBroadcastReceiver.KEY_EVENT_ID, eventId?.value)
    }

    private fun TestScope.createNotificationBroadcastReceiverHandler(
        matrixRoom: FakeMatrixRoom? = FakeMatrixRoom(),
        joinRoom: (RoomId) -> Result<Unit> = { lambdaError() },
        matrixClient: MatrixClient? = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, matrixRoom)
            joinRoomLambda = joinRoom
        },
        sessionPreferencesStore: SessionPreferencesStoreFactory = FakeSessionPreferencesStoreFactory(),
        notificationDrawerManager: NotificationDrawerManager = FakeNotificationDrawerManager(),
        systemClock: SystemClock = FakeSystemClock(),
        onNotifiableEventReceived: OnNotifiableEventReceived = FakeOnNotifiableEventReceived(),
        stringProvider: StringProvider = FakeStringProvider(),
        replyMessageExtractor: ReplyMessageExtractor = FakeReplyMessageExtractor(),
    ): NotificationBroadcastReceiverHandler {
        return NotificationBroadcastReceiverHandler(
            appCoroutineScope = this,
            matrixClientProvider = FakeMatrixClientProvider {
                if (matrixClient == null) {
                    Result.failure(Exception("No matrix client"))
                } else {
                    Result.success(matrixClient)
                }
            },
            sessionPreferencesStore = sessionPreferencesStore,
            notificationDrawerManager = notificationDrawerManager,
            actionIds = actionIds,
            systemClock = systemClock,
            onNotifiableEventReceived = onNotifiableEventReceived,
            stringProvider = stringProvider,
            replyMessageExtractor = replyMessageExtractor,
        )
    }
}
