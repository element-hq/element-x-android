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

package io.element.android.features.messages.impl.voicemessages.timeline

import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.di.CacheDirectory
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.toFile
import java.io.File

/**
 * Manages the local disk cache for a voice message.
 */
interface VoiceMessageCache {

    /**
     * Factory for [VoiceMessageCache].
     */
    fun interface Factory {
        /**
         * Creates a [VoiceMessageCache] for the given Matrix Content (mxc://) URI.
         *
         * @param mxcUri the Matrix Content (mxc://) URI of the voice message.
         */
        fun create(
            mediaSource: MediaSource,
            mimeType: String?,
            body: String?,
        ): VoiceMessageCache
    }

    /**
     * Returns the voice message file.
     *
     * In case of a cache hit the file is returned immediately.
     * In case of a cache miss the file is downloaded and then returned.
     *
     * @return the voice message file from the cache directory.
     */
    suspend fun getMediaFile(): Result<File>
}

/**
 * Default implementation of [VoiceMessageCache].
 *
 * NB: All methods will throw an [IllegalStateException] if the mxcUri is invalid.
 *
 * @param cacheDir the application's cache directory.
 * @param mxcUri the Matrix Content (mxc://) URI of the voice message.
 */
class VoiceMessageCacheImpl @AssistedInject constructor(
    @CacheDirectory private val cacheDir: File,
    private val matrixMediaLoader: MatrixMediaLoader,
    @Assisted private val mediaSource: MediaSource,
    @Assisted("mimeType") private val mimeType: String?,
    @Assisted("body") private val body: String?,
) : VoiceMessageCache {

    @ContributesBinding(RoomScope::class)
    @AssistedFactory
    fun interface Factory : VoiceMessageCache.Factory {
        override fun create(
            mediaSource: MediaSource,
            @Assisted("mimeType") mimeType: String?,
            @Assisted("body") body: String?,
        ): VoiceMessageCacheImpl
    }

    override suspend fun getMediaFile(): Result<File> = if (!isInCache()) {
        matrixMediaLoader.downloadMediaFile(
            source = mediaSource,
            mimeType = mimeType,
            body = body,
        ).mapCatching {
            val dest = cachedFilePath.apply { parentFile?.mkdirs() }
            // TODO: By not closing the MediaFile we're leaking the rust file handle here.
            // Not that big of a deal but better to avoid it.
            if (it.toFile().renameTo(dest)) {
                dest
            } else {
                error("Failed to move file to cache.")
            }
        }
    } else {
        Result.success(cachedFilePath)
    }

    private val cachedFilePath: File = File("${cacheDir.path}/$CACHE_VOICE_SUBDIR/${mxcUri2FilePath(mediaSource.url)}")

    private fun isInCache(): Boolean = cachedFilePath.exists()
}

/**
 * Subdirectory of the application's cache directory where voice messages are stored.
 */
private const val CACHE_VOICE_SUBDIR = "temp/voice"

/**
 * Regex to match a Matrix Content (mxc://) URI.
 *
 * See: https://spec.matrix.org/v1.8/client-server-api/#matrix-content-mxc-uris
 */
private val mxcRegex = Regex("""^mxc:\/\/([^\/]+)\/([^\/]+)$""")

/**
 * Sanitizes an mxcUri to be used as a relative file path.
 *
 * @param mxcUri the Matrix Content (mxc://) URI of the voice message.
 * @return the relative file path as "<server-name>/<media-id>".
 * @throws IllegalStateException if the mxcUri is invalid.
 */
private fun mxcUri2FilePath(mxcUri: String): String = checkNotNull(mxcRegex.matchEntire(mxcUri)) {
    "mxcUri2FilePath: Invalid mxcUri: $mxcUri"
}.let { match ->
    buildString {
        append(match.groupValues[1])
        append("/")
        append(match.groupValues[2])
    }
}
