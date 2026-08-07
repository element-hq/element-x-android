/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.api.CallData
import io.element.android.features.call.test.FakeElementCallEntryPoint
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.exception.NotificationResolverException
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.FakePushHistoryService
import io.element.android.libraries.push.impl.notifications.channels.FakeNotificationChannels
import io.element.android.libraries.push.impl.notifications.fixtures.aFallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableCallEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aNotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.fixtures.aPushRequest
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.impl.push.FakeMutableBatteryOptimizationStore
import io.element.android.libraries.push.impl.push.FakeOnNotifiableEventReceived
import io.element.android.libraries.push.impl.push.FakeOnRedactedEventReceived
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNotificationResultProcessorTest {
    @Test
    fun `when not able to resolve the event, the banner to disable battery optimization will be displayed`() {
        `test notification resolver failure`(
            notificationResolveResult = { requests: List<PushRequest> ->
                Result.success(
                    requests.associateWith { Result.failure(NotificationResolverException.UnknownError("Unable to resolve event")) }
                )
            },
            shouldSetOptimizationBatteryBanner = true,
        )
    }

    private fun `test notification resolver failure`(
        notificationResolveResult: (List<PushRequest>) -> Result<Map<PushRequest, Result<ResolvedPushEvent>>>,
        shouldSetOptimizationBatteryBanner: Boolean,
    ) {
        runTest {
            val notifiableEventResult =
                lambdaRecorder<SessionId, List<PushRequest>, Result<Map<PushRequest, Result<ResolvedPushEvent>>>> { _, requests ->
                    notificationResolveResult(requests)
                }
            val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
            val pushHistoryService = FakePushHistoryService(
                onPushReceivedResult = onPushReceivedResult,
            )
            val showBatteryOptimizationBannerResult = lambdaRecorder<Unit> {}
            val processor = createDefaultNotificationResultProcessor(
                mutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(
                    showBatteryOptimizationBannerResult = showBatteryOptimizationBannerResult,
                ),
                pushHistoryService = pushHistoryService,
            )

            runningProcessor(processor) {
                emit(mapOf(aPushRequest() to Result.failure(IllegalStateException("boom"))))
            }

            notifiableEventResult.assertions()
                .isNeverCalled()
            onPushReceivedResult.assertions()
                .isCalledOnce()
                .with(any(), value(AN_EVENT_ID), value(A_ROOM_ID), value(A_USER_ID), value(false), value(true), any())
            showBatteryOptimizationBannerResult.assertions().let {
                if (shouldSetOptimizationBatteryBanner) {
                    it.isCalledOnce()
                } else {
                    it.isNeverCalled()
                }
            }
        }
    }

    @Test
    fun `when ringing call PushData is received, the incoming call will be handled`() = runTest {
        val handleIncomingCallLambda = lambdaRecorder<
            CallData,
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
        val processor = createDefaultNotificationResultProcessor(
            elementCallEntryPoint = elementCallEntryPoint,
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            pushHistoryService = pushHistoryService,
        )
        runningProcessor(processor) {
            emit(mapOf(aPushRequest() to Result.success(ResolvedPushEvent.Event(aNotifiableCallEvent()))))
        }

        advanceTimeBy(300.milliseconds)

        handleIncomingCallLambda.assertions().isCalledOnce()
        onNotifiableEventsReceived.assertions().isNeverCalled()
        onPushReceivedResult.assertions().isCalledOnce()
    }

    @Test
    fun `when notify call PushData is received, the incoming call will be treated as a normal notification`() = runTest {
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val handleIncomingCallLambda = lambdaRecorder<
            CallData,
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
        val processor = createDefaultNotificationResultProcessor(
            elementCallEntryPoint = elementCallEntryPoint,
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            pushHistoryService = pushHistoryService,
        )

        runningProcessor(processor) {
            processor.emit(mapOf(aPushRequest() to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent(type = EventType.RTC_NOTIFICATION)))))
        }

        advanceTimeBy(300.milliseconds)

        handleIncomingCallLambda.assertions().isNeverCalled()
        onNotifiableEventsReceived.assertions().isCalledOnce()
        onPushReceivedResult.assertions().isCalledOnce()
    }

    @Test
    fun `when notify call PushData is received, the incoming call will be treated as a normal notification even if notification are disabled`() = runTest {
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val handleIncomingCallLambda = lambdaRecorder<
            CallData,
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
        val processor = createDefaultNotificationResultProcessor(
            elementCallEntryPoint = elementCallEntryPoint,
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            pushHistoryService = pushHistoryService,
        )

        runningProcessor(processor) {
            processor.emit(mapOf(aPushRequest() to Result.success(ResolvedPushEvent.Event(aNotifiableCallEvent()))))
        }

        advanceTimeBy(300.milliseconds)

        handleIncomingCallLambda.assertions().isCalledOnce()
        onNotifiableEventsReceived.assertions().isNeverCalled()
        onPushReceivedResult.assertions().isCalledOnce()
    }

    @Test
    fun `when a redaction is received, the onRedactedEventReceived is informed`() = runTest {
        val aRedaction = ResolvedPushEvent.Redaction(
            sessionId = A_SESSION_ID,
            roomId = A_ROOM_ID,
            redactedEventId = AN_EVENT_ID_2,
            reason = null
        )
        val onRedactedEventReceived = lambdaRecorder<List<ResolvedPushEvent.Redaction>, Unit> { }
        val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )
        val processor = createDefaultNotificationResultProcessor(
            onRedactedEventReceived = onRedactedEventReceived,
            pushHistoryService = pushHistoryService,
        )

        runningProcessor(processor) {
            emit(mapOf(aPushRequest() to Result.success(aRedaction)))
        }

        advanceTimeBy(300.milliseconds)

        onRedactedEventReceived.assertions().isCalledOnce()
            .with(value(listOf(aRedaction)))
        onPushReceivedResult.assertions()
            .isCalledOnce()
    }

    @Test
    fun `when receiving a fallback event, we notify the push history service about it not being resolved`() = runTest {
        val aNotifiableFallbackEvent = aFallbackNotifiableEvent()
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        var receivedFallbackEvent = false
        val onPushReceivedResult =
            lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, isResolved, _, comment ->
                receivedFallbackEvent = !isResolved && comment == "Unable to resolve event: ${aNotifiableFallbackEvent.cause}"
            }
        val pushHistoryService = FakePushHistoryService(
            onPushReceivedResult = onPushReceivedResult,
        )

        val processor = createDefaultNotificationResultProcessor(
            onNotifiableEventsReceived = onNotifiableEventsReceived,
            pushHistoryService = pushHistoryService,
        )

        runningProcessor(processor) {
            emit(mapOf(aPushRequest() to Result.success(ResolvedPushEvent.Event(aNotifiableFallbackEvent))))
        }

        advanceTimeBy(300.milliseconds)

        onNotifiableEventsReceived.assertions().isCalledOnce()

        assertThat(receivedFallbackEvent).isTrue()
    }

    private suspend fun TestScope.runningProcessor(processor: NotificationResultProcessor, block: suspend NotificationResultProcessor.() -> Unit) {
        processor.start()

        runCurrent()

        block(processor)

        runCurrent()

        processor.stop()
    }

    private fun TestScope.createDefaultNotificationResultProcessor(
        systemClock: FakeSystemClock = FakeSystemClock(),
        pushHistoryService: FakePushHistoryService = FakePushHistoryService(),
        mutableBatteryOptimizationStore: FakeMutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(),
        fallbackNotificationFactory: FallbackNotificationFactory = FallbackNotificationFactory(systemClock),
        userPushStoreFactory: FakeUserPushStoreFactory = FakeUserPushStoreFactory(),
        onRedactedEventReceived: (List<ResolvedPushEvent.Redaction>) -> Unit = {},
        onNotifiableEventsReceived: (List<NotifiableEvent>) -> Unit = {},
        elementCallEntryPoint: FakeElementCallEntryPoint = FakeElementCallEntryPoint(),
        notificationChannels: FakeNotificationChannels = FakeNotificationChannels(),
        coroutineScope: CoroutineScope = backgroundScope,
    ) = DefaultNotificationResultProcessor(
        pushHistoryService = pushHistoryService,
        batteryOptimizationStore = mutableBatteryOptimizationStore,
        fallbackNotificationFactory = fallbackNotificationFactory,
        userPushStoreFactory = userPushStoreFactory,
        onRedactedEventReceived = FakeOnRedactedEventReceived(onRedactedEventReceived),
        onNotifiableEventReceived = FakeOnNotifiableEventReceived(onNotifiableEventsReceived),
        elementCallEntryPoint = elementCallEntryPoint,
        notificationChannels = notificationChannels,
        coroutineScope = coroutineScope,
    )
}
