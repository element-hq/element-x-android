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
