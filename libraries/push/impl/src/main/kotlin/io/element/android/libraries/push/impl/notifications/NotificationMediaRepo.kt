/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.mxc.MxcTools
import java.io.File

/**
 * Fetches the media file for a notification.
 *
 * Media is downloaded from the rust sdk and stored in the application's cache directory.
 * Media files are indexed by their Matrix Content (mxc://) URI and considered immutable.
 * Whenever a given mxc is found in the cache, it is returned immediately.
 */
interface NotificationMediaRepo {
    /**
     * Factory for [NotificationMediaRepo].
     */
    fun interface Factory {
        /**
         * Creates a [NotificationMediaRepo].
         *
         */
        fun create(
            client: MatrixClient
        ): NotificationMediaRepo
    }

    /**
     * Returns the file.
     *
     * In case of a cache hit the file is returned immediately.
     * In case of a cache miss the file is downloaded and then returned.
     *
     * @param mediaSource the media source of the media.
     * @param mimeType the mime type of the media.
     * @param filename optional String which will be used to name the file.
     * @return A [Result] holding either the media [File] from the cache directory or an [Exception].
     */
    suspend fun getMediaFile(
        mediaSource: MediaSource,
        mimeType: String?,
        filename: String?,
    ): Result<File>
}

class DefaultNotificationMediaRepo @AssistedInject constructor(
    @CacheDirectory private val cacheDir: File,
    private val mxcTools: MxcTools,
    @Assisted private val client: MatrixClient,
) : NotificationMediaRepo {
    @ContributesBinding(AppScope::class)
    @AssistedFactory
    fun interface Factory : NotificationMediaRepo.Factory {
        override fun create(
            client: MatrixClient,
        ): DefaultNotificationMediaRepo
    }

    private val matrixMediaLoader = client.mediaLoader

    override suspend fun getMediaFile(
        mediaSource: MediaSource,
        mimeType: String?,
        filename: String?,
    ): Result<File> {
        val cachedFile = mediaSource.cachedFile()
        return when {
            cachedFile == null -> Result.failure(IllegalStateException("Invalid mxcUri."))
            cachedFile.exists() -> Result.success(cachedFile)
            else -> matrixMediaLoader.downloadMediaFile(
                source = mediaSource,
                mimeType = mimeType,
                filename = filename,
            ).mapCatching {
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
    }

    private fun MediaSource.cachedFile(): File? = mxcTools.mxcUri2FilePath(url)?.let {
        File("${cacheDir.path}/$CACHE_NOTIFICATION_SUBDIR/$it")
    }
}

/**
 * Subdirectory of the application's cache directory where file are stored.
 */
private const val CACHE_NOTIFICATION_SUBDIR = "temp/notif"
