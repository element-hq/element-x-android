/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.api

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId

/**
 * Builds the app's internal deep links, used to point a notification or a shortcut at a precise place in the app.
 */
fun interface DeepLinkCreator {
    /**
     * Builds a link to the most precise target the arguments allow, ignoring the deeper ones once an outer one is `null`.
     *
     * @param sessionId the session to open.
     * @param roomId the room to open, or `null` to stop at the room list.
     * @param threadId the thread to open within that room, or `null` for the main timeline.
     * @param eventId the event to focus on, or `null` to open at the latest message.
     */
    fun create(sessionId: SessionId, roomId: RoomId?, threadId: ThreadId?, eventId: EventId?): String
}
