/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkRequest
import androidx.work.workDataOf
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequest
import io.element.android.libraries.workmanager.api.WorkManagerRequestType
import io.element.android.libraries.workmanager.api.workManagerTag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.security.InvalidParameterException

class SyncNotificationWorkManagerRequest(
    private val sessionId: SessionId,
    private val notificationEventRequests: List<NotificationEventRequest>,
) : WorkManagerRequest {
    private val json = Json { ignoreUnknownKeys = true }

    override fun build(): Result<WorkRequest> {
        if (notificationEventRequests.isEmpty()) {
            return Result.failure(InvalidParameterException("notificationEventRequests cannot be empty"))
        }

        val json = runCatchingExceptions { json.encodeToString(notificationEventRequests.map { it.toData() }) }
            .getOrElse {
                Timber.e(it, "Failed to serialize notification requests")
                return Result.failure(it)
            }

        Timber.d("Scheduling ${notificationEventRequests.size} notification requests with WorkManager for $sessionId")

        return Result.success(
            OneTimeWorkRequestBuilder<FetchNotificationsWorker>()
                .setInputData(workDataOf("requests" to json))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setTraceTag(workManagerTag(sessionId, WorkManagerRequestType.NOTIFICATION_SYNC))
                // TODO investigate using this instead of the resolver queue
    //            .setInputMerger()
                .build()
        )
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
    ) {
        fun toRequest(): NotificationEventRequest {
            return NotificationEventRequest(
                sessionId = SessionId(sessionId),
                roomId = RoomId(roomId),
                eventId = EventId(eventId),
                providerInfo = providerInfo,
            )
        }
    }
}

private fun NotificationEventRequest.toData(): SyncNotificationWorkManagerRequest.Data {
    return SyncNotificationWorkManagerRequest.Data(
        sessionId = sessionId.value,
        roomId = roomId.value,
        eventId = eventId.value,
        providerInfo = providerInfo,
    )
}
