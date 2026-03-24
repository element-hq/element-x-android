/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.history.onDiagnosticPush
import io.element.android.libraries.push.impl.history.onInvalidPushReceived
import io.element.android.libraries.push.impl.history.onUnableToRetrieveSession
import io.element.android.libraries.push.impl.notifications.NotificationResultProcessor
import io.element.android.libraries.push.impl.test.DefaultTestPush
import io.element.android.libraries.push.impl.troubleshoot.DiagnosticPushHandler
import io.element.android.libraries.push.impl.workmanager.SyncPendingNotificationsRequestBuilder
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

private val loggerTag = LoggerTag("PushHandler", LoggerTag.PushLoggerTag)

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultPushHandler(
    private val incrementPushDataStore: IncrementPushDataStore,
    private val pushClientSecret: PushClientSecret,
    private val buildMeta: BuildMeta,
    private val diagnosticPushHandler: DiagnosticPushHandler,
    private val pushHistoryService: PushHistoryService,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val analyticsService: AnalyticsService,
    private val systemClock: SystemClock,
    private val workManagerScheduler: WorkManagerScheduler,
    private val syncPendingNotificationsRequestFactory: SyncPendingNotificationsRequestBuilder.Factory,
    resultProcessor: NotificationResultProcessor,
) : PushHandler {
    init {
        resultProcessor.start()
    }

    /**
     * Called when message is received.
     *
     * @param pushData the data received in the push.
     * @param providerInfo the provider info.
     */
    override suspend fun handle(pushData: PushData, providerInfo: String): Boolean {
        // Start measuring how long it takes to display a notification from when the push is received
        Timber.d("Calculating push-to-notification for event ${pushData.eventId}")
        val parent = analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(pushData.eventId.value))
        analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(pushData.eventId.value), parent)

        Timber.tag(loggerTag.value).d("## handling pushData: ${pushData.roomId}/${pushData.eventId}")
        if (buildMeta.lowPrivacyLoggingEnabled) {
            Timber.tag(loggerTag.value).d("## pushData: $pushData")
        }

        // Update the push counter without blocking the coroutine execution, as it is not critical to be updated before handling the push
        CoroutineScope(currentCoroutineContext()).launch {
            incrementPushDataStore.incrementPushCounter()
        }

        // Diagnostic Push
        return if (pushData.eventId == DefaultTestPush.TEST_EVENT_ID) {
            pushHistoryService.onDiagnosticPush(providerInfo)
            diagnosticPushHandler.handlePush()
            false
        } else {
            handleInternal(pushData, providerInfo)
        }
    }

    override suspend fun handleInvalid(providerInfo: String, data: String) {
        incrementPushDataStore.incrementPushCounter()
        pushHistoryService.onInvalidPushReceived(providerInfo, data)
    }

    /**
     * Internal receive method.
     *
     * @param pushData Object containing message data.
     * @param providerInfo the provider info.
     */
    private suspend fun handleInternal(pushData: PushData, providerInfo: String): Boolean {
        try {
            if (buildMeta.lowPrivacyLoggingEnabled) {
                Timber.tag(loggerTag.value).d("## handleInternal() : $pushData")
            } else {
                Timber.tag(loggerTag.value).d("## handleInternal()")
            }
            // Get userId from client secret
            val userId = pushClientSecret.getUserIdFromSecret(pushData.clientSecret)
            if (userId == null) {
                Timber.w("Unable to get userId from client secret")
                pushHistoryService.onUnableToRetrieveSession(
                    providerInfo = providerInfo,
                    eventId = pushData.eventId,
                    roomId = pushData.roomId,
                    reason = "Unable to get userId from client secret",
                )
                return false
            }

            val areNotificationsEnabled = userPushStoreFactory.getOrCreate(userId).getNotificationEnabledForDevice().first()
            if (!areNotificationsEnabled) {
                Timber.w("Push notification received when push notifications are disabled.")
                return false
            }

            val pushRequest = PushRequest(
                pushDate = systemClock.epochMillis(),
                providerInfo = providerInfo,
                eventId = pushData.eventId.value,
                roomId = pushData.roomId.value,
                sessionId = userId.value,
                status = PushRequestStatus.PENDING.value,
                retries = 0L,
            )

            Timber.d("Queueing notification: $pushRequest")
            pushHistoryService.insertOrUpdatePushRequest(pushRequest)
            Timber.d("Queueing notification finished")

            if (!workManagerScheduler.hasPendingWork(userId, WorkManagerRequestType.NOTIFICATION_SYNC)) {
                Timber.d("No pending worker for push notifications found")
                workManagerScheduler.submit(syncPendingNotificationsRequestFactory.create(userId))
            }

            return true
        } catch (e: Exception) {
            Timber.tag(loggerTag.value).e(e, "## handleInternal() failed")
            return false
        }
    }
}

/**
 * Represents the status of a [PushRequest].
 */
enum class PushRequestStatus(val value: Long) {
    /**
     * Either it was enqueued, and we never tried to fetch it, or it failed with a recoverable error.
     */
    PENDING(0),

    /**
     * The event for the [PushRequest] was fetched successfully.
     */
    SUCCESS(1),

    /**
     * Fetching the event for the [PushRequest] failed with an unrecoverable error, and it won't be retried.
     */
    FAILED(2),
}
