/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.push.impl.push

import app.cash.turbine.test
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SECRET
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.FakePushHistoryService
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.notifications.FakeNotificationResultProcessor
import io.element.android.libraries.push.impl.test.DefaultTestPush
import io.element.android.libraries.push.impl.troubleshoot.DiagnosticPushHandler
import io.element.android.libraries.push.impl.workmanager.SyncPendingNotificationsRequestBuilder
import io.element.android.libraries.push.test.workmanager.FakeSyncPendingNotificationsRequestBuilder
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStore
import io.element.android.libraries.pushstore.test.userpushstore.FakeUserPushStoreFactory
import io.element.android.libraries.pushstore.test.userpushstore.clientsecret.FakePushClientSecret
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.test.FakeWorkManagerScheduler
import io.element.android.services.analytics.test.FakeAnalyticsService
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

private const val A_PUSHER_INFO = "info"

@Suppress("LargeClass")
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
    fun `when classical PushData is received, the work is scheduled`() = runTest {
        val incrementPushCounterResult = lambdaRecorder<Unit> {}
        val aPushData = PushData(
            eventId = AN_EVENT_ID,
            roomId = A_ROOM_ID,
            unread = 0,
            clientSecret = A_SECRET,
        )

        val enqueuePushRequestResult = lambdaRecorder<PushRequest, Result<Unit>> { Result.success(Unit) }
        val pushHistoryService = FakePushHistoryService(
            enqueuePushRequest = enqueuePushRequestResult,
        )
        val submitWorkLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
        val workManagerScheduler = FakeWorkManagerScheduler(submitLambda = submitWorkLambda)

        val defaultPushHandler = createDefaultPushHandler(
            pushClientSecret = FakePushClientSecret(
                getUserIdFromSecretResult = { A_USER_ID }
            ),
            incrementPushCounterResult = incrementPushCounterResult,
            workManagerScheduler = workManagerScheduler,
            pushHistoryService = pushHistoryService,
        )
        defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

        advanceTimeBy(300.milliseconds)

        submitWorkLambda.assertions()
            .isCalledOnce()

        incrementPushCounterResult.assertions()
            .isCalledOnce()
    }

    @Test
    fun `when classical PushData is received, but notifications are disabled, nothing happen`() =
        runTest {
            val incrementPushCounterResult = lambdaRecorder<Unit> {}
            val aPushData = PushData(
                eventId = AN_EVENT_ID,
                roomId = A_ROOM_ID,
                unread = 0,
                clientSecret = A_SECRET,
            )
            val onPushReceivedResult = lambdaRecorder<String, EventId?, RoomId?, SessionId?, Boolean, Boolean, String?, Unit> { _, _, _, _, _, _, _ -> }
            val enqueuePushRequestResult = lambdaRecorder<PushRequest, Result<Unit>> { Result.success(Unit) }
            val pushHistoryService = FakePushHistoryService(
                onPushReceivedResult = onPushReceivedResult,
                enqueuePushRequest = enqueuePushRequestResult,
            )
            val submitWorkLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
            val workManagerScheduler = FakeWorkManagerScheduler(submitLambda = submitWorkLambda)

            val defaultPushHandler = createDefaultPushHandler(
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { A_USER_ID }
                ),
                userPushStore = FakeUserPushStore().apply {
                    setNotificationEnabledForDevice(false)
                },
                incrementPushCounterResult = incrementPushCounterResult,
                pushHistoryService = pushHistoryService,
                workManagerScheduler = workManagerScheduler,
            )
            defaultPushHandler.handle(aPushData, A_PUSHER_INFO)

            advanceTimeBy(300.milliseconds)

            submitWorkLambda.assertions()
                .isNeverCalled()
            enqueuePushRequestResult.assertions()
                .isNeverCalled()
            incrementPushCounterResult.assertions()
                .isCalledOnce()
            onPushReceivedResult.assertions()
                .isNeverCalled()
        }

    @Test
    fun `when PushData is received, but client secret is not known, nothing happen`() = runTest {
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
            val submitWorkLambda = lambdaRecorder<WorkManagerRequestBuilder, Unit> {}
            val workManagerScheduler = FakeWorkManagerScheduler(submitLambda = submitWorkLambda)
            val defaultPushHandler = createDefaultPushHandler(
                pushClientSecret = FakePushClientSecret(
                    getUserIdFromSecretResult = { null }
                ),
                incrementPushCounterResult = incrementPushCounterResult,
                pushHistoryService = pushHistoryService,
                workManagerScheduler = workManagerScheduler,
            )
            defaultPushHandler.handle(aPushData, A_PUSHER_INFO)
            submitWorkLambda.assertions()
                .isNeverCalled()
            incrementPushCounterResult.assertions()
                .isCalledOnce()
            onPushReceivedResult.assertions()
                .isCalledOnce()
        }

    @Test
    fun `when diagnostic PushData is received, the diagnostic push handler is informed`() = runTest {
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

    private fun createDefaultPushHandler(
        incrementPushCounterResult: () -> Unit = { lambdaError() },
        userPushStore: FakeUserPushStore = FakeUserPushStore(),
        pushClientSecret: PushClientSecret = FakePushClientSecret(),
        buildMeta: BuildMeta = aBuildMeta(),
        diagnosticPushHandler: DiagnosticPushHandler = DiagnosticPushHandler(),
        pushHistoryService: PushHistoryService = FakePushHistoryService(),
        workManagerScheduler: FakeWorkManagerScheduler = FakeWorkManagerScheduler(),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
        systemClock: FakeSystemClock = FakeSystemClock(),
        resultProcessor: FakeNotificationResultProcessor = FakeNotificationResultProcessor(
            emit = { Result.success(Unit) },
            start = {},
            stop = {},
        ),
    ): DefaultPushHandler {
        return DefaultPushHandler(
            incrementPushDataStore = object : IncrementPushDataStore {
                override suspend fun incrementPushCounter() {
                    incrementPushCounterResult()
                }
            },
            userPushStoreFactory = FakeUserPushStoreFactory { userPushStore },
            pushClientSecret = pushClientSecret,
            buildMeta = buildMeta,
            diagnosticPushHandler = diagnosticPushHandler,
            pushHistoryService = pushHistoryService,
            // We don't use a fake here so we can perform tests that are a bit more end to end
            analyticsService = analyticsService,
            systemClock = systemClock,
            workManagerScheduler = workManagerScheduler,
            resultProcessor = resultProcessor,
            syncPendingNotificationsRequestFactory = SyncPendingNotificationsRequestBuilder.Factory {
                FakeSyncPendingNotificationsRequestBuilder()
            }
        )
    }
}
