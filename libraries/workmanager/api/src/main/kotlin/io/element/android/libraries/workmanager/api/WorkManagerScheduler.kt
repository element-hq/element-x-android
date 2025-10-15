/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.workmanager.api

import io.element.android.libraries.matrix.api.core.SessionId

interface WorkManagerScheduler {
    fun submit(workManagerRequest: WorkManagerRequest)
    fun cancel(sessionId: SessionId)
}

fun workManagerTag(sessionId: SessionId, requestType: WorkManagerRequestType): String {
    val prefix = when (requestType) {
        WorkManagerRequestType.NOTIFICATION_SYNC -> "notifications"
    }
    return "$prefix-$sessionId"
}

enum class WorkManagerRequestType {
    NOTIFICATION_SYNC,
}
