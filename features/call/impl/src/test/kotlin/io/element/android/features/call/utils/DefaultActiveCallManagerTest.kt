/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.utils

import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallType
import io.element.android.features.call.impl.notifications.RingingCallNotificationCreator
import io.element.android.features.call.impl.utils.ActiveCall
import io.element.android.features.call.impl.utils.CallState
import io.element.android.features.call.impl.utils.DefaultActiveCallManager
import io.element.android.features.call.impl.utils.DefaultCurrentCallService
import io.element.android.features.call.test.aCallNotificationData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.ui.media.test.FakeImageLoaderHolder
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.test.notifications.FakeOnMissedCallNotificationHandler
import io.element.android.libraries.push.test.notifications.push.FakeNotificationBitmapLoader
import io.element.android.services.appnavstate.test.FakeAppForegroundStateService
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.plantTestTimber
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class DefaultActiveCallManagerTest {
    private val notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.INCOMING_CALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `registerIncomingCall - sets the incoming call as active`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat)

        assertThat(manager.activeWakeLock?.isHeld).isFalse()
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

        assertThat(manager.activeWakeLock?.isHeld).isTrue()
        verify { notificationManagerCompat.notify(notificationId, any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `registerIncomingCall - when there is an already active call adds missed call notification`() = runTest {
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        val onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda)
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

    @Test
    fun `incomingCallTimedOut - when there isn't an active call does nothing`() = runTest {
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        val manager = createActiveCallManager(
            onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda)
        )

        manager.incomingCallTimedOut(displayMissedCallNotification = true)

        addMissedCallNotificationLambda.assertions().isNeverCalled()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `incomingCallTimedOut - when there is an active call removes it and adds a missed call notification`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        val addMissedCallNotificationLambda = lambdaRecorder<SessionId, RoomId, EventId, Unit> { _, _, _ -> }
        val manager = createActiveCallManager(
            onMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(addMissedCallNotificationLambda = addMissedCallNotificationLambda),
            notificationManagerCompat = notificationManagerCompat,
        )

        manager.registerIncomingCall(aCallNotificationData())
        assertThat(manager.activeCall.value).isNotNull()
        assertThat(manager.activeWakeLock?.isHeld).isTrue()

        manager.incomingCallTimedOut(displayMissedCallNotification = true)
        advanceTimeBy(1)

        assertThat(manager.activeCall.value).isNull()
        assertThat(manager.activeWakeLock?.isHeld).isFalse()
        addMissedCallNotificationLambda.assertions().isCalledOnce()
        verify { notificationManagerCompat.cancel(notificationId) }
    }

    @Test
    fun `hungUpCall - removes existing call if the CallType matches`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat)

        val notificationData = aCallNotificationData()
        manager.registerIncomingCall(notificationData)
        assertThat(manager.activeCall.value).isNotNull()
        assertThat(manager.activeWakeLock?.isHeld).isTrue()

        manager.hungUpCall(CallType.RoomCall(notificationData.sessionId, notificationData.roomId))
        assertThat(manager.activeCall.value).isNull()
        assertThat(manager.activeWakeLock?.isHeld).isFalse()

        verify { notificationManagerCompat.cancel(notificationId) }
    }

    @Test
    fun `Decline event - Hangup on a ringing call should send a decline event`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)

        val room = mockk<JoinedRoom>(relaxed = true)

        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val clientProvider = FakeMatrixClientProvider({ Result.success(matrixClient) })

        val manager = createActiveCallManager(
            matrixClientProvider = clientProvider,
            notificationManagerCompat = notificationManagerCompat
        )

        val notificationData = aCallNotificationData(roomId = A_ROOM_ID)
        manager.registerIncomingCall(notificationData)

        manager.hungUpCall(CallType.RoomCall(notificationData.sessionId, notificationData.roomId))

        coVerify {
            room.declineCall(notificationEventId = notificationData.eventId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Decline event - Declining from another session should stop ringing`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)

        val room = FakeJoinedRoom()

        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val clientProvider = FakeMatrixClientProvider({ Result.success(matrixClient) })

        val manager = createActiveCallManager(
            matrixClientProvider = clientProvider,
            notificationManagerCompat = notificationManagerCompat
        )

        val notificationData = aCallNotificationData(roomId = A_ROOM_ID)
        manager.registerIncomingCall(notificationData)

        runCurrent()

        // Simulate declined from other session
        room.baseRoom.givenDecliner(matrixClient.sessionId, notificationData.eventId)

        runCurrent()

        assertThat(manager.activeCall.value).isNull()
        assertThat(manager.activeWakeLock?.isHeld).isFalse()

        verify { notificationManagerCompat.cancel(notificationId) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Decline event - Should ignore decline for other notification events`() = runTest {
        plantTestTimber()
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)

        val room = FakeJoinedRoom()

        val matrixClient = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val clientProvider = FakeMatrixClientProvider({ Result.success(matrixClient) })

        val manager = createActiveCallManager(
            matrixClientProvider = clientProvider,
            notificationManagerCompat = notificationManagerCompat
        )

        val notificationData = aCallNotificationData(roomId = A_ROOM_ID)
        manager.registerIncomingCall(notificationData)

        runCurrent()

        // Simulate declined for another notification event
        room.baseRoom.givenDecliner(matrixClient.sessionId, AN_EVENT_ID_2)

        runCurrent()

        assertThat(manager.activeCall.value).isNotNull()
        assertThat(manager.activeWakeLock?.isHeld).isTrue()

        verify(exactly = 0) { notificationManagerCompat.cancel(notificationId) }
    }

    @Test
    fun `hungUpCall - does nothing if the CallType doesn't match`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat)

        manager.registerIncomingCall(aCallNotificationData())
        assertThat(manager.activeCall.value).isNotNull()
        assertThat(manager.activeWakeLock?.isHeld).isTrue()

        manager.hungUpCall(CallType.ExternalUrl("https://example.com"))
        assertThat(manager.activeCall.value).isNotNull()
        assertThat(manager.activeWakeLock?.isHeld).isTrue()

        verify(exactly = 0) { notificationManagerCompat.cancel(notificationId) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `joinedCall - register an ongoing call and tries sending the call notify event`() = runTest {
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeRingingCalls - will cancel the active ringing call if the call is cancelled`() = runTest {
        val room = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeRingingCalls - will do nothing if either the session or the room are not found`() = runTest {
        val room = FakeBaseRoom().apply {
            givenRoomInfo(aRoomInfo())
        }
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `IncomingCall - rings no longer than expiration time`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        val clock = FakeSystemClock()
        val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat, systemClock = clock)

        assertThat(manager.activeWakeLock?.isHeld).isFalse()
        assertThat(manager.activeCall.value).isNull()

        val eventTimestamp = A_FAKE_TIMESTAMP
        // The call should not ring more than 30 seconds after the initial event was sent
        val expirationTimestamp = eventTimestamp + 30_000

        val callNotificationData = aCallNotificationData(
            timestamp = eventTimestamp,
            expirationTimestamp = expirationTimestamp,
        )

        // suppose it took 10s to be notified
        clock.epochMillisResult = eventTimestamp + 10_000
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

        assertThat(manager.activeWakeLock?.isHeld).isTrue()
        verify { notificationManagerCompat.notify(notificationId, any()) }

        // advance by 21s it should have stopped ringing
        advanceTimeBy(21_000)
        runCurrent()

        verify { notificationManagerCompat.cancel(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `IncomingCall - ignore expired ring lifetime`() = runTest {
        setupShadowPowerManager()
        val notificationManagerCompat = mockk<NotificationManagerCompat>(relaxed = true)
        val clock = FakeSystemClock()
        val manager = createActiveCallManager(notificationManagerCompat = notificationManagerCompat, systemClock = clock)

        assertThat(manager.activeWakeLock?.isHeld).isFalse()
        assertThat(manager.activeCall.value).isNull()

        val eventTimestamp = A_FAKE_TIMESTAMP
        // The call should not ring more than 30 seconds after the initial event was sent
        val expirationTimestamp = eventTimestamp + 30_000

        val callNotificationData = aCallNotificationData(
            timestamp = eventTimestamp,
            expirationTimestamp = expirationTimestamp,
        )

        // suppose it took 35s to be notified
        clock.epochMillisResult = eventTimestamp + 35_000
        manager.registerIncomingCall(callNotificationData)

        assertThat(manager.activeCall.value).isNull()

        runCurrent()

        assertThat(manager.activeWakeLock?.isHeld).isFalse()
        verify(exactly = 0) { notificationManagerCompat.notify(notificationId, any()) }
    }

    private fun setupShadowPowerManager() {
        shadowOf(InstrumentationRegistry.getInstrumentation().targetContext.getSystemService<PowerManager>()).apply {
            setIsWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK, true)
        }
    }

    private fun TestScope.createActiveCallManager(
        matrixClientProvider: FakeMatrixClientProvider = FakeMatrixClientProvider(),
        onMissedCallNotificationHandler: FakeOnMissedCallNotificationHandler = FakeOnMissedCallNotificationHandler(),
        notificationManagerCompat: NotificationManagerCompat = mockk(relaxed = true),
        systemClock: FakeSystemClock = FakeSystemClock(),
    ) = DefaultActiveCallManager(
        context = InstrumentationRegistry.getInstrumentation().targetContext,
        coroutineScope = backgroundScope,
        onMissedCallNotificationHandler = onMissedCallNotificationHandler,
        ringingCallNotificationCreator = RingingCallNotificationCreator(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            matrixClientProvider = matrixClientProvider,
            imageLoaderHolder = FakeImageLoaderHolder(),
            notificationBitmapLoader = FakeNotificationBitmapLoader(),
        ),
        notificationManagerCompat = notificationManagerCompat,
        matrixClientProvider = matrixClientProvider,
        defaultCurrentCallService = DefaultCurrentCallService(),
        appForegroundStateService = FakeAppForegroundStateService(),
        imageLoaderHolder = FakeImageLoaderHolder(),
        systemClock = systemClock,
    )
}
