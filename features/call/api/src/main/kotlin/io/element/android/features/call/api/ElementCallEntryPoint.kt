/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.api

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Entry point for the call feature.
 */
interface ElementCallEntryPoint {
    /**
     * Start a call of the given type.
     * @param callType The type of call to start.
     */
    fun startCall(callType: CallType)

    /**
     * Handle an incoming call.
     * @param callType The type of call.
     * @param eventId The event id of the event that started the call.
     * @param senderId The user id of the sender of the event that started the call.
     * @param roomName The name of the room the call is in.
     * @param senderName The name of the sender of the event that started the call.
     * @param avatarUrl The avatar url of the room or DM.
     * @param timestamp The timestamp of the event that started the call.
     * @param notificationChannelId The id of the notification channel to use for the call notification.
     */
    fun handleIncomingCall(
        callType: CallType.RoomCall,
        eventId: EventId,
        senderId: UserId,
        roomName: String?,
        senderName: String?,
        avatarUrl: String?,
        timestamp: Long,
        notificationChannelId: String,
    )
}
