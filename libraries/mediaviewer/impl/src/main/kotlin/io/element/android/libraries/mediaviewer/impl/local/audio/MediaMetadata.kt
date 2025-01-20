/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.audio

import androidx.media3.common.MediaMetadata

fun MediaMetadata?.hasArtwork(): Boolean {
    return this?.artworkData != null || this?.artworkUri != null
}

fun MediaMetadata?.buildInfo(): String {
    this ?: return ""
    return buildString {
        if (artist != null) {
            append(artist)
        }
        if (title != null) {
            if (isNotEmpty()) {
                append(" - ")
            }
            append(title)
        }
        if (recordingYear != null) {
            if (isNotEmpty()) {
                append(" - ")
            }
            append(recordingYear)
        }
    }
}
