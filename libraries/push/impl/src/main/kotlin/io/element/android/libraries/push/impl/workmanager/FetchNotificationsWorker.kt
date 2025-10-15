/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.di.WorkManagerNotificationBindings
import io.element.android.libraries.push.impl.di.WorkManagerPushBindings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

class FetchNotificationsWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Timber.d("FetchNotificationsWorker started")
        val rawRequestsJson = inputData.getString("requests") ?: return@withContext Result.failure()
        val requests = runCatchingExceptions {
            json.decodeFromString<List<SyncNotificationWorkManagerRequest.Data>>(rawRequestsJson).map { it.toRequest() }
        }.getOrElse {
            Timber.e(it, "Failed to deserialize notification requests")
            return@withContext Result.failure()
        }

        Timber.d("Deserialized ${requests.size} requests")

        val workManagerBindings = applicationContext.bindings<WorkManagerPushBindings>()

        val networkMonitor = applicationContext.bindings<WorkManagerNotificationBindings>().networkMonitor()

        // Wait for network to be available, but not more than 10 seconds
        val hasNetwork = withTimeoutOrNull(10.seconds) {
            networkMonitor.connectivity.first { it == NetworkStatus.Connected }
        } != null
        if (!hasNetwork) {
            Timber.w("No network, retrying later")
            return@withContext Result.retry()
        }

        val eventResolver = workManagerBindings.notifiableEventResolver()
        val queue = workManagerBindings.notifiableEventQueue()

        val failedSyncForSessions = mutableSetOf<SessionId>()

        val groupedRequests = requests.groupBy { it.sessionId }
        for ((sessionId, notificationRequests) in groupedRequests) {
            Timber.d("Processing notification requests for session $sessionId")
            eventResolver.resolveEvents(sessionId, notificationRequests)
                .fold(
                    onSuccess = { result ->
                        // Update the resolved results in the queue
                        (queue.results as MutableSharedFlow).emit(requests to result)
                    },
                    onFailure = {
                        failedSyncForSessions += sessionId
                        Timber.e(it, "Failed to resolve notification events for session $sessionId")
                    }
                )
        }

        // If there were failures for whole sessions, we retry all their requests
        if (failedSyncForSessions.isNotEmpty()) {
            val scheduler = workManagerBindings.workManagerScheduler()
            for (failedSessionId in failedSyncForSessions) {
                val requestsToRetry = groupedRequests[failedSessionId] ?: continue
                Timber.d("Re-scheduling ${requestsToRetry.size} failed notification requests for session $failedSessionId")
                scheduler.submit(SyncNotificationWorkManagerRequest(failedSessionId, requestsToRetry))
            }
        }

        Timber.d("Notifications processed successfully")

        performOpportunisticSyncIfNeeded(workManagerBindings, groupedRequests)

        Result.success()
    }

    private suspend fun performOpportunisticSyncIfNeeded(
        workManagerPushBindings: WorkManagerPushBindings,
        groupedRequests: Map<SessionId, List<NotificationEventRequest>>,
    ) {
        val syncOnNotifiableEvent = workManagerPushBindings.syncOnNotifiableEvent()

        for ((sessionId, notificationRequests) in groupedRequests) {
            runCatchingExceptions {
                syncOnNotifiableEvent(notificationRequests)
            }.onFailure {
                Timber.e(it, "Failed to sync on notifiable events for session $sessionId")
            }
        }
    }
}
