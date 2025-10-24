/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.ForegroundServiceType
import io.element.android.libraries.push.api.notifications.NotificationIdProvider
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.api.push.SyncOnNotifiableEvent
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.api.di.WorkerKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
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
    private val coroutineDispatchers: CoroutineDispatchers,
    private val notificationChannels: NotificationChannels,
    private val workerDataConverter: WorkerDataConverter,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(coroutineDispatchers.io) {
        Timber.d("FetchNotificationsWorker started")
        val requests = workerDataConverter.deserialize(inputData) ?: return@withContext Result.failure()
        // Wait for network to be available, but not more than 10 seconds
        val hasNetwork = withTimeoutOrNull(10.seconds) {
            networkMonitor.connectivity.first { it == NetworkStatus.Connected }
        } != null
        if (!hasNetwork) {
            Timber.w("No network, retrying later")
            return@withContext Result.retry()
        }

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
            for (failedSessionId in failedSyncForSessions) {
                val requestsToRetry = groupedRequests[failedSessionId] ?: continue
                Timber.d("Re-scheduling ${requestsToRetry.size} failed notification requests for session $failedSessionId")
                workManagerScheduler.submit(
                    SyncNotificationWorkManagerRequest(
                        sessionId = failedSessionId,
                        notificationEventRequests = requestsToRetry,
                        workerDataConverter = workerDataConverter,
                    )
                )
            }
        }

        Timber.d("Notifications processed successfully")

        performOpportunisticSyncIfNeeded(groupedRequests)

        Result.success()
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

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notificationChannelId = notificationChannels.getChannelIdForMessage(false)
        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(CommonDrawables.ic_notification)
            .setOngoing(true)
            .setTicker("Fetching notifications...")
            .setContentTitle("Fetching notifications...")
            .setContentText("Loading the events associated with received notifications.")
            .build()
        val notificationId = NotificationIdProvider.getForegroundServiceNotificationId(ForegroundServiceType.NOTIFICATION_FETCHING)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_REMOTE_MESSAGING)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    @ContributesIntoMap(AppScope::class, binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>())
    @WorkerKey(FetchNotificationsWorker::class)
    @AssistedFactory
    interface Factory : MetroWorkerFactory.WorkerInstanceFactory<FetchNotificationsWorker>
}
