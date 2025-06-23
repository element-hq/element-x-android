/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class SyncOnNotifiableEvent @Inject constructor(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
) {
    suspend operator fun invoke(notifiableEvents: List<NotifiableEvent>) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }

        try {
            val eventsBySession = notifiableEvents.groupBy { it.sessionId }

            appForegroundStateService.updateIsSyncingNotificationEvent(true)
            Timber.d("Starting opportunistic room list sync | In foreground: ${appForegroundStateService.isInForeground.value}")

            for ((sessionId, events) in eventsBySession) {
                val client = matrixClientProvider.getOrRestore(sessionId).getOrNull() ?: continue
                val eventsByRoomId = events.groupBy { it.roomId }

                client.roomListService.subscribeToVisibleRooms(eventsByRoomId.keys.toList())

                if (!appForegroundStateService.isInForeground.value) {
                    // Give the sync some time to complete in background
                    delay(10.seconds)
                }
            }
        } finally {
            Timber.d("Finished opportunistic room list sync")
            appForegroundStateService.updateIsSyncingNotificationEvent(false)
        }
    }
}
