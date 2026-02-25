/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.OverwritingInputMerger
import androidx.work.WorkRequest
import androidx.work.workDataOf
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.security.InvalidParameterException

class SyncPendingNotificationsWorkManagerRequest(
    private val sessionId: SessionId,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : WorkManagerRequest {
    override suspend fun build(): Result<List<WorkRequest>> {
        return Result.success(
            listOf(
                OneTimeWorkRequestBuilder<FetchPendingNotificationsWorker>()
                    .setInputData(workDataOf("session_id" to sessionId.value))
                    .apply {
                        // Expedited workers aren't needed on Android 12 or lower:
                        // They force displaying a foreground sync notification for no good reason, since they sync almost immediately anyway
                        // See https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#backwards-compat
                        if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.TIRAMISU)) {
                            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        }
                    }
                    .setTraceTag(workManagerTag(sessionId, WorkManagerRequestType.NOTIFICATION_SYNC))
                    // TODO investigate using this instead of the resolver queue
                    // .setInputMerger()
                    .build()
            )
        )
    }
}
