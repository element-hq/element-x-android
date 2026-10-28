/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.db.PushRequest
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.services.appnavstate.api.AppForegroundStateService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@ContributesBinding(AppScope::class)
class DefaultSyncOnNotifiableEvent(
    private val matrixClientProvider: MatrixClientProvider,
    private val featureFlagService: FeatureFlagService,
    private val appForegroundStateService: AppForegroundStateService,
    private val dispatchers: CoroutineDispatchers,
    private val pushHistoryService: PushHistoryService,
) : SyncOnNotifiableEvent {
    override suspend operator fun invoke(requests: List<PushRequest>) = withContext(dispatchers.io) {
        if (!featureFlagService.isFeatureEnabled(FeatureFlags.SyncOnPush)) {
            return@withContext
        }

        try {
            val eventsBySession = requests.groupBy { it.sessionId }

            appForegroundStateService.updateIsSyncingNotificationEvent(true)
            Timber.d("Starting opportunistic room list sync | In foreground: ${appForegroundStateService.isInForeground.value}")

            for ((sessionId, events) in eventsBySession) {
                val client = matrixClientProvider.getOrRestore(SessionId(sessionId)).getOrNull() ?: continue
                val roomIds = events.map { RoomId(it.roomId) }.distinct()

                client.roomListService.subscribeToVisibleRooms(roomIds)

                if (!appForegroundStateService.isInForeground.value) {
                    // Give the sync some time to complete in background, but stop early if a newer push is waiting: the work fetching it can only
                    // start once this one is done, and an incoming call can't wait that long to ring.
                    // Pushes handled by this work and pending again after a network error keep their push date, so they don't end it early.
                    val newestHandledPushDate = Instant.fromEpochMilliseconds(events.maxOf { it.pushDate })
                    val newPushQueued = withTimeoutOrNull(10.seconds) {
                        pushHistoryService.getPendingPushRequestsFlow(SessionId(sessionId), since = newestHandledPushDate).first { it.isNotEmpty() }
                    } != null
                    if (newPushQueued) {
                        Timber.d("A newer push is waiting to be fetched, ending the opportunistic sync early")
                    }
                }
            }
        } finally {
            Timber.d("Finished opportunistic room list sync")
            appForegroundStateService.updateIsSyncingNotificationEvent(false)
        }
    }
}
