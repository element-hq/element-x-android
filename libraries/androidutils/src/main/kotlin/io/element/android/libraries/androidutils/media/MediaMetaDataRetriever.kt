/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.androidutils.media

import android.media.MediaMetadataRetriever

/** [MediaMetadataRetriever] only implements `AutoClosable` since API 29, so we need to execute this to have the same in older APIs. */
inline fun <T> MediaMetadataRetriever.runAndRelease(block: MediaMetadataRetriever.() -> T): T {
    return try {
        block()
    } finally {
        release()
    }
}
