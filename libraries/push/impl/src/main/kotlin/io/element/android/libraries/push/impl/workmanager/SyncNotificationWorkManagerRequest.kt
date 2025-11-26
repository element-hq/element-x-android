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
import androidx.work.WorkRequest
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.workManagerTag
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.security.InvalidParameterException

class SyncNotificationWorkManagerRequest(
    private val sessionId: SessionId,
    private val notificationEventRequests: List<NotificationEventRequest>,
    private val workerDataConverter: WorkerDataConverter,
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
) : WorkManagerRequest {
    override fun build(): Result<List<WorkRequest>> {
        if (notificationEventRequests.isEmpty()) {
            return Result.failure(InvalidParameterException("notificationEventRequests cannot be empty"))
        }
        Timber.d("Scheduling ${notificationEventRequests.size} notification requests with WorkManager for $sessionId")
        return workerDataConverter.serialize(notificationEventRequests).map { dataList ->
            dataList.map { data ->
                OneTimeWorkRequestBuilder<FetchNotificationsWorker>()
                    .setInputData(data)
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
            }
        }
    }

    @Serializable
    data class Data(
        @SerialName("session_id")
        val sessionId: String,
        @SerialName("room_id")
        val roomId: String,
        @SerialName("event_id")
        val eventId: String,
        @SerialName("provider_info")
        val providerInfo: String,
    )
}
