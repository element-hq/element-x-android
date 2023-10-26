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
         * @param body the body of the voice message.
         */
        fun create(
            mediaSource: MediaSource,
            mimeType: String?,
            body: String?,
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

class DefaultVoiceMessageMediaRepo @AssistedInject constructor(
    @CacheDirectory private val cacheDir: File,
    private val matrixMediaLoader: MatrixMediaLoader,
    @Assisted private val mediaSource: MediaSource,
    @Assisted("mimeType") private val mimeType: String?,
    @Assisted("body") private val body: String?,
) : VoiceMessageMediaRepo {

    @ContributesBinding(RoomScope::class)
    @AssistedFactory
    fun interface Factory : VoiceMessageMediaRepo.Factory {
        override fun create(
            mediaSource: MediaSource,
            @Assisted("mimeType") mimeType: String?,
            @Assisted("body") body: String?,
        ): DefaultVoiceMessageMediaRepo
    }

    override suspend fun getMediaFile(): Result<File> = if (!isInCache()) {
        matrixMediaLoader.downloadMediaFile(
            source = mediaSource,
            mimeType = mimeType,
            body = body,
        ).mapCatching {
            val dest = cachedFilePath.apply { parentFile?.mkdirs() }
            // TODO By not closing the MediaFile we're leaking the rust file handle here.
            // Not that big of a deal but better to avoid it someday.
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
