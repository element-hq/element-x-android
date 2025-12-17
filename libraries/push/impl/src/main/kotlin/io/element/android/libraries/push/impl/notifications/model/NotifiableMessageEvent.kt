/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.libraries.push.impl.notifications.model

import android.net.Uri
import androidx.core.net.toUri
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
    private val imageUriString: String?,
    val imageMimeType: String?,
    val threadId: ThreadId?,
    val roomName: String?,
    val roomIsDm: Boolean = false,
    val roomAvatarPath: String? = null,
    val senderAvatarPath: String? = null,
    val soundName: String? = null,
    // This is used for >N notification, as the result of a smart reply
    val outGoingMessage: Boolean = false,
    val outGoingMessageFailed: Boolean = false,
    override val isRedacted: Boolean = false,
    override val isUpdated: Boolean = false,
    val type: String = EventType.MESSAGE,
    val hasMentionOrReply: Boolean = false,
) : NotifiableEvent {
    override val description: String = body ?: ""

    // Example of value:
    // content://io.element.android.x.debug.notifications.fileprovider/downloads/temp/notif/matrix.org/XGItzSDOnSyXjYtOPfiKexDJ
    val imageUri: Uri?
        get() = imageUriString?.toUri()
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
