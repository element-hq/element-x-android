/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.auth.SessionRestorationException
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.api.push.SyncOnNotifiableEvent
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.api.di.WorkerKey
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.finishLongRunningTransaction
import io.element.android.services.analytics.api.recordTransaction
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class FetchNotificationsWorker(
    @Assisted workerParams: WorkerParameters,
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val eventResolver: NotifiableEventResolver,
    private val queue: NotificationResolverQueue,
    private val workManagerScheduler: WorkManagerScheduler,
    private val syncOnNotifiableEvent: SyncOnNotifiableEvent,
    private val workerDataConverter: SyncNotificationsWorkerDataConverter,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
    private val analyticsService: AnalyticsService,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        Timber.d("FetchNotificationsWorker started")
        val requests = workerDataConverter.deserialize(inputData) ?: return Result.failure()
        // Wait for network to be available, but not more than 10 seconds
        val networkTimeoutSpans = requests.mapNotNull { request ->
            val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(request.eventId.value))
            parent?.startChild("Waiting for network connectivity", "await_network")
        }
        val hasNetwork = withTimeoutOrNull(10.seconds) {
            networkMonitor.connectivity.first { it == NetworkStatus.Connected }
        } != null

        for (span in networkTimeoutSpans) {
            span.finish()
        }

        if (!hasNetwork) {
            Timber.w("No network, retrying later")
            for (request in requests) {
                val eventId = request.eventId.value
                analyticsService.finishLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(eventId))
                val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(eventId))
                // Since we're retrying, start a new transaction
                analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(eventId), parent)
            }
            return Result.retry()
        }

        val pendingAnalyticTransactions = requests.mapNotNull { request ->
            analyticsService.finishLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(request.eventId.value))
            val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(request.eventId.value))
            val transactionName = "WorkManager to event fetched"
            parent?.startChild(transactionName)?.let { request.eventId to it }
        }.toMap()

        val failedSyncForSessions = mutableMapOf<SessionId, Throwable>()

        val groupedRequests = requests.groupBy { it.sessionId }.toMutableMap()
        for ((sessionId, notificationRequests) in groupedRequests) {
            Timber.d("Processing notification requests for session $sessionId")
            eventResolver.resolveEvents(sessionId, notificationRequests)
                .fold(
                    onSuccess = { result ->
                        for ((_, transaction) in pendingAnalyticTransactions) {
                            transaction.finish()
                        }
                        // Update the resolved results in the queue
                        (queue.results as MutableSharedFlow).emit(requests to result)
                    },
                    onFailure = {
                        for ((_, transaction) in pendingAnalyticTransactions) {
                            transaction.attachError(it)
                            transaction.finish()
                        }
                        failedSyncForSessions[sessionId] = it
                        Timber.e(it, "Failed to resolve notification events for session $sessionId")
                    }
                )
        }

        // If there were failures for whole sessions, we retry all their requests
        if (failedSyncForSessions.isNotEmpty()) {
            @Suppress("LoopWithTooManyJumpStatements")
            for ((failedSessionId, exception) in failedSyncForSessions) {
                if (exception.cause is SessionRestorationException) {
                    Timber.e(exception, "Session $failedSessionId could not be restored, not retrying notification fetching")
                    groupedRequests.remove(failedSessionId)
                    continue
                }
                val requestsToRetry = groupedRequests[failedSessionId] ?: continue

                for (request in requestsToRetry) {
                    val failedTransaction = pendingAnalyticTransactions[request.eventId]
                    failedTransaction?.attachError(exception)
                    failedTransaction?.finish()

                    val eventId = request.eventId.value
                    val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(eventId))
                    // Since we're retrying, start a new transaction
                    analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(eventId), parent)
                }

                Timber.d("Re-scheduling ${requestsToRetry.size} failed notification requests for session $failedSessionId")
                workManagerScheduler.submit(
                    SyncNotificationWorkManagerRequest(
                        sessionId = failedSessionId,
                        notificationEventRequests = requestsToRetry,
                        workerDataConverter = workerDataConverter,
                        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
                    )
                )
            }
        }

        Timber.d("Notifications processed successfully")

        analyticsService.recordTransaction("Opportunistic sync", "opportunistic_sync") {
            performOpportunisticSyncIfNeeded(groupedRequests)
        }

        return Result.success()
    }

    private suspend fun performOpportunisticSyncIfNeeded(
        groupedRequests: Map<SessionId, List<NotificationEventRequest>>,
    ) {
        for ((sessionId, notificationRequests) in groupedRequests) {
            runCatchingExceptions {
                syncOnNotifiableEvent(notificationRequests)
            }.onFailure {
                Timber.e(it, "Failed to sync on notifiable events for session $sessionId")
            }
        }
    }

    @ContributesIntoMap(AppScope::class, binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>())
    @WorkerKey(FetchNotificationsWorker::class)
    @AssistedFactory
    interface Factory : MetroWorkerFactory.WorkerInstanceFactory<FetchNotificationsWorker>
}
