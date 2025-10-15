/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.impl

import android.content.Context
import androidx.work.WorkManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.libraries.workmanager.api.workManagerTag
import timber.log.Timber

@ContributesBinding(AppScope::class)
@Inject
class DefaultWorkManagerScheduler(
    @ApplicationContext private val context: Context,
) : WorkManagerScheduler {
    override fun submit(workManagerRequest: WorkManagerRequest) {
        workManagerRequest.build().fold(
            onSuccess = {
                val workManager = WorkManager.Companion.getInstance(context)
                workManager.enqueue(it)
            },
            onFailure = {
                Timber.Forest.e(it, "Failed to build WorkManager request $workManagerRequest")
            }
        )
    }

    override fun cancel(sessionId: SessionId) {
        val workManager = WorkManager.Companion.getInstance(context)
        Timber.Forest.d("Cancelling work for sessionId: $sessionId")

        for (requestType in WorkManagerRequestType.entries) {
            workManager.cancelAllWorkByTag(workManagerTag(sessionId, requestType))
        }
    }
}
