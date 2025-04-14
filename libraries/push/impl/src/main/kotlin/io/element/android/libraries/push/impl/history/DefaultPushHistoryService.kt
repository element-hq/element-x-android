/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.history

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.PushDatabase
import io.element.android.libraries.push.impl.db.PushHistory
import io.element.android.services.toolbox.api.systemclock.SystemClock
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultPushHistoryService @Inject constructor(
    private val pushDatabase: PushDatabase,
    private val systemClock: SystemClock,
) : PushHistoryService {
    override fun onPushReceived(
        providerInfo: String,
        eventId: EventId?,
        roomId: RoomId?,
        sessionId: SessionId?,
        hasBeenResolved: Boolean,
        comment: String?,
    ) {
        pushDatabase.pushHistoryQueries.insertPushHistory(
            PushHistory(
                pushDate = systemClock.epochMillis(),
                providerInfo = providerInfo,
                eventId = eventId?.value,
                roomId = roomId?.value,
                sessionId = sessionId?.value,
                hasBeenResolved = if (hasBeenResolved) 1 else 0,
                comment = comment,
            )
        )

        // Keep only the last 100 events
        pushDatabase.pushHistoryQueries.removeOldest(100)
    }
}
