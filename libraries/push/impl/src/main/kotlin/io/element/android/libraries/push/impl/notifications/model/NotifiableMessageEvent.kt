/*
 * Copyright (c) 2023 New Vector Ltd
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
package io.element.android.libraries.push.impl.notifications.model

import android.net.Uri
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventType
import io.element.android.services.appnavstate.api.AppNavigationState
import io.element.android.services.appnavstate.api.currentRoomId
import io.element.android.services.appnavstate.api.currentSessionId
import io.element.android.services.appnavstate.api.currentThreadId

data class NotifiableMessageEvent(
    override val sessionId: SessionId,
    override val roomId: RoomId,
    override val eventId: EventId,
    override val editedEventId: EventId?,
    override val canBeReplaced: Boolean,
    val senderId: UserId,
    val noisy: Boolean,
    val timestamp: Long,
    val senderDisambiguatedDisplayName: String?,
    val body: String?,
    // We cannot use Uri? type here, as that could trigger a
    // NotSerializableException when persisting this to storage
    val imageUriString: String?,
    val threadId: ThreadId?,
    val roomName: String?,
    val roomIsDirect: Boolean = false,
    val roomAvatarPath: String? = null,
    val senderAvatarPath: String? = null,
    val soundName: String? = null,
    // This is used for >N notification, as the result of a smart reply
    val outGoingMessage: Boolean = false,
    val outGoingMessageFailed: Boolean = false,
    override val isRedacted: Boolean = false,
    override val isUpdated: Boolean = false,
    val type: String = EventType.MESSAGE
) : NotifiableEvent {
    override val description: String = body ?: ""

    // Example of value:
    // content://io.element.android.x.debug.notifications.fileprovider/downloads/temp/notif/matrix.org/XGItzSDOnSyXjYtOPfiKexDJ
    val imageUri: Uri?
        get() = imageUriString?.let { Uri.parse(it) }
}

/**
 * Used to check if a notification should be ignored based on the current app and navigation state.
 */
fun NotifiableEvent.shouldIgnoreEventInRoom(appNavigationState: AppNavigationState): Boolean {
    val currentSessionId = appNavigationState.navigationState.currentSessionId() ?: return false
    return when (val currentRoomId = appNavigationState.navigationState.currentRoomId()) {
        null -> false
        else -> {
            // Never ignore ringing call notifications
            if (this is NotifiableRingingCallEvent) {
                false
            } else {
                appNavigationState.isInForeground &&
                    sessionId == currentSessionId &&
                    roomId == currentRoomId &&
                    (this as? NotifiableMessageEvent)?.threadId == appNavigationState.navigationState.currentThreadId()
            }
        }
    }
}
