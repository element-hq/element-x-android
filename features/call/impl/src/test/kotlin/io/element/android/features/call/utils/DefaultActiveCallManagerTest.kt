/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.utils

import androidx.core.app.NotificationManagerCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.features.call.impl.utils.ActiveCall
import io.element.android.features.call.impl.utils.CallState
import io.element.android.features.call.impl.utils.DefaultActiveCallManager
import io.element.android.features.call.test.aCallNotificationData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.test.notifications.FakeImageLoaderHolder
import io.element.android.libraries.push.test.notifications.FakeOnMissedCallNotificationHandler
import io.element.android.libraries.push.test.notifications.push.FakeNotificationBitmapLoader
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultActiveCallManagerTest {
    private val notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.INCOMING_CALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `registerIncomingCall - sets the incoming call as active`() = runTest {
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        inCancellableScope {
            val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat)

            assertThat(manager.activeCall.value).isNull()

            val callNotificationData = aCallNotificationData()
            manager.registerIncomingCall(callNotificationData)

            assertThat(manager.activeCall.value).isEqualTo(
                ActiveCall(
                    callType = CallType.RoomCall(
                        sessionId = callNotificationData.sessionId,
                        roomId = callNotificationData.roomId,
                    ),
                    callState = CallState.Ringing(callNotificationData)
                )
            )

            runCurrent()

            verify { notificationManagerCompat.notify(notificationId, any()) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `registerIncomingCall - when there is an already active call adds missed call notification`() = runTest {
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        val onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda)
        inCancellableScope {
            val manager = createActiveCallManager(
                onMissedCallNotificationHandler = onMissedCallNotificationHandler,
            )

            // Register existing call
            val callNotificationData = aCallNotificationData()
            manager.registerIncomingCall(callNotificationData)
            val activeCall = manager.activeCall.value

            // Now add a new call
            manager.registerIncomingCall(aCallNotificationData(roomId = A_ROOM_ID_2))

            assertThat(manager.activeCall.value).isEqualTo(activeCall)
            assertThat((manager.activeCall.value?.callType as? CallType.RoomCall)?.roomId).isNotEqualTo(A_ROOM_ID_2)

            advanceTimeBy(1)

            addMissedCallNotificationLambda.assertions()
                .isCalledOnce()
                .with(value(A_SESSION_ID), value(A_ROOM_ID_2), value(AN_EVENT_ID))
        }
    }

    @Test
    fun `incomingCallTimedOut - when there isn't an active call does nothing`() = runTest {
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        inCancellableScope {
            val manager = createActiveCallManager(
                onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda)
            )

            manager.incomingCallTimedOut()

            addMissedCallNotificationLambda.assertions().isNeverCalled()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `incomingCallTimedOut - when there is an active call removes it and adds a missed call notification`() = runTest {
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        inCancellableScope {
            val manager = createActiveCallManager(
                onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda),
                notificationManagerCompat = notificationManagerCompat,
            )

            manager.registerIncomingCall(aCallNotificationData())
            assertThat(manager.activeCall.value).isNotNull()

            manager.incomingCallTimedOut()
            advanceTimeBy(1)

            assertThat(manager.activeCall.value).isNull()
            addMissedCallNotificationLambda.assertions().isCalledOnce()
            verify { notificationManagerCompat.cancel(notificationId) }
        }
    }

    @Test
    fun `hungUpCall - removes existing call if the CallType matches`() = runTest {
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        // Create a cancellable coroutine scope to cancel the test when needed
        inCancellableScope {
            val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat)

            val notificationData = aCallNotificationData()
            manager.registerIncomingCall(notificationData)
            assertThat(manager.activeCall.value).isNotNull()

            manager.hungUpCall(CallType.RoomCall(notificationData.sessionId, notificationData.roomId))
            assertThat(manager.activeCall.value).isNull()

            verify { notificationManagerCompat.cancel(notificationId) }
        }
    }

    @Test
    fun `hungUpCall - does nothing if the CallType doesn't match`() = runTest {
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        // Create a cancellable coroutine scope to cancel the test when needed
        inCancellableScope {
            val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat)

            manager.registerIncomingCall(aCallNotificationData())
            assertThat(manager.activeCall.value).isNotNull()

            manager.hungUpCall(CallType.ExternalUrl("https://example.com"))
            assertThat(manager.activeCall.value).isNotNull()

            verify(exactly = 0) { notificationManagerCompat.cancel(notificationId) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `joinedCall - register an ongoing call and tries sending the call notify event`() = runTest {
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        inCancellableScope {
            val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat)
            assertThat(manager.activeCall.value).isNull()

            manager.joinedCall(CallType.RoomCall(A_SESSION_ID, A_ROOM_ID))
            assertThat(manager.activeCall.value).isEqualTo(
                ActiveCall(
                    callType = CallType.RoomCall(
                        sessionId = A_SESSION_ID,
                        roomId = A_ROOM_ID,
                    ),
                    callState = CallState.InCall,
                )
            )

            runCurrent()

            verify { notificationManagerCompat.cancel(notificationId) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeRingingCalls - will cancel the active ringing call if the call is cancelled`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        // Create a cancellable coroutine scope to cancel the test when needed
        inCancellableScope {
            val matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.success(client) })
            val manager = createActiveCallManager(matrixClientProvider = matrixClientProvider)

            manager.registerIncomingCall(aCallNotificationData())

            // Call is active (the other user join the call)
            room.givenRoomInfo(aRoomInfo(hasRoomCall = true))
            advanceTimeBy(1)
            // Call is cancelled (the other user left the call)
            room.givenRoomInfo(aRoomInfo(hasRoomCall = false))
            advanceTimeBy(1)

            assertThat(manager.activeCall.value).isNull()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeRingingCalls - will do nothing if either the session or the room are not found`() = runTest {
        val room = FakeMatrixRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        // Create a cancellable coroutine scope to cancel the test when needed
        inCancellableScope {
            val matrixClientProvider = FakeMatrixClientProvider(getClient = { Result.failure(IllegalStateException("Matrix client not found")) })
            val manager = createActiveCallManager(matrixClientProvider = matrixClientProvider)

            // No matrix client

            manager.registerIncomingCall(aCallNotificationData())

            room.givenRoomInfo(aRoomInfo(hasRoomCall = true))
            advanceTimeBy(1)
            room.givenRoomInfo(aRoomInfo(hasRoomCall = false))
            advanceTimeBy(1)

            // The call should still be active
            assertThat(manager.activeCall.value).isNotNull()

            // No room
            client.givenGetRoomResult(A_ROOM_ID, null)
            matrixClientProvider.getClient = { Result.success(client) }

            manager.registerIncomingCall(aCallNotificationData())

            room.givenRoomInfo(aRoomInfo(hasRoomCall = true))
            advanceTimeBy(1)
            room.givenRoomInfo(aRoomInfo(hasRoomCall = false))
            advanceTimeBy(1)

            // The call should still be active
            assertThat(manager.activeCall.value).isNotNull()
        }
    }

    private fun TestScope.inCancellableScope(block: suspend CoroutineScope.() -> Unit) {
        launch(SupervisorJob()) {
            block()
            cancel()
        }
    }

    private fun CoroutineScope.createActiveCallManager(
        matrixClientProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
        onMissedCallNotificationHandler: FakeOnMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(),
        notificationManagerCompat: NotificationManagerCompat = mockk(relaxed = true),
        coroutineScope: CoroutineScope = this,
    ) = DefaultActiveCallManager(
        coroutineScope = coroutineScope,
        onMissedCallNotificationHandler = onMissedCallNotificationHandler,
        ringingCallNotificationCreator = RingingCallNotificationCreator(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            matrixClientProvider = matrixClientProvider,
            imageLoaderHolder = FakeImageLoaderHolder(),
            notificationBitmapLoader = FakeNotificationBitmapLoader(),
        ),
        notificationManagerCompat = notificationManagerCompat,
        matrixClientProvider = matrixClientProvider,
    )
}
