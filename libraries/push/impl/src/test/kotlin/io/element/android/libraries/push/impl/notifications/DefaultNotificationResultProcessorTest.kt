/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

class DefaultNotificationResultProcessorTest {
    /*
    @Test
    fun `when classical PushData is received, but not able to resolve the event, the banner to disable battery optimization will be displayed`() {
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
            val showBatteryOptimizationBannerResult = lambdaRecorder<Unit> {}
            val defaultPushHandler = createDefaultPushHandler(
                buildMeta = aBuildMeta(
                    // Also test `lowPrivacyLoggingEnabled = false` here
                    lowPrivacyLoggingEnabled = false
                ),
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { A_USER_ID }
                ),
                incrementPushCounterResult = incrementPushCounterResult,
                mutableBatteryOptimizationStore = FakeMutableBatteryOptimizationStore(
                    showBatteryOptimizationBannerResult = showBatteryOptimizationBannerResult,
                ),
                pushHistoryService = pushHistoryService,
            )
            defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

            advanceTimeBy(300.milliseconds)

            incrementPushCounterResult.assertions()
                .isCalledOnce()
            notifiableEventResult.assertions()
                .isCalledOnce()
                .with(value(A_USER_ID), any())
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
                val request = aPushRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(
                    mapOf(
                        request to Result.success(
                            ResolvedPushEvent.Event(
                                aNotifiableCallEvent(rtcNotificationType = RtcNotificationType.RING, timestamp = Instant.now().toEpochMilli())
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
                val request = aPushRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableMessageEvent(type = EventType.RTC_NOTIFICATION)))))
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
                val request = aPushRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
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
                val request = aPushRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
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
    fun `when receiving a fallback event, we notify the push history service about it not being resolved`() = runTest {
        val aNotifiableFallbackEvent = aFallbackNotifiableEvent()
        val notifiableEventResult =
            lambdaRecorder<SessionId, List<PushRequest>, Result<Map<PushRequest, Result<ResolvedPushEvent>>>> { _, _ ->
                val request = aPushRequest(A_SESSION_ID, A_ROOM_ID, AN_EVENT_ID, A_PUSHER_INFO)
                Result.success(mapOf(request to Result.success(ResolvedPushEvent.Event(aNotifiableFallbackEvent))))
            }
        val onNotifiableEventsReceived = lambdaRecorder<List<NotifiableEvent>, Unit> {}
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        var receivedFallbackEvent = false
        val onPushReceivedResult =
            lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, isResolved, _, comment ->
                receivedFallbackEvent = !isResolved && comment == "Unable to resolve event: ${aNotifiableFallbackEvent.cause}"
            }
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

        onNotifiableEventsReceived.assertions().isCalledOnce()

        assertThat(receivedFallbackEvent).isTrue()
    }
     */
}
