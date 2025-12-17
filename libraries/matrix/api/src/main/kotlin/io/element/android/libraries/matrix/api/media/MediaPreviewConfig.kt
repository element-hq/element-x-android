/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

/**
 * Configuration for media preview ie. invite avatars and timeline media.
 */
data class MediaPreviewConfig(
    val mediaPreviewValue: MediaPreviewValue,
    val hideInviteAvatar: Boolean,
) {
    companion object {
        /**
         * The default config if unknown (no local nor server config).
         */
        val DEFAULT = MediaPreviewConfig(
            mediaPreviewValue = MediaPreviewValue.DEFAULT,
            hideInviteAvatar = false
        )
    }
}
