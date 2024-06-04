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
