/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.media

import io.element.android.libraries.matrix.api.media.MediaPreviewValue.Off
import io.element.android.libraries.matrix.api.media.MediaPreviewValue.On
import io.element.android.libraries.matrix.api.media.MediaPreviewValue.Private
import io.element.android.libraries.matrix.api.room.join.JoinRule

/**
 * Represents the values for media preview settings.
 * - [On] means that media preview are enabled
 * - [Off] means that media preview are disabled
 * - [Private] means that media preview are enabled only for private chats.
 */
enum class MediaPreviewValue {
    On,
    Off,
    Private
}

fun MediaPreviewValue.isPreviewEnabled(joinRule: JoinRule?): Boolean {
    return when (this) {
        On -> true
        Off -> false
        Private -> when (joinRule) {
            is JoinRule.Knock,
            is JoinRule.Invite,
            is JoinRule.Restricted,
            is JoinRule.KnockRestricted -> true
            else -> false
        }
    }
}
