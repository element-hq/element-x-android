/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.api

import io.element.android.libraries.matrix.api.core.SessionId

interface WorkManagerScheduler {
    /**
     * Submits a new work request built from [workManagerRequestBuilder] to run in `WorkManager`.
     */
    suspend fun submit(workManagerRequestBuilder: WorkManagerRequestBuilder)

    /**
     * Checks if there are any pending requests scheduled for the provided [sessionId] and [requestType].
     */
    fun hasPendingWork(sessionId: SessionId, requestType: WorkManagerRequestType): Boolean

    /**
     * Cancel pending work requests for the session [SessionId].
     * If [requestType] is provided, it will only cancel requests for that type, otherwise it will cancel all requests.
     */
    fun cancel(sessionId: SessionId, requestType: WorkManagerRequestType? = null)
}

fun workManagerTag(sessionId: SessionId, requestType: WorkManagerRequestType): String {
    val prefix = when (requestType) {
        WorkManagerRequestType.NOTIFICATION_SYNC -> "notifications"
        WorkManagerRequestType.DB_VACUUM -> "db_vacuum"
    }
    return "$prefix-$sessionId"
}

enum class WorkManagerRequestType {
    NOTIFICATION_SYNC,
    DB_VACUUM,
}
