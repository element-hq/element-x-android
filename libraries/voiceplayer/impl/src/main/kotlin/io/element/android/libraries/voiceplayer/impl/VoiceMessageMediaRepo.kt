/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.mxc.MxcTools
import java.io.File

/**
 * Fetches the media file for a voice message.
 *
 * Media is downloaded from the rust sdk and stored in the application's cache directory.
 * Media files are indexed by their Matrix Content (mxc://) URI and considered immutable.
 * Whenever a given mxc is found in the cache, it is returned immediately.
 */
interface VoiceMessageMediaRepo {
    /**
     * Factory for [VoiceMessageMediaRepo].
     */
    fun interface Factory {
        /**
         * Creates a [VoiceMessageMediaRepo].
         *
         * @param mediaSource the media source of the voice message.
         * @param mimeType the mime type of the voice message.
         * @param filename the filename of the voice message.
         */
        fun create(
            mediaSource: MediaSource,
            mimeType: String?,
            filename: String?,
        ): VoiceMessageMediaRepo
    }

    /**
     * Returns the voice message media file.
     *
     * In case of a cache hit the file is returned immediately.
     * In case of a cache miss the file is downloaded and then returned.
     *
     * @return A [Result] holding either the media [File] from the cache directory or an [Exception].
     */
    suspend fun getMediaFile(): Result<File>
}

@AssistedInject
class DefaultVoiceMessageMediaRepo(
    @CacheDirectory private val cacheDir: File,
    mxcTools: MxcTools,
    private val matrixMediaLoader: MatrixMediaLoader,
    @Assisted private val mediaSource: MediaSource,
    @Assisted("mimeType") private val mimeType: String?,
    @Assisted("filename") private val filename: String?,
) : VoiceMessageMediaRepo {
    @ContributesBinding(RoomScope::class)
    @AssistedFactory
    fun interface Factory : VoiceMessageMediaRepo.Factory {
        override fun create(
            mediaSource: MediaSource,
            @Assisted("mimeType") mimeType: String?,
            @Assisted("filename") filename: String?,
        ): DefaultVoiceMessageMediaRepo
    }

    override suspend fun getMediaFile(): Result<File> = when {
        cachedFile == null -> Result.failure(IllegalStateException("Invalid mxcUri."))
        cachedFile.exists() -> Result.success(cachedFile)
        else -> matrixMediaLoader.downloadMediaFile(
            source = mediaSource,
            mimeType = mimeType,
            filename = filename,
        ).mapCatchingExceptions {
            it.use { mediaFile ->
                val dest = cachedFile.apply { parentFile?.mkdirs() }
                if (mediaFile.persist(dest.path)) {
                    dest
                } else {
                    error("Failed to move file to cache.")
                }
            }
        }
    }

    private val cachedFile: File? = mxcTools.mxcUri2FilePath(mediaSource.url)?.let {
        File("${cacheDir.path}/$CACHE_VOICE_SUBDIR/$it")
    }
}

/**
 * Subdirectory of the application's cache directory where voice messages are stored.
 */
private const val CACHE_VOICE_SUBDIR = "temp/voice"
