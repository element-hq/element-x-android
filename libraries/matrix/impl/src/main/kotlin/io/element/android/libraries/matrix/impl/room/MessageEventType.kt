/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.MessageEventType
import org.matrix.rustcomponents.sdk.MessageLikeEventType

fun MessageEventType.map(): MessageLikeEventType = when (this) {
    MessageEventType.CALL_ANSWER -> MessageLikeEventType.CALL_ANSWER
    MessageEventType.CALL_INVITE -> MessageLikeEventType.CALL_INVITE
    MessageEventType.CALL_HANGUP -> MessageLikeEventType.CALL_HANGUP
    MessageEventType.CALL_CANDIDATES -> MessageLikeEventType.CALL_CANDIDATES
    MessageEventType.CALL_NOTIFY -> MessageLikeEventType.CALL_NOTIFY
    MessageEventType.KEY_VERIFICATION_READY -> MessageLikeEventType.KEY_VERIFICATION_READY
    MessageEventType.KEY_VERIFICATION_START -> MessageLikeEventType.KEY_VERIFICATION_START
    MessageEventType.KEY_VERIFICATION_CANCEL -> MessageLikeEventType.KEY_VERIFICATION_CANCEL
    MessageEventType.KEY_VERIFICATION_ACCEPT -> MessageLikeEventType.KEY_VERIFICATION_ACCEPT
    MessageEventType.KEY_VERIFICATION_KEY -> MessageLikeEventType.KEY_VERIFICATION_KEY
    MessageEventType.KEY_VERIFICATION_MAC -> MessageLikeEventType.KEY_VERIFICATION_MAC
    MessageEventType.KEY_VERIFICATION_DONE -> MessageLikeEventType.KEY_VERIFICATION_DONE
    MessageEventType.REACTION -> MessageLikeEventType.REACTION
    MessageEventType.ROOM_ENCRYPTED -> MessageLikeEventType.ROOM_ENCRYPTED
    MessageEventType.ROOM_MESSAGE -> MessageLikeEventType.ROOM_MESSAGE
    MessageEventType.ROOM_REDACTION -> MessageLikeEventType.ROOM_REDACTION
    MessageEventType.STICKER -> MessageLikeEventType.STICKER
    MessageEventType.POLL_END -> MessageLikeEventType.POLL_END
    MessageEventType.POLL_RESPONSE -> MessageLikeEventType.POLL_RESPONSE
    MessageEventType.POLL_START -> MessageLikeEventType.POLL_START
    MessageEventType.UNSTABLE_POLL_END -> MessageLikeEventType.UNSTABLE_POLL_END
    MessageEventType.UNSTABLE_POLL_RESPONSE -> MessageLikeEventType.UNSTABLE_POLL_RESPONSE
    MessageEventType.UNSTABLE_POLL_START -> MessageLikeEventType.UNSTABLE_POLL_START
}

fun MessageLikeEventType.map(): MessageEventType = when (this) {
    MessageLikeEventType.CALL_ANSWER -> MessageEventType.CALL_ANSWER
    MessageLikeEventType.CALL_INVITE -> MessageEventType.CALL_INVITE
    MessageLikeEventType.CALL_HANGUP -> MessageEventType.CALL_HANGUP
    MessageLikeEventType.CALL_CANDIDATES -> MessageEventType.CALL_CANDIDATES
    MessageLikeEventType.CALL_NOTIFY -> MessageEventType.CALL_NOTIFY
    MessageLikeEventType.KEY_VERIFICATION_READY -> MessageEventType.KEY_VERIFICATION_READY
    MessageLikeEventType.KEY_VERIFICATION_START -> MessageEventType.KEY_VERIFICATION_START
    MessageLikeEventType.KEY_VERIFICATION_CANCEL -> MessageEventType.KEY_VERIFICATION_CANCEL
    MessageLikeEventType.KEY_VERIFICATION_ACCEPT -> MessageEventType.KEY_VERIFICATION_ACCEPT
    MessageLikeEventType.KEY_VERIFICATION_KEY -> MessageEventType.KEY_VERIFICATION_KEY
    MessageLikeEventType.KEY_VERIFICATION_MAC -> MessageEventType.KEY_VERIFICATION_MAC
    MessageLikeEventType.KEY_VERIFICATION_DONE -> MessageEventType.KEY_VERIFICATION_DONE
    MessageLikeEventType.REACTION -> MessageEventType.REACTION
    MessageLikeEventType.ROOM_ENCRYPTED -> MessageEventType.ROOM_ENCRYPTED
    MessageLikeEventType.ROOM_MESSAGE -> MessageEventType.ROOM_MESSAGE
    MessageLikeEventType.ROOM_REDACTION -> MessageEventType.ROOM_REDACTION
    MessageLikeEventType.STICKER -> MessageEventType.STICKER
    MessageLikeEventType.POLL_END -> MessageEventType.POLL_END
    MessageLikeEventType.POLL_RESPONSE -> MessageEventType.POLL_RESPONSE
    MessageLikeEventType.POLL_START -> MessageEventType.POLL_START
    MessageLikeEventType.UNSTABLE_POLL_END -> MessageEventType.UNSTABLE_POLL_END
    MessageLikeEventType.UNSTABLE_POLL_RESPONSE -> MessageEventType.UNSTABLE_POLL_RESPONSE
    MessageLikeEventType.UNSTABLE_POLL_START -> MessageEventType.UNSTABLE_POLL_START
}
