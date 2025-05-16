/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import io.element.android.libraries.push.impl.history.FakePushHistoryService
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.notifications.FakeNotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationEventRequest
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.push.impl.notifications.ResolvingException
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
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.matching
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

private const val A_PUSHER_INFO = "info"

class DefaultPushHandlerTest {
    @Test
    fun `check handleInvalid behavior`() = runTest {
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val defaultPushHandler = createDefaultPushHandler(
            incrementPushCounterResult = incrementPushCounterResult,
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handleInvalid(A_PUSHER_INFO, "data")
        incrementPushCounterResult.assertions()
            .isCalledOnce()
        onPushReceivedResult.assertions()
            .isCalledOnce()
            .with(value(A_PUSHER_INFO), value(null), value(null), value(null), value(false), value(false), value("Invalid or ignored push data:\ndata"))
    }

    @Test
    fun `when classical PushData is received, the notification drawer is informed`() = runTest {
        val aNotifiableMessageEvent = aNotifiableMessageEvent()
        val notifiableEventResult =
            lambdaRecorder<SessionId, List<NotificationEventRequest>, Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>> { _, _, ->
                val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent))))
            }
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val defaultPushHandler = createDefaultPushHandler(
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            notifiableEventsResult = notifiableEventResult,
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            incrementPushCounterResult = incrementPushCounterResult,
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

        advanceTimeBy(300.milliseconds)

        incrementPushCounterResult.assertions()
            .isCalledOnce()
        notifiableEventResult.assertions()
            .isCalledOnce()
            .with(value(A_USER_ID), any())
        onNotifiableEventsReceived.assertions()
            .isCalledOnce()
            .with(value(listOf(aNotifiableMessageEvent)))
        onPushReceivedResult.assertions()
            .isCalledOnce()
    }

    @Test
    fun `when classical PushData is received, but notifications are disabled, nothing happen`() =
        runTest {
            val aNotifiableMessageEvent = aNotifiableMessageEvent()
            val notifiableEventResult =
                lambdaRecorder<SessionId, List<NotificationEventRequest>, Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>> { _, _ ->
                    val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                    Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent))))
                }
            val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
            val pushHistoryService = FakePushHistoryService(
                onPushReceivedResult = onPushReceivedResult,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventsReceived = onNotifiableEventsReceived,
                notifiableEventsResult = notifiableEventResult,
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { A_USER_ID }
                ),
                userPushStore = FakeUserPushStore().apply {
                    setNotificationEnabledForDevice(false)
                },
                incrementPushCounterResult = incrementPushCounterResult,
                pushHistoryService = pushHistoryService,
            )
            defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

            advanceTimeBy(300.milliseconds)

            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isCalledOnce()
            onNotifiableEventsReceived.assertions()
                .isNeverCalled()
            onPushReceivedResult.assertions()
                .isCalledOnce()
        }

    @Test
    fun `when PushData is received, but client secret is not known, fallback the latest session`() =
        runTest {
            val aNotifiableMessageEvent = aNotifiableMessageEvent()
            val notifiableEventResult =
                lambdaRecorder<SessionId, List<NotificationEventRequest>, Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>> { _, _ ->
                    val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                    Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent))))
                }
            val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
            val pushHistoryService = FakePushHistoryService(
                onPushReceivedResult = onPushReceivedResult,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventsReceived = onNotifiableEventsReceived,
                notifiableEventsResult = notifiableEventResult,
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { null }
                ),
                matrixAuthenticationService = FakeMatrixAuthenticationService().apply {
                    getLatestSessionIdLambda = { A_USER_ID }
                },
                incrementPushCounterResult = incrementPushCounterResult,
                pushHistoryService = pushHistoryService,
            )
            defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

            advanceTimeBy(300.milliseconds)

            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isCalledOnce()
                .with(value(A_USER_ID), any())
            onNotifiableEventsReceived.assertions()
                .isCalledOnce()
                .with(value(listOf(aNotifiableMessageEvent)))
            onPushReceivedResult.assertions()
                .isCalledOnce()
        }

    @Test
    fun `when PushData is received, but client secret is not known, and there is no latest session, nothing happen`() =
        runTest {
            val aNotifiableMessageEvent = aNotifiableMessageEvent()
            val notifiableEventResult =
                lambdaRecorder<SessionId, List<NotificationEventRequest>, Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>> { _, _ ->
                    val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                    Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent))))
                }
            val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
            val pushHistoryService = FakePushHistoryService(
                onPushReceivedResult = onPushReceivedResult,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventsReceived = onNotifiableEventsReceived,
                notifiableEventsResult = notifiableEventResult,
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { null }
                ),
                matrixAuthenticationService = FakeMatrixAuthenticationService().apply {
                    getLatestSessionIdLambda = { null }
                },
                incrementPushCounterResult = incrementPushCounterResult,
                pushHistoryService = pushHistoryService,
            )
            defaultPushHandler.handle(aPushData, A_PUSHER_INFO)
            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isNeverCalled()
            onNotifiableEventsReceived.assertions()
                .isNeverCalled()
            onPushReceivedResult.assertions()
                .isCalledOnce()
        }

    @Test
    fun `when classical PushData is received, but not able to resolve the event, nothing happen`() =
        runTest {
            val notifiableEventResult =
                lambdaRecorder<SessionId, List<NotificationEventRequest>, Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>> { _, _ ->
                    Result.failure(ResolvingException("Unable to resolve"))
                }
            val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
            val pushHistoryService = FakePushHistoryService(
                onPushReceivedResult = onPushReceivedResult,
            )
            val defaultPushHandler = createDefaultPushHandler(
                onNotifiableEventsReceived = onNotifiableEventsReceived,
                notifiableEventsResult = notifiableEventResult,
                buildMeta = aBuildMeta(
                    // Also test `lowPrivacyLoggingEnabled = false` here
                    lowPrivacyLoggingEnabled = false
                ),
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { A_USER_ID }
                ),
                incrementPushCounterResult = incrementPushCounterResult,
                pushHistoryService = pushHistoryService,
            )
            defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

            advanceTimeBy(300.milliseconds)

            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isCalledOnce()
                .with(value(A_USER_ID), any())
            onNotifiableEventsReceived.assertions()
                .isNeverCalled()
            onPushReceivedResult.assertions()
                .isCalledOnce()
                .with(any(), value(AN_EVENT_ID), value(A_ROOM_ID), value(A_USER_ID), value(false), value(true), any())
        }

    @Test
    fun `when ringing call PushData is received, the incoming call will be handled`() = runTest {
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val handleIncomingCallLambda = lambdaRecorder<
            CallType.RoomCall,
            EventId,
            UserId,
            String?,
            String?,
            String?,
            String,
            String?,
            Unit,
            > { _, _, _, _, _, _, _, _ -> }
        val elementCallEntryPoint = FakeElementCallEntryPoint(handleIncomingCallResult = handleIncomingCallLambda)
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val defaultPushHandler = createDefaultPushHandler(
            elementCallEntryPoint = elementCallEntryPoint,
            notifiableEventsResult = { _, _ ->
                val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(
                    mapOf(
                        request to Result.success(
                            ResolvedPushEvent.Event(
                                aNotifiableCallEvent(callNotifyType = CallNotifyType.RING, timestamp = Instant.now().toEpochMilli())
                            )
                        )
                    )
                )
            },
            incrementPushCounterResult = {},
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

        advanceTimeBy(300.milliseconds)

        handleIncomingCallLambda.assertions().isCalledOnce()
        onNotifiableEventsReceived.assertions().isNeverCalled()
        onPushReceivedResult.assertions().isCalledOnce()
    }

    @Test
    fun `when notify call PushData is received, the incoming call will be treated as a normal notification`() = runTest {
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val handleIncomingCallLambda = lambdaRecorder<
            CallType.RoomCall,
            EventId,
            UserId,
            String?,
            String?,
            String?,
            String,
            String?,
            Unit,
            > { _, _, _, _, _, _, _, _ -> }
        val elementCallEntryPoint = FakeElementCallEntryPoint(handleIncomingCallResult = handleIncomingCallLambda)
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val defaultPushHandler = createDefaultPushHandler(
            elementCallEntryPoint = elementCallEntryPoint,
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            notifiableEventsResult = { _, _ ->
                val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent(type = EventType.CALL_NOTIFY)))))
            },
            incrementPushCounterResult = {},
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

        advanceTimeBy(300.milliseconds)

        handleIncomingCallLambda.assertions().isNeverCalled()
        onNotifiableEventsReceived.assertions().isCalledOnce()
        onPushReceivedResult.assertions().isCalledOnce()
    }

    @Test
    fun `when notify call PushData is received, the incoming call will be treated as a normal notification even if notification are disabled`() = runTest {
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val handleIncomingCallLambda = lambdaRecorder<
            CallType.RoomCall,
            EventId,
            UserId,
            String?,
            String?,
            String?,
            String,
            String?,
            Unit,
            > { _, _, _, _, _, _, _, _ -> }
        val elementCallEntryPoint = FakeElementCallEntryPoint(handleIncomingCallResult = handleIncomingCallLambda)
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val defaultPushHandler = createDefaultPushHandler(
            elementCallEntryPoint = elementCallEntryPoint,
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            notifiableEventsResult = { _, _ ->
                val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableCallEvent()))))
            },
            incrementPushCounterResult = {},
            userPushStore = FakeUserPushStore().apply {
                setNotificationEnabledForDevice(false)
            },
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

        advanceTimeBy(300.milliseconds)

        handleIncomingCallLambda.assertions().isCalledOnce()
        onNotifiableEventsReceived.assertions().isNeverCalled()
        onPushReceivedResult.assertions().isCalledOnce()
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
        val onRedactedEventReceived = lambdaRecorder<List<ResolvedPushEvent.Redaction>, Unit> { }
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val defaultPushHandler = createDefaultPushHandler(
            onRedactedEventsReceived = onRedactedEventReceived,
            incrementPushCounterResult = incrementPushCounterResult,
            notifiableEventsResult = { _, _ ->
                val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(mapOf(request to Result.success(aRedaction)))
            },
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

        advanceTimeBy(300.milliseconds)

        incrementPushCounterResult.assertions()
            .isCalledOnce()
        onRedactedEventReceived.assertions().isCalledOnce()
            .with(value(listOf(aRedaction)))
        onPushReceivedResult.assertions()
            .isCalledOnce()
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
            val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
            val pushHistoryService = FakePushHistoryService(
                onPushReceivedResult = onPushReceivedResult,
            )
            val defaultPushHandler = createDefaultPushHandler(
                diagnosticPushHandler = diagnosticPushHandler,
                incrementPushCounterResult = { },
                pushHistoryService = pushHistoryService,
            )
            diagnosticPushHandler.state.test {
                defaultPushHandler.handle(aPushData, A_PUSHER_INFO)
                awaitItem()
            }
            onPushReceivedResult.assertions()
                .isCalledOnce()
        }

    @Test
    fun `when receiving several push notifications at the same time, those are batched before being processed`() = runTest {
        val aNotifiableMessageEvent = aNotifiableMessageEvent()
        val notifiableEventResult =
            lambdaRecorder<SessionId, List<NotificationEventRequest>, Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>>> { _, _, ->
                val request = NotificationEventRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent))))
            }
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val anotherPushData = PushData(
            eventId = AN_EVENT_ID_2,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )
        val defaultPushHandler = createDefaultPushHandler(
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            notifiableEventsResult = notifiableEventResult,
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            incrementPushCounterResult = incrementPushCounterResult,
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handle(aPushData, A_PUSHER_INFO)
        defaultPushHandler.handle(anotherPushData, A_PUSHER_INFO)

        advanceTimeBy(300.milliseconds)

        incrementPushCounterResult.assertions()
            .isCalledExactly(2)
        notifiableEventResult.assertions()
            .isCalledOnce()
            .with(value(A_USER_ID), matching<List<NotificationEventRequest>> { requests ->
                requests.size == 2 && requests.first().eventId == AN_EVENT_ID && requests.last().eventId == AN_EVENT_ID_2
            })
        onNotifiableEventsReceived.assertions()
            .isCalledOnce()
        onPushReceivedResult.assertions()
            .isCalledExactly(2)
    }

    private fun TestScope.createDefaultPushHandler(
        onNotifiableEventsReceived: (List<NotifiableEvent>) -> Unit = { lambdaError() },
        onRedactedEventsReceived: (List<ResolvedPushEvent.Redaction>) -> Unit = { lambdaError() },
        notifiableEventsResult: (SessionId, List<NotificationEventRequest>) -> Result<Map<NotificationEventRequest, Result<ResolvedPushEvent>>> =
            { _, _, -> lambdaError() },
        incrementPushCounterResult: () -> Unit = { lambdaError() },
        userPushStore: UserPushStore = FakeUserPushStore(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        buildMeta: BuildMeta = aBuildMeta(),
        matrixAuthenticationService: MatrixAuthenticationService = FakeMatrixAuthenticationService(),
        diagnosticPushHandler: DiagnosticPushHandler = DiagnosticPushHandler(),
        elementCallEntryPoint: FakeElementCallEntryPoint = FakeElementCallEntryPoint(),
        notificationChannels: FakeNotificationChannels = FakeNotificationChannels(),
        pushHistoryService: PushHistoryService = FakePushHistoryService(),
    ): DefaultPushHandler {
        return DefaultPushHandler(
            onNotifiableEventReceived = FakeOnNotifiableEventReceived(onNotifiableEventsReceived),
            onRedactedEventReceived = FakeOnRedactedEventReceived(onRedactedEventsReceived),
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
            pushHistoryService = pushHistoryService,
            resolverQueue = NotificationResolverQueue(notifiableEventResolver = FakeNotifiableEventResolver(notifiableEventsResult), backgroundScope),
            appCoroutineScope = backgroundScope,
        )
    }
}
