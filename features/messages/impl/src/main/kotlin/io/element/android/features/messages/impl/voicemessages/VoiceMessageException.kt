/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages

internal sealed class VoiceMessageException : Exception() {
    data class FileException(
        override val message: String?,
        override val cause: Throwable? = null
    ) : VoiceMessageException()
    data class PermissionMissing(
        override val message: String?,
        override val cause: Throwable?
    ) : VoiceMessageException()
    data class PlayMessageError(
        override val message: String?,
        override val cause: Throwable?
    ) : VoiceMessageException()
}
