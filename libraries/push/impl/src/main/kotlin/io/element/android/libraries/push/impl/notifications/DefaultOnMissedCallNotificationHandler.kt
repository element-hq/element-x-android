/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.notifications.OnMissedCallNotificationHandler
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultOnMissedCallNotificationHandler @Inject constructor(
    private val matrixClientProvider: MatrixClientProvider,
    private val defaultNotificationDrawerManager: DefaultNotificationDrawerManager,
    private val callNotificationEventResolver: CallNotificationEventResolver,
) : OnMissedCallNotificationHandler {
    override suspend fun addMissedCallNotification(
        sessionId: SessionId,
        roomId: RoomId,
        eventId: EventId,
    ) {
        // Resolve the event and add a notification for it, at this point it should no longer be a ringing one
        val notificationData = matrixClientProvider.getOrRestore(sessionId).getOrNull()
            ?.notificationService()
            ?.getNotification(roomId, eventId)
            ?.getOrNull()
            ?: return

        val notifiableEvent = callNotificationEventResolver.resolveEvent(
            sessionId = sessionId,
            notificationData = notificationData,
            // Make sure the notifiable event is not a ringing one
            forceNotify = true,
        )
        notifiableEvent?.let { defaultNotificationDrawerManager.onNotifiableEventReceived(it) }
    }
}
