/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.video

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext

interface VideoMetadataExtractor : AutoCloseable {
    fun getSize(): Result<Size>
    fun getDuration(): Result<Long>
    interface Factory {
        fun create(uri: Uri): VideoMetadataExtractor
    }
}

@ContributesBinding(AppScope::class)
class DefaultVideoMetadataExtractor @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val uri: Uri,
) : VideoMetadataExtractor {
    @ContributesBinding(AppScope::class)
    @AssistedFactory
    interface Factory : VideoMetadataExtractor.Factory {
        override fun create(uri: Uri): DefaultVideoMetadataExtractor
    }

    // Don't use `by lazy` so we can catch any exceptions thrown during initialization
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

    override fun getDuration(): Result<Long> = runCatchingExceptions {
        mediaMetadataRetriever.value.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            ?.takeIf { it > 0L }
            ?: error("Could not retrieve video duration from metadata")
    }

    override fun close() {
        if (mediaMetadataRetriever.isInitialized()) {
            mediaMetadataRetriever.value.release()
        }
    }
}
