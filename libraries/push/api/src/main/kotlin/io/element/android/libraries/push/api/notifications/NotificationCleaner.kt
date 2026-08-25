/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId

/**
 * Dismisses notifications that are already displayed, typically because the user has now seen the events elsewhere.
 *
 * Every method also removes the grouping summary notification when it is left without children.
 */
interface NotificationCleaner {
    /**
     * Dismisses every message notification of a session.
     *
     * @param sessionId the session whose notifications are dismissed.
     */
    fun clearAllMessagesEvents(sessionId: SessionId)

    /**
     * Dismisses the message notifications of one room.
     *
     * @param sessionId the session the room belongs to.
     * @param roomId the room whose notifications are dismissed.
     */
    fun clearMessagesForRoom(sessionId: SessionId, roomId: RoomId)

    /**
     * Dismisses the message notifications of one thread, leaving the rest of the room's notifications in place.
     *
     * @param sessionId the session the room belongs to.
     * @param roomId the room the thread belongs to.
     * @param threadId the thread whose notifications are dismissed.
     */
    fun clearMessagesForThread(sessionId: SessionId, roomId: RoomId, threadId: ThreadId)

    /**
     * Dismisses the notification of a single event.
     *
     * @param sessionId the session the event belongs to.
     * @param eventId the event whose notification is dismissed.
     */
    fun clearEvent(sessionId: SessionId, eventId: EventId)

    /**
     * Dismisses every membership notification of a session, i.e. the invitations and the join requests.
     *
     * @param sessionId the session whose notifications are dismissed.
     */
    fun clearMembershipNotificationForSession(sessionId: SessionId)

    /**
     * Dismisses the membership notifications of one room.
     *
     * @param sessionId the session the room belongs to.
     * @param roomId the room whose notifications are dismissed.
     */
    fun clearMembershipNotificationForRoom(sessionId: SessionId, roomId: RoomId)
}
