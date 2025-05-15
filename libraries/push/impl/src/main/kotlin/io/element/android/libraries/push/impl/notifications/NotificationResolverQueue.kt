/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@SingleIn(AppScope::class)
class NotificationResolverQueue @Inject constructor(
    private val notifiableEventResolver: NotifiableEventResolver,
    private val coroutineScope: CoroutineScope,
) {
    companion object {
        private const val BATCH_WINDOW_MS = 250L
    }
    private val requestQueue = Channel<NotificationEventRequest>(capacity = 100)

    val results: SharedFlow<Pair<List<NotificationEventRequest>, List<ResolvedPushEvent>>> = MutableSharedFlow()

    init {
        coroutineScope.launch {
            while (coroutineScope.isActive) {
                // Wait for a batch of requests to be received in a specified time window
                delay(BATCH_WINDOW_MS.milliseconds)

                val groupedRequestsById = buildList {
                    while (!requestQueue.isEmpty) {
                        requestQueue.receiveCatching().getOrNull()?.let(this::add)
                    }
                }.groupBy { it.sessionId }

                val sessionIds = groupedRequestsById.keys
                for (sessionId in sessionIds) {
                    val requests = groupedRequestsById[sessionId].orEmpty()
                    Timber.d("Fetching notifications for $sessionId: $requests. Pending requests: ${!requestQueue.isEmpty}")

                    launch {
                        // No need for a Mutex since the SDK already has one internally
                        val notifications = notifiableEventResolver.resolveEvents(sessionId, requests).getOrNull().orEmpty()
                        (results as MutableSharedFlow).emit(requests to notifications.values.filterNotNull())
                    }
                }
            }
        }
    }

    suspend fun enqueue(request: NotificationEventRequest) {
        requestQueue.send(request)
    }
}

data class NotificationEventRequest(
    val sessionId: SessionId,
    val roomId: RoomId,
    val eventId: EventId,
    val providerInfo: String,
)
