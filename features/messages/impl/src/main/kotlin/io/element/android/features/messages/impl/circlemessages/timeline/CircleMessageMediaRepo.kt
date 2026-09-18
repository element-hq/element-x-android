/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.circlemessages.timeline

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

interface CircleMessageMediaRepo {
    fun interface Factory {
        fun create(
            mediaSource: MediaSource,
            mimeType: String?,
            filename: String?,
        ): CircleMessageMediaRepo
    }

    suspend fun getMediaFile(): Result<File>
}

@AssistedInject
class DefaultCircleMessageMediaRepo(
    @CacheDirectory private val cacheDir: File,
    mxcTools: MxcTools,
    private val matrixMediaLoader: MatrixMediaLoader,
    @Assisted private val mediaSource: MediaSource,
    @Assisted private val mimeType: String?,
    @Assisted private val filename: String?,
) : CircleMessageMediaRepo {
    @ContributesBinding(RoomScope::class)
    @AssistedFactory
    fun interface Factory : CircleMessageMediaRepo.Factory {
        override fun create(
            mediaSource: MediaSource,
            @Assisted mimeType: String?,
            @Assisted filename: String?,
        ): DefaultCircleMessageMediaRepo
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

    private val cachedFile: File? = mxcTools.mxcUri2FilePath(mediaSource.safeUrl)?.let {
        File("${cacheDir.path}/$CACHE_CIRCLE_SUBDIR/$it")
    }
}

private const val CACHE_CIRCLE_SUBDIR = "temp/circle"
