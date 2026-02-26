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
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.exception.isNetworkError
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationResultProcessor
import io.element.android.libraries.push.impl.push.PushRequestStatus
import io.element.android.libraries.push.impl.push.SyncOnNotifiableEvent
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.api.di.WorkerKey
import io.element.android.services.analytics.api.AnalyticsLongRunningTransaction
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analytics.api.finishLongRunningTransaction
import io.element.android.services.analytics.api.recordTransaction
import io.element.android.services.analyticsproviders.api.AnalyticsTransaction
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@AssistedInject
class FetchPendingNotificationsWorker(
    @Assisted private val params: WorkerParameters,
    @ApplicationContext private val context: Context,
    private val pushHistoryService: PushHistoryService,
    private val networkMonitor: NetworkMonitor,
    private val eventResolver: NotifiableEventResolver,
    private val workManagerScheduler: WorkManagerScheduler,
    private val syncOnNotifiableEvent: SyncOnNotifiableEvent,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
    private val resultProcessor: NotificationResultProcessor,
    private val analyticsService: AnalyticsService,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        Timber.d("FetchNotificationsWorker started")
        val sessionId = inputData.getString("session_id")?.let(::SessionId) ?: return Result.failure()

        val requests = pushHistoryService.getPendingPushRequests(sessionId).getOrNull() ?: return Result.failure()
        if (requests.isEmpty()) {
            Timber.d("No pending notifications to fetch, returning early")
            return Result.success()
        }

        Timber.d("Fetching ${requests.size} push requests")

        val networkTimeoutSpans = requests.mapNotNull { request ->
            val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(request.eventId))
            parent?.startChild("Waiting for network connectivity", "await_network")
        }

        // Wait for network to be available, but not more than 10 seconds
        val hasNetwork = withTimeoutOrNull(10.seconds) {
            networkMonitor.connectivity.first { it == NetworkStatus.Connected }
        } != null

        networkTimeoutSpans.finish()

        // If there is a problem with the updated network values, report it and retry if needed
        if (reportConnectivityError(requests = requests, hasNetwork = hasNetwork, isNetworkBlocked = networkMonitor.isNetworkBlocked())) {
            // TODO increase attempt count
            return Result.retry()
        }

        val pendingAnalyticTransactions = requests.mapNotNull { request ->
            analyticsService.finishLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(request.eventId))
            val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(request.eventId))
            val transactionName = "WorkManager to event fetched"
            parent?.startChild(transactionName)?.let { request.eventId to it }
        }.toMap()

        var failedSyncForSession: Throwable? = null

        Timber.d("Processing notification requests for session $sessionId")
        val results = eventResolver.resolveEvents(sessionId, requests)
            .fold(
                onSuccess = { results ->
                    for ((_, transaction) in pendingAnalyticTransactions) {
                        transaction.finish()
                    }
                    // Update the resolved results in the queue
                    resultProcessor.emit(results)

                    results
                },
                onFailure = {
                    for ((_, transaction) in pendingAnalyticTransactions) {
                        transaction.attachError(it)
                        transaction.finish()
                    }
                    failedSyncForSession = it
                    Timber.e(it, "Failed to resolve notification events for session $sessionId")

                    emptyMap()
                }
            )

        // If there were failures for whole sessions, we retry all their requests
        failedSyncForSession?.let { exception ->
            @Suppress("LoopWithTooManyJumpStatements")
            if (exception.cause is SessionRestorationException) {
                Timber.e(exception, "Session $sessionId could not be restored, not retrying notification fetching")
            }
            for (request in requests) {
                val failedTransaction = pendingAnalyticTransactions[request.eventId]
                failedTransaction?.attachError(exception)
                failedTransaction?.finish()

                val eventId = request.eventId
                val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(eventId))
                // Since we're retrying, start a new transaction
                analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(eventId), parent)
            }

            Timber.d("Re-scheduling ${requests.size} failed notification requests for session $sessionId")
            // TODO: update attempts
            if (!workManagerScheduler.hasPendingWork(sessionId, WorkManagerRequestType.NOTIFICATION_SYNC)) {
                workManagerScheduler.submit(
                    SyncPendingNotificationsWorkManagerRequestBuilder(
                        sessionId = sessionId,
                        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
                    )
                )
            }
        }

        val updatedRequests = mutableListOf<PushRequest>()
        for (request in requests) {
            val result = results[request] ?: continue
            result.fold(
                onSuccess = { updatedRequests.add(request.copy(status = PushRequestStatus.SUCCESS.value)) },
                onFailure = { exception ->
                    if (exception is ClientException && exception.isNetworkError()) {
                        updatedRequests.add(request.copy(status = PushRequestStatus.PENDING.value))
                    } else {
                        updatedRequests.add(request.copy(status = PushRequestStatus.FAILED_UNRECOVERABLE.value))
                    }
                }
            )
        }

        Timber.d("Notifications processed successfully")

        pushHistoryService.replacePushRequests(updatedRequests)

        analyticsService.recordTransaction("Opportunistic sync", "opportunistic_sync") {
            performOpportunisticSyncIfNeeded(mapOf(sessionId to requests))
        }

        return Result.success()
    }

    private suspend fun performOpportunisticSyncIfNeeded(
        groupedRequests: Map<SessionId, List<PushRequest>>,
    ) {
        for ((sessionId, notificationRequests) in groupedRequests) {
            runCatchingExceptions {
                syncOnNotifiableEvent(notificationRequests)
            }.onFailure {
                Timber.e(it, "Failed to sync on notifiable events for session $sessionId")
            }
        }
    }

    private fun reportConnectivityError(requests: List<PushRequest>, hasNetwork: Boolean, isNetworkBlocked: Boolean): Boolean {
        return if (!hasNetwork || isNetworkBlocked) {
            for (request in requests) {
                val eventId = request.eventId
                analyticsService.finishLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(eventId)) {
                    it.putExtraData("has_network_connection", hasNetwork.toString())
                    it.putExtraData("is_network_blocked", isNetworkBlocked.toString())
                }
                val parent = analyticsService.getLongRunningTransaction(AnalyticsLongRunningTransaction.PushToNotification(eventId))
                // Since we're retrying, start a new transaction
                analyticsService.startLongRunningTransaction(AnalyticsLongRunningTransaction.PushToWorkManager(eventId), parent)
            }
            Timber.w("FetchNotificationsWorker will retry. Has network connectivity: $hasNetwork. Is network blocked: $isNetworkBlocked")
            true
        } else {
            false
        }
    }

    @ContributesIntoMap(AppScope::class, binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>())
    @WorkerKey(FetchPendingNotificationsWorker::class)
    @AssistedFactory
    interface Factory : MetroWorkerFactory.WorkerInstanceFactory<FetchPendingNotificationsWorker>
}

private fun <T : AnalyticsTransaction> Collection<T>.finish() = forEach { it.finish() }
