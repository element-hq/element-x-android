/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
     * @param callData The data of call to start.
     */
    fun startCall(callData: CallData)

    /**
     * Start (or join) a Push-to-Talk session for the given room, headlessly: the full-screen call
     * UI is never shown and the user stays in the room. Audio-only.
     * @param callData The data of the session to start.
     */
    fun startPttSession(callData: CallData)

    /**
     * Leave the current Push-to-Talk session (if any), tearing down the headless host.
     */
    fun stopPttSession()

    /**
     * Handle an incoming call.
     * @param callData The data of call.
     * @param eventId The event id of the event that started the call.
     * @param senderId The user id of the sender of the event that started the call.
     * @param roomName The name of the room the call is in.
     * @param senderName The name of the sender of the event that started the call.
     * @param avatarUrl The avatar url of the room or DM.
     * @param timestamp The timestamp of the event that started the call.
     * @param expirationTimestamp The timestamp at which the call should stop ringing.
     * @param notificationChannelId The id of the notification channel to use for the call notification.
     * @param textContent The text content of the notification. If null the default content from the system will be used.
     */
    suspend fun handleIncomingCall(
        callData: CallData,
        eventId: EventId,
        senderId: UserId,
        roomName: String?,
        senderName: String?,
        avatarUrl: String?,
        timestamp: Long,
        expirationTimestamp: Long,
        notificationChannelId: String,
        textContent: String?,
    )
}
