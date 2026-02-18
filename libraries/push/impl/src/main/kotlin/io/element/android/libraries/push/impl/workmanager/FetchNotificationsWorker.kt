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
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.auth.SessionRestorationException
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.GroupedNotificationEventRequest
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.api.push.SyncOnNotifiableEvent
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
import io.element.android.libraries.push.impl.notifications.ResolveGroupedNotificationsResult
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.libraries.workmanager.api.di.MetroWorkerFactory
import io.element.android.libraries.workmanager.api.di.WorkerKey
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

private const val MAX_RETRY_ATTEMPTS = 3
private const val TAG = "FetchNotificationsWorker"

@AssistedInject
class FetchNotificationsWorker(
    @Assisted private val workerParams: WorkerParameters,
    @ApplicationContext private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val eventResolver: NotifiableEventResolver,
    private val queue: NotificationResolverQueue,
    private val workManagerScheduler: WorkManagerScheduler,
    private val syncOnNotifiableEvent: SyncOnNotifiableEvent,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val workerDataConverter: GroupedSyncNotificationsWorkerDataConverter,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(coroutineDispatchers.io) {
        Timber.tag(TAG).d("FetchNotificationsWorker started")
        val requests = workerDataConverter.deserialize(inputData)?.toMutableList() ?: return@withContext Result.failure()
        // Wait for network to be available, but not more than 10 seconds
        val hasNetwork = withTimeoutOrNull(10.seconds) {
            networkMonitor.connectivity.first { it == NetworkStatus.Connected }
        } != null
        if (!hasNetwork) {
            if (workerParams.runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Timber.tag(TAG).w("No network, retrying later")
                return@withContext Result.retry()
            } else {
                Timber.tag(TAG).w("No network available and reached max retry attempts (${workerParams.runAttemptCount}/$MAX_RETRY_ATTEMPTS)")
                return@withContext Result.failure()
            }
        }

        val failedSyncForSessions = mutableMapOf<SessionId, Throwable>()

        for (groupedRequests in requests) {
            Timber.tag(TAG).d("Processing notification requests for session ${groupedRequests.sessionId}")
            val result = eventResolver.resolveEvents(groupedRequests)
                when (result) {
                    is ResolveGroupedNotificationsResult.Success -> {
                        // Update the resolved results in the queue
                        (queue.results as MutableSharedFlow).emit(groupedRequests to result.eventResults)
                    }
                    is ResolveGroupedNotificationsResult.Error -> {
                        failedSyncForSessions[groupedRequests.sessionId] = result.throwable
                        Timber.e(result.throwable, "Failed to resolve notification events for session ${groupedRequests.sessionId}")
                    }
                }
        }

        // If there were failures for whole sessions, we retry all their requests
        if (failedSyncForSessions.isNotEmpty()) {
            @Suppress("LoopWithTooManyJumpStatements")
            for ((failedSessionId, exception) in failedSyncForSessions) {
                if (exception.cause is SessionRestorationException) {
                    Timber.tag(TAG).e(exception, "Session $failedSessionId could not be restored, not retrying notification fetching")
                    requests.removeIf { it.sessionId == failedSessionId }
                    continue
                }

                val requestsToRetry = requests.filter { it.sessionId == failedSessionId }
                if (workerParams.runAttemptCount < MAX_RETRY_ATTEMPTS) {
                    Timber.tag(TAG).d("Re-scheduling ${requestsToRetry.size} failed notification requests for session $failedSessionId")
                    val splitNotificationRequests = requestsToRetry.flatMap { request ->
                        request.requestsByRoom.flatMap { (roomId, eventIds) ->
                            eventIds.map {
                                NotificationEventRequest(
                                    sessionId = request.sessionId,
                                    roomId = roomId,
                                    eventId = it,
                                    providerInfo = request.providerInfo,
                                )
                            }
                        }
                    }
                    workManagerScheduler.submit(
                        SyncNotificationWorkManagerRequestFactory(
                            sessionId = failedSessionId,
                            notificationEventRequests = splitNotificationRequests,
                            workerDataConverter = workerDataConverter,
                            buildVersionSdkIntProvider = buildVersionSdkIntProvider,
                        )
                    )
                }
            }
        }

        Timber.tag(TAG).d("Notifications processed successfully")

        performOpportunisticSyncIfNeeded(requests)

        Result.success()
    }

    private suspend fun performOpportunisticSyncIfNeeded(
        groupedRequests: List<GroupedNotificationEventRequest>,
    ) {
        for ((sessionId, notificationRequests) in groupedRequests) {
            runCatchingExceptions {
                syncOnNotifiableEvent(sessionId, notificationRequests.keys.toList())
            }.onFailure {
                Timber.tag(TAG).e(it, "Failed to sync on notifiable events for session $sessionId")
            }
        }
    }

    @ContributesIntoMap(AppScope::class, binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>())
    @WorkerKey(FetchNotificationsWorker::class)
    @AssistedFactory
    interface Factory : MetroWorkerFactory.WorkerInstanceFactory<FetchNotificationsWorker>
}
