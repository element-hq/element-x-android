/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.push.impl.push

import app.cash.turbine.test
import io.element.android.features.call.api.CallType
import io.element.android.features.call.test.FakeElementCallEntryPoint
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.notification.CallNotifyType
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.auth.FakeMatrixAuthenticationService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.push.impl.notifications.FakeNotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.channels.FakeNotificationChannels
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableCallEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.impl.test.DefaultTestPush
import io.element.android.libraries.push.impl.troubleshoot.DiagnosticPushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushstore.api.UserPushStore
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class DefaultPushHandlerTest {
    @Test
    fun `when classical PushData is received, the notification drawer is informed`() = runTest {
        val aNotifiableMessageEvent = aNotifiableMessageEvent()
        val notifiableEventResult =
            lambdaRecorder<SessionId, RoomId, EventId, ResolvedPushEvent> { _, _, _ ->
                ResolvedPushEvent.Event(aNotifiableMessageEvent)
            }
        val onNotifiableEventReceived = lambdaRecorder<NotifiableEvent, Unit> {}
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val defaultPushHandler = createDefaultPushHandler(
            onNotifiableEventReceived = onNotifiableEventReceived,
            notifiableEventResult = notifiableEventResult,
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            incrementPushCounterResult = incrementPushCounterResult
        )
        defaultPushHandler.handle(aPushData)
        incrementPushCounterResult.assertions()
            .isCalledOnce()
        notifiableEventResult.assertions()
            .isCalledOnce()
            .with(value(A_USER_ID), value(A_ROOM_ID), value(AN_EVENT_ID))
        onNotifiableEventReceived.assertions()
            .isCalledOnce()
            .with(value(aNotifiableMessageEvent))
    }

    @Test
    fun `when classical PushData is received, but notifications are disabled, nothing happen`() =
        runTest {
            val aNotifiableMessageEvent = aNotifiableMessageEvent()
            val notifiableEventResult =
                lambdaRecorder<SessionId, RoomId, EventId, ResolvedPushEvent.Event> { _, _, _ ->
                    ResolvedPushEvent.Event(aNotifiableMessageEvent)
                }
            val onNotifiableEventReceived = lambdaRecorder<NotifiableEvent, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventReceived = onNotifiableEventReceived,
                notifiableEventResult = notifiableEventResult,
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { A_USER_ID }
                ),
                userPushStore = FakeUserPushStore().apply {
                    setNotificationEnabledForDevice(false)
                },
                incrementPushCounterResult = incrementPushCounterResult
            )
            defaultPushHandler.handle(aPushData)
            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isCalledOnce()
            onNotifiableEventReceived.assertions()
                .isNeverCalled()
        }

    @Test
    fun `when PushData is received, but client secret is not known, fallback the latest session`() =
        runTest {
            val aNotifiableMessageEvent = aNotifiableMessageEvent()
            val notifiableEventResult =
                lambdaRecorder<SessionId, RoomId, EventId, ResolvedPushEvent.Event> { _, _, _ ->
                    ResolvedPushEvent.Event(aNotifiableMessageEvent)
                }
            val onNotifiableEventReceived = lambdaRecorder<NotifiableEvent, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventReceived = onNotifiableEventReceived,
                notifiableEventResult = notifiableEventResult,
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { null }
                ),
                matrixAuthenticationService = FakeMatrixAuthenticationService().apply {
                    getLatestSessionIdLambda = { A_USER_ID }
                },
                incrementPushCounterResult = incrementPushCounterResult
            )
            defaultPushHandler.handle(aPushData)
            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isCalledOnce()
                .with(value(A_USER_ID), value(A_ROOM_ID), value(AN_EVENT_ID))
            onNotifiableEventReceived.assertions()
                .isCalledOnce()
                .with(value(aNotifiableMessageEvent))
        }

    @Test
    fun `when PushData is received, but client secret is not known, and there is no latest session, nothing happen`() =
        runTest {
            val aNotifiableMessageEvent = aNotifiableMessageEvent()
            val notifiableEventResult =
                lambdaRecorder<SessionId, RoomId, EventId, ResolvedPushEvent.Event> { _, _, _ ->
                    ResolvedPushEvent.Event(aNotifiableMessageEvent)
                }
            val onNotifiableEventReceived = lambdaRecorder<NotifiableEvent, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventReceived = onNotifiableEventReceived,
                notifiableEventResult = notifiableEventResult,
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { null }
                ),
                matrixAuthenticationService = FakeMatrixAuthenticationService().apply {
                    getLatestSessionIdLambda = { null }
                },
                incrementPushCounterResult = incrementPushCounterResult
            )
            defaultPushHandler.handle(aPushData)
            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isNeverCalled()
            onNotifiableEventReceived.assertions()
                .isNeverCalled()
        }

    @Test
    fun `when classical PushData is received, but not able to resolve the event, nothing happen`() =
        runTest {
            val notifiableEventResult =
                lambdaRecorder<SessionId, RoomId, EventId, ResolvedPushEvent.Event?> { _, _, _ -> null }
            val onNotifiableEventReceived = lambdaRecorder<NotifiableEvent, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventReceived = onNotifiableEventReceived,
                notifiableEventResult = notifiableEventResult,
                buildMeta = aBuildMeta(
                    // Also test `lowPrivacyLoggingEnabled = false` here
                    lowPrivacyLoggingEnabled = false
                ),
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { A_USER_ID }
                ),
                incrementPushCounterResult = incrementPushCounterResult
            )
            defaultPushHandler.handle(aPushData)
            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isCalledOnce()
                .with(value(A_USER_ID), value(A_ROOM_ID), value(AN_EVENT_ID))
            onNotifiableEventReceived.assertions()
                .isNeverCalled()
        }

    @Test
    fun `when ringing call PushData is received, the incoming call will be handled`() = runTest {
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val handleIncomingCallLambda = lambdaRecorder<CallType.RoomCall, EventId, UserId, String?, String?, String?, String, Unit> { _, _, _, _, _, _, _ -> }
        val elementCallEntryPoint = FakeElementCallEntryPoint(handleIncomingCallResult = handleIncomingCallLambda)
        val defaultPushHandler = createDefaultPushHandler(
            elementCallEntryPoint = elementCallEntryPoint,
            notifiableEventResult = { _, _, _ ->
                ResolvedPushEvent.Event(aNotifiableCallEvent(callNotifyType = CallNotifyType.RING, timestamp = Instant.now().toEpochMilli()))
            },
            incrementPushCounterResult = {},
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
        )
        defaultPushHandler.handle(aPushData)

        handleIncomingCallLambda.assertions().isCalledOnce()
    }

    @Test
    fun `when notify call PushData is received, the incoming call will be treated as a normal notification`() = runTest {
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val onNotifiableEventReceived = lambdaRecorder<NotifiableEvent, Unit> {}
        val handleIncomingCallLambda = lambdaRecorder<CallType.RoomCall, EventId, UserId, String?, String?, String?, String, Unit> { _, _, _, _, _, _, _ -> }
        val elementCallEntryPoint = FakeElementCallEntryPoint(handleIncomingCallResult = handleIncomingCallLambda)
        val defaultPushHandler = createDefaultPushHandler(
            elementCallEntryPoint = elementCallEntryPoint,
            onNotifiableEventReceived = onNotifiableEventReceived,
            notifiableEventResult = { _, _, _ ->
                ResolvedPushEvent.Event(aNotifiableMessageEvent(type = EventType.CALL_NOTIFY))
            },
            incrementPushCounterResult = {},
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
        )
        defaultPushHandler.handle(aPushData)

        handleIncomingCallLambda.assertions().isNeverCalled()
        onNotifiableEventReceived.assertions().isCalledOnce()
    }

    @Test
    fun `when notify call PushData is received, the incoming call will be treated as a normal notification even if notification are disabled`() = runTest {
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val onNotifiableEventReceived = lambdaRecorder<NotifiableEvent, Unit> {}
        val handleIncomingCallLambda = lambdaRecorder<CallType.RoomCall, EventId, UserId, String?, String?, String?, String, Unit> { _, _, _, _, _, _, _ -> }
        val elementCallEntryPoint = FakeElementCallEntryPoint(handleIncomingCallResult = handleIncomingCallLambda)
        val defaultPushHandler = createDefaultPushHandler(
            elementCallEntryPoint = elementCallEntryPoint,
            onNotifiableEventReceived = onNotifiableEventReceived,
            notifiableEventResult = { _, _, _ ->
                ResolvedPushEvent.Event(aNotifiableCallEvent())
            },
            incrementPushCounterResult = {},
            userPushStore = FakeUserPushStore().apply {
                setNotificationEnabledForDevice(false)
            },
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
        )
        defaultPushHandler.handle(aPushData)
        handleIncomingCallLambda.assertions().isCalledOnce()
        onNotifiableEventReceived.assertions().isNeverCalled()
    }

    @Test
    fun `when a redaction is received, the onRedactedEventReceived is informed`() = runTest {
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val aRedaction = ResolvedPushEvent.Redaction(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            redactedEventId = AN_EVENT_ID_2,
            reason = null
        )
        val onRedactedEventReceived = lambdaRecorder<ResolvedPushEvent.Redaction, Unit> { }
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        val defaultPushHandler = createDefaultPushHandler(
            onRedactedEventReceived = onRedactedEventReceived,
            incrementPushCounterResult = incrementPushCounterResult,
            notifiableEventResult = { _, _, _ -> aRedaction },
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
        )
        defaultPushHandler.handle(aPushData)
        incrementPushCounterResult.assertions()
            .isCalledOnce()
        onRedactedEventReceived.assertions().isCalledOnce()
            .with(value(aRedaction))
    }

    @Test
    fun `when diagnostic PushData is received, the diagnostic push handler is informed`() =
        runTest {
            val aPushData = PushData(
                eventId = DefaultTestPush.TEST_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val diagnosticPushHandler = DiagnosticPushHandler()
            val defaultPushHandler = createDefaultPushHandler(
                diagnosticPushHandler = diagnosticPushHandler,
                incrementPushCounterResult = { }
            )
            diagnosticPushHandler.state.test {
                defaultPushHandler.handle(aPushData)
                awaitItem()
            }
        }

    private fun createDefaultPushHandler(
        onNotifiableEventReceived: (NotifiableEvent) -> Unit = { lambdaError() },
        onRedactedEventReceived: (ResolvedPushEvent.Redaction) -> Unit = { lambdaError() },
        notifiableEventResult: (SessionId, RoomId, EventId) -> ResolvedPushEvent? = { _, _, _ -> lambdaError() },
        incrementPushCounterResult: () -> Unit = { lambdaError() },
        userPushStore: UserPushStore = FakeUserPushStore(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        buildMeta: BuildMeta = aBuildMeta(),
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        diagnosticPushHandler: DiagnosticPushHandler = DiagnosticPushHandler(),
        elementCallEntryPoint: FakeElementCallEntryPoint = FakeElementCallEntryPoint(),
        notificationChannels: FakeNotificationChannels = FakeNotificationChannels(),
    ): DefaultPushHandler {
        return DefaultPushHandler(
            onNotifiableEventReceived = FakeOnNotifiableEventReceived(onNotifiableEventReceived),
            onRedactedEventReceived = FakeOnRedactedEventReceived(onRedactedEventReceived),
            notifiableEventResolver = FakeNotifiableEventResolver(notifiableEventResult),
            incrementPushDataStore = object : IncrementPushDataStore {
                override suspend fun incrementPushCounter() {
                    incrementPushCounterResult()
                }
            },
            userPushStoreFactory = FakeUserPushStoreFactory { userPushStore },
            pushClientSecret = pushClientSecret,
            buildMeta = buildMeta,
            matrixAuthenticationService = matrixAuthenticationService,
            diagnosticPushHandler = diagnosticPushHandler,
            elementCallEntryPoint = elementCallEntryPoint,
            notificationChannels = notificationChannels,
        )
    }
}
