/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
