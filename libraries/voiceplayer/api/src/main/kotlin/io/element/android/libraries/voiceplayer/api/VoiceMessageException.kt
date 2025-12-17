/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.api

sealed class VoiceMessageException : Exception() {
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
