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
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.matrix.api.exception.isNetworkError
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.api.push.SyncOnNotifiableEvent
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationResolverQueue
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
private val rescheduleDelay = 30.seconds
private const val TAG = "NotificationsWorker"

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
    private val workerDataConverter: SyncNotificationsWorkerDataConverter,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = withContext(coroutineDispatchers.io) {
        Timber.tag(TAG).d("FetchNotificationsWorker started")
        val canRetry = workerParams.runAttemptCount < MAX_RETRY_ATTEMPTS

        val requests = workerDataConverter.deserialize(inputData) ?: return@withContext Result.failure()
        // Wait for network to be available, but not more than 10 seconds
        val hasNetwork = withTimeoutOrNull(10.seconds) {
            networkMonitor.connectivity.first { it == NetworkStatus.Connected }
        } != null
        if (!hasNetwork) {
            Timber.tag(TAG).w("No network, re-scheduling to retry later")
            val sessionId = requests.first().sessionId
            workManagerScheduler.submit(
                SyncNotificationWorkManagerRequest(
                    sessionId = sessionId,
                    notificationEventRequests = requests,
                    workerDataConverter = workerDataConverter,
                    buildVersionSdkIntProvider = buildVersionSdkIntProvider,
                    delay = rescheduleDelay,
                )
            )
            return@withContext Result.failure()
        }

        val failedSyncForSessions = mutableMapOf<SessionId, Throwable>()

        val groupedRequests = requests.groupBy { it.sessionId }.toMutableMap()
        val recoverableFailedRequests = mutableSetOf<NotificationEventRequest>()
        for ((sessionId, notificationRequests) in groupedRequests) {
            Timber.tag(TAG).d("Processing notification requests for session $sessionId")
            eventResolver.resolveEvents(sessionId, notificationRequests)
                .fold(
                    onSuccess = { result ->
                        // Store failed but recoverable requests
                        recoverableFailedRequests.addAll(
                            result
                                .filter { (_, eventResult) ->
                                    val exception = eventResult.exceptionOrNull()
                                    exception is ClientException.Generic && exception.isNetworkError()
                                }
                                .map { it.key }
                        )

                        // Update the resolved results in the queue
                        (queue.results as MutableSharedFlow).emit(requests to result)
                    },
                    onFailure = {
                        failedSyncForSessions[sessionId] = it
                        Timber.e(it, "Failed to resolve notification events for session $sessionId")
                    }
                )
        }

        // Handle failures, re-schedule and retry/fail as needed
        handleFailures(
            canRetry = canRetry,
            requests = requests,
            recoverableFailedRequests = recoverableFailedRequests,
            failedSyncForSessions = failedSyncForSessions,
        )?.let { result ->
            return@withContext result
        }

        Timber.tag(TAG).d("Notifications processed successfully")

        performOpportunisticSyncIfNeeded(groupedRequests)

        Result.success()
    }

    private fun handleFailures(
        canRetry: Boolean,
        requests: List<NotificationEventRequest>,
        recoverableFailedRequests: Set<NotificationEventRequest>,
        failedSyncForSessions: Map<SessionId, Throwable>,
    ): Result? {
        val allRequestsFailed = recoverableFailedRequests == requests.toSet()
        if (allRequestsFailed) {
            return if (canRetry) Result.retry() else Result.failure()
        } else if (!canRetry) {
            return Result.failure()
        } else if (failedSyncForSessions.isNotEmpty()) {
            val groupedRequests = requests.groupBy { it.sessionId }.toMutableMap()
            @Suppress("LoopWithTooManyJumpStatements")
            for ((failedSessionId, exception) in failedSyncForSessions) {
                if (exception.cause is SessionRestorationException) {
                    Timber.tag(TAG).e(exception, "Session $failedSessionId could not be restored, not retrying notification fetching")
                    groupedRequests.remove(failedSessionId)
                    continue
                }
                val requestsToRetry = groupedRequests[failedSessionId] ?: continue
                Timber.tag(TAG).d("Re-scheduling ${requestsToRetry.size} failed notification requests for session $failedSessionId")
                workManagerScheduler.submit(
                    SyncNotificationWorkManagerRequest(
                        sessionId = failedSessionId,
                        notificationEventRequests = requestsToRetry,
                        workerDataConverter = workerDataConverter,
                        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
                        delay = rescheduleDelay,
                    )
                )
            }
        } else if (recoverableFailedRequests.isNotEmpty()) {
            val bySessionId = recoverableFailedRequests.groupBy { it.sessionId }
            for ((sessionId, failedRequests) in bySessionId) {
                Timber.tag(TAG).d("Re-scheduling ${recoverableFailedRequests.size} recoverable failed notification requests for $sessionId")
                workManagerScheduler.submit(
                    SyncNotificationWorkManagerRequest(
                        sessionId = sessionId,
                        notificationEventRequests = failedRequests,
                        workerDataConverter = workerDataConverter,
                        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
                        delay = rescheduleDelay,
                    )
                )
            }
        }

        return null
    }

    private suspend fun performOpportunisticSyncIfNeeded(
        groupedRequests: Map<SessionId, List<NotificationEventRequest>>,
    ) {
        for ((sessionId, notificationRequests) in groupedRequests) {
            runCatchingExceptions {
                syncOnNotifiableEvent(notificationRequests)
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
