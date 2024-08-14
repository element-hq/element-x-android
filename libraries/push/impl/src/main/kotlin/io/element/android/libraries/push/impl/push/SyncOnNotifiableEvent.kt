/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SyncOnNotifiableEvent @Inject constructor(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
) {
    private var syncCounter = AtomicInteger(0)

    suspend operator fun invoke(notifiableEvent: NotifiableEvent) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }
        val client = matrixClientProvider.getOrRestore(notifiableEvent.sessionId).getOrNull() ?: return@withContext
        client.getRoom(notifiableEvent.roomId)?.use { room ->
            room.subscribeToSync()

            // If the app is in foreground, sync is already running, so just add the subscription.
            if (!appForegroundStateService.isInForeground.value) {
                val syncService = client.syncService()
                syncService.startSyncIfNeeded()
                room.waitsUntilEventIsKnown(eventId = notifiableEvent.eventId, timeout = 10.seconds)
                syncService.stopSyncIfNeeded()
            }
        }
    }

    private suspend fun MatrixRoom.waitsUntilEventIsKnown(eventId: EventId, timeout: Duration) {
        withTimeoutOrNull(timeout) {
            liveTimeline.timelineItems.first { timelineItems ->
                timelineItems.any { timelineItem ->
                    when (timelineItem) {
                        is MatrixTimelineItem.Event -> timelineItem.eventId == eventId
                        else -> false
                    }
                }
            }
        }
    }

    private suspend fun SyncService.startSyncIfNeeded() {
        if (syncCounter.getAndIncrement() == 0) {
            startSync()
        }
    }

    private suspend fun SyncService.stopSyncIfNeeded() {
        if (syncCounter.decrementAndGet() == 0 && !appForegroundStateService.isInForeground.value) {
            stopSync()
        }
    }
}
