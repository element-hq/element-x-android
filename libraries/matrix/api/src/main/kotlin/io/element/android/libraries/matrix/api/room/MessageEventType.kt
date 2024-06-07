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

package io.element.android.libraries.matrix.api.room

enum class MessageEventType {
    CALL_ANSWER,
    CALL_INVITE,
    CALL_HANGUP,
    CALL_CANDIDATES,
    CALL_NOTIFY,
    KEY_VERIFICATION_READY,
    KEY_VERIFICATION_START,
    KEY_VERIFICATION_CANCEL,
    KEY_VERIFICATION_ACCEPT,
    KEY_VERIFICATION_KEY,
    KEY_VERIFICATION_MAC,
    KEY_VERIFICATION_DONE,
    REACTION,
    ROOM_ENCRYPTED,
    ROOM_MESSAGE,
    ROOM_REDACTION,
    STICKER,
    POLL_END,
    POLL_RESPONSE,
    POLL_START,
    UNSTABLE_POLL_END,
    UNSTABLE_POLL_RESPONSE,
    UNSTABLE_POLL_START,
}
