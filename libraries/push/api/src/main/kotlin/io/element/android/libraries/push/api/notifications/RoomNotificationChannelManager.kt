/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.api.notifications

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Manages a per-room Android `NotificationChannel`, created automatically the first time a room
 * produces a noisy notification - or the first time a message is sent in a room whose
 * notification mode is `ALL_MESSAGES`, so a room you've only ever sent messages in (e.g. a
 * self-chat, which never notifies its own sender) still gets one. It is linked to the app's
 * shared "noisy" channel via `setConversationId`, which is what makes the room eligible for
 * Android's Conversations UI (grouped shade section, Priority Conversation toggle in system
 * Settings) - without it, a room's notification is never listed there no matter how it's
 * otherwise built (shortcut, `MessagingStyle`, etc).
 *
 * Either way, a channel is only ever created by passing [noisy][getChannelIdForRoom] `= true`:
 * some Matrix push rule modes (e.g. "mentions only") vary noisy per-event within the same room,
 * and a channel's importance can never change after creation, so creating one from a silent
 * event (or a room whose mode isn't `ALL_MESSAGES`) would permanently silence a room's future
 * mentions. A silent notification, or a send in a non-`ALL_MESSAGES` room, keeps using the app's
 * plain shared channel exactly as before this existed.
 */
interface RoomNotificationChannelManager {
    /**
     * Returns the channel id to notify on for [roomId]: its own channel if [noisy] (creating it
     * on first use), otherwise the app's shared channel (same as
     * [io.element.android.libraries.push.impl.notifications.channels.NotificationChannels.getChannelIdForMessage]).
     * A created channel is filed under the "Private chats" or "Rooms" system channel group
     * depending on [isDm], so it doesn't fall into Android's generic "Other" bucket for ungrouped
     * channels.
     */
    suspend fun getChannelIdForRoom(sessionId: SessionId, roomId: RoomId, roomDisplayName: String, isDm: Boolean, noisy: Boolean): String

    /** Deletes [roomId]'s channel (if any) and its persisted last-notified state. Idempotent. */
    suspend fun clearRoomChannel(sessionId: SessionId, roomId: RoomId)

    /** Deletes channels for rooms no longer in [roomIds] (left via another device/client, etc). */
    suspend fun pruneChannelsForSession(sessionId: SessionId, roomIds: Set<RoomId>)

    /** Deletes every per-room channel for [sessionId]. Call on logout. */
    suspend fun clearAllChannelsForSession(sessionId: SessionId)

    /**
     * Retires long-inactive, unmodified per-room channels for [sessionId], keeping the total
     * bounded, since Android gives apps no automatic garbage collection for channels.
     *
     * A channel is skipped (never deleted) if either:
     * - the user has marked it a Priority Conversation ([android.app.NotificationChannel.isImportantConversation]), or
     * - its live importance/sound/vibration/lights no longer match what this manager would create,
     *   meaning the user (or something else) changed it directly via system Settings.
     *
     * Among the remaining, unprotected candidates, a channel is deleted if it hasn't notified in
     * over 30 days, or - if the session still has more than 50 remaining after that - the oldest
     * ones are removed down to that limit.
     */
    suspend fun pruneInactiveChannels(sessionId: SessionId)
}
