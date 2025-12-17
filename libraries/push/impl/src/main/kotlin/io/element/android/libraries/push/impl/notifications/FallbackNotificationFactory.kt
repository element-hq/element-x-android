/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.R
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.services.toolbox.api.systemclock.SystemClock

@Inject
class FallbackNotificationFactory(
    private val clock: SystemClock,
    private val stringProvider: StringProvider,
) {
    fun create(
        sessionId: SessionId,
        roomId: RoomId,
        eventId: EventId,
        cause: String?,
    ): FallbackNotifiableEvent = FallbackNotifiableEvent(
        sessionId = sessionId,
        roomId = roomId,
        eventId = eventId,
        editedEventId = null,
        canBeReplaced = true,
        isRedacted = false,
        isUpdated = false,
        timestamp = clock.epochMillis(),
        description = stringProvider.getString(R.string.notification_fallback_content),
        cause = cause,
    )
}
