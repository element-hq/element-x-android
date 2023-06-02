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
    val noisy: Boolean,
    val timestamp: Long,
    val senderName: String?,
    val senderId: String?,
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
    override val isUpdated: Boolean = false
) : NotifiableEvent {

    val type: String = EventType.MESSAGE
    val description: String = body ?: ""
    val title: String = senderName ?: ""

    // TODO EAx The image has to be downloaded and expose using the file provider.
    // Example of value from Element Android:
    // content://im.vector.app.debug.mx-sdk.fileprovider/downloads/downloads/816abf76d806c768760568952b1862c8/F/72c33edd23dee3b95f4d5a18aa25fa54/image.png
    val imageUri: Uri?
        get() = imageUriString?.let { Uri.parse(it) }
}

fun NotifiableMessageEvent.shouldIgnoreMessageEventInRoom(
    appNavigationState: AppNavigationState?
): Boolean {
    val currentSessionId = appNavigationState?.currentSessionId() ?: return false
    return when (val currentRoomId = appNavigationState.currentRoomId()) {
        null -> false
        else -> sessionId == currentSessionId && roomId == currentRoomId && threadId == appNavigationState.currentThreadId()
    }
}
