/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            ?.getNotification(sessionId, roomId, eventId)
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
