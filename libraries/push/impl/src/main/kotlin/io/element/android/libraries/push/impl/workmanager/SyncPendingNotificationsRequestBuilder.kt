/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import android.os.Build
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.workDataOf
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.workmanager.api.WorkManagerRequestBuilder
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.WorkManagerRequestWrapper
import io.element.android.libraries.workmanager.api.WorkManagerWorkerType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider

class SyncPendingNotificationsRequestBuilder(
    private val sessionId: SessionId,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : WorkManagerRequestBuilder {
    companion object {
        const val SESSION_ID = "session_id"
    }

    override suspend fun build(): Result<List<WorkManagerRequestWrapper>> {
        val type = WorkManagerWorkerType.Unique(
            name = workManagerTag(sessionId = sessionId, requestType = WorkManagerRequestType.NOTIFICATION_SYNC),
            policy = ExistingWorkPolicy.APPEND_OR_REPLACE,
        )
        val request = OneTimeWorkRequestBuilder<FetchPendingNotificationsWorker>()
            .setInputData(workDataOf(SESSION_ID to sessionId.value))
            .apply {
                // Expedited workers aren't needed on Android 12 or lower:
                // They force displaying a foreground sync notification for no good reason, since they sync almost immediately anyway
                // See https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#backwards-compat
                if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
                    setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                }
            }
            .setTraceTag(workManagerTag(sessionId, WorkManagerRequestType.NOTIFICATION_SYNC))
            .build()
        return Result.success(listOf(WorkManagerRequestWrapper(request, type)))
    }
}
