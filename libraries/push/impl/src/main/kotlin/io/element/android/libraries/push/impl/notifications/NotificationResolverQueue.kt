/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.push.api.push.GroupedNotificationEventRequest
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.impl.workmanager.GroupedSyncNotificationsWorkerDataConverter
import io.element.android.libraries.push.impl.workmanager.SyncNotificationWorkManagerRequestFactory
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

interface NotificationResolverQueue {
    val results: SharedFlow<Pair<GroupedNotificationEventRequest, Map<EventId, Result<ResolvedPushEvent>>>>
    suspend fun enqueue(request: NotificationEventRequest)
}

/**
 * This class is responsible for periodically batching notification requests and resolving them in a single call,
 * so that we can avoid having to resolve each notification individually in the SDK.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultNotificationResolverQueue(
    private val notifiableEventResolver: NotifiableEventResolver,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
    private val workManagerScheduler: WorkManagerScheduler,
    private val workerDataConverter: GroupedSyncNotificationsWorkerDataConverter,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : NotificationResolverQueue {
    companion object {
        private const val BATCH_WINDOW_MS = 250L
    }

    private val requestQueue = Channel<NotificationEventRequest>(capacity = 100)

    private var currentProcessingJob: Job? = null

    /**
     * A flow that emits pairs of a list of notification event requests and a map of the resolved events.
     * The map contains the original request as the key and the resolved event as the value.
     */
    override val results = MutableSharedFlow<Pair<GroupedNotificationEventRequest, Map<EventId, Result<ResolvedPushEvent>>>>()

    /**
     * Enqueues a notification event request to be resolved.
     * The request will be processed in batches, so it may not be resolved immediately.
     *
     * @param request The notification event request to enqueue.
     */
    override suspend fun enqueue(request: NotificationEventRequest) {
        // Cancel previous processing job if it exists, acting as a debounce operation
        Timber.d("Cancelling job: $currentProcessingJob")
        currentProcessingJob?.cancel()

        // Enqueue the request and start a delayed processing job
        requestQueue.send(request)
        currentProcessingJob = processQueue()
        Timber.d("Starting processing job for request: $request")
    }

    private fun processQueue() = appCoroutineScope.launch(SupervisorJob()) {
        delay(BATCH_WINDOW_MS.milliseconds)

        // If this job is still active (so this is the latest job), we launch a separate one that won't be cancelled when enqueueing new items
        // to process the existing queued items.
        appCoroutineScope.launch {
            val groupedRequestsById = buildList {
                while (!requestQueue.isEmpty) {
                    requestQueue.receiveCatching().getOrNull()?.let(::add)
                }
            }.groupBy { it.sessionId }

            for ((sessionId, requests) in groupedRequestsById) {
                workManagerScheduler.submit(
                    SyncNotificationWorkManagerRequestFactory(
                        sessionId = sessionId,
                        notificationEventRequests = requests,
                        workerDataConverter = workerDataConverter,
                        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
                    )
                )
            }
        }
    }
}
