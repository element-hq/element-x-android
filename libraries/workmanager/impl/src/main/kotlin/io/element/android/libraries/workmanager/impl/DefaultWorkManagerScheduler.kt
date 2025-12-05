/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.impl

import androidx.work.WorkManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.sessionstorage.api.observer.SessionListener
import io.element.android.libraries.sessionstorage.api.observer.SessionObserver
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.libraries.workmanager.api.workManagerTag
import timber.log.Timber

@ContributesBinding(AppScope::class)
class DefaultWorkManagerScheduler(
    lazyWorkManager: Lazy<WorkManager>,
    sessionObserver: SessionObserver,
) : WorkManagerScheduler {
    private val workManager by lazyWorkManager

    init {
        // Observe session removals to cancel associated work automatically
        sessionObserver.addListener(object : SessionListener {
            override suspend fun onSessionDeleted(userId: String, wasLastSession: Boolean) {
                val sessionId = SessionId(userId)
                Timber.d("Session deleted for userId: $userId, cancelling associated workmanager requests")
                cancel(sessionId)
            }
        })
    }

    override fun submit(workManagerRequest: WorkManagerRequest) {
        workManagerRequest.build().fold(
            onSuccess = { workRequests ->
                workManager.enqueue(workRequests)
            },
            onFailure = {
                Timber.e(it, "Failed to build WorkManager request $workManagerRequest")
            }
        )
    }

    override fun cancel(sessionId: SessionId) {
        Timber.d("Cancelling work for sessionId: $sessionId")
        for (requestType in WorkManagerRequestType.entries) {
            workManager.cancelAllWorkByTag(workManagerTag(sessionId, requestType))
        }
    }
}
