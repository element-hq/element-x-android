/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @param body the body of the message.
     * @return A [Result] holding either the media [File] from the cache directory or an [Exception].
     */
    suspend fun getMediaFile(
        mediaSource: MediaSource,
        mimeType: String?,
        body: String?,
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
        body: String?,
    ): Result<File> {
        val cachedFile = mediaSource.cachedFile()
        return when {
            cachedFile == null -> Result.failure(IllegalStateException("Invalid mxcUri."))
            cachedFile.exists() -> Result.success(cachedFile)
            else -> matrixMediaLoader.downloadMediaFile(
                source = mediaSource,
                mimeType = mimeType,
                body = body,
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
