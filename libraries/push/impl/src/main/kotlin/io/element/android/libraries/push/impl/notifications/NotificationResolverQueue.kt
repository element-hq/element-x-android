/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
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
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * This class is responsible for periodically batching notification requests and resolving them in a single call,
 * so that we can avoid having to resolve each notification individually in the SDK.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
class NotificationResolverQueue @Inject constructor(
    private val notifiableEventResolver: NotifiableEventResolver,
    @AppCoroutineScope
    private val appCoroutineScope: CoroutineScope,
) {
    companion object {
        private const val BATCH_WINDOW_MS = 250L
    }
    private val requestQueue = Channel<NotificationEventRequest>(capacity = 100)

    private var currentProcessingJob: Job? = null

    /**
     * A flow that emits pairs of a list of notification event requests and a map of the resolved events.
     * The map contains the original request as the key and the resolved event as the value.
     */
    val results: SharedFlow<Pair<List<NotificationEventRequest>, Map<NotificationEventRequest, Result<ResolvedPushEvent>>>> = MutableSharedFlow()

    /**
     * Enqueues a notification event request to be resolved.
     * The request will be processed in batches, so it may not be resolved immediately.
     *
     * @param request The notification event request to enqueue.
     */
    suspend fun enqueue(request: NotificationEventRequest) {
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
                    requestQueue.receiveCatching().getOrNull()?.let(this::add)
                }
            }.groupBy { it.sessionId }

            val sessionIds = groupedRequestsById.keys
            for (sessionId in sessionIds) {
                val requests = groupedRequestsById[sessionId].orEmpty()
                Timber.d("Fetching notifications for $sessionId: $requests. Pending requests: ${!requestQueue.isEmpty}")
                // Resolving the events in parallel should improve performance since each session id will query a different Client
                launch {
                    // No need for a Mutex since the SDK already has one internally
                    val notifications = notifiableEventResolver.resolveEvents(sessionId, requests).getOrNull().orEmpty()
                    (results as MutableSharedFlow).emit(requests to notifications)
                }
            }
        }
    }
}

data class NotificationEventRequest(
    val sessionId: SessionId,
    val roomId: RoomId,
    val eventId: EventId,
    val providerInfo: String,
)
