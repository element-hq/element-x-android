/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.impl

import android.content.Context
import androidx.work.WorkManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerScheduler
import io.element.android.libraries.workmanager.api.workManagerTag
import timber.log.Timber

@ContributesBinding(AppScope::class)
class DefaultWorkManagerScheduler(
    @ApplicationContext private val context: Context,
) : WorkManagerScheduler {
    private val workManager by lazy { WorkManager.getInstance(context) }

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
