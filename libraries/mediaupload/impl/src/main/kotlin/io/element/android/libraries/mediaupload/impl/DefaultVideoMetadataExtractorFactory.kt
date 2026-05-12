/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.mediaupload.api.VideoMetadataExtractor
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DefaultVideoMetadataExtractorFactory(
    private val context: Context,
) : VideoMetadataExtractor.Factory {
    override fun create(uri: Uri): VideoMetadataExtractor {
        return DefaultVideoMetadataExtractor(context, uri)
    }
}

private class DefaultVideoMetadataExtractor(
    private val context: Context,
    private val uri: Uri,
) : VideoMetadataExtractor {
    private val mediaMetadataRetriever = lazy {
        MediaMetadataRetriever().apply {
            setDataSource(context, uri)
        }
    }

    override fun getSize(): Result<Size> = runCatchingExceptions {
        val width = mediaMetadataRetriever.value.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
        val height = mediaMetadataRetriever.value.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()

        @Suppress("ComplexCondition")
        if (width != null && width > 0 && height != null && height > 0) {
            Size(width, height)
        } else {
            error("Could not retrieve video size from metadata for $uri")
        }
    }

    override fun getDuration(): Result<Duration> = runCatchingExceptions {
        mediaMetadataRetriever.value.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            ?.takeIf { it > 0L }
            ?.milliseconds
            ?: error("Could not retrieve video duration from metadata")
    }

    override fun close() {
        if (mediaMetadataRetriever.isInitialized()) {
            mediaMetadataRetriever.value.release()
        }
    }
}
