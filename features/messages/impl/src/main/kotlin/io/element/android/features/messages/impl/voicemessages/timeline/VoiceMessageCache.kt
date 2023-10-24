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
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.CacheDirectory
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
        fun create(mxcUri: String): VoiceMessageCache
    }

    /**
     * The file path of the voice message in the cache directory.
     * NB: This doesn't necessarily mean that the file exists.
     *
     * @return the file path of the voice message in the cache directory.
     */
    val cachePath: String

    /**
     * Checks if the voice message is in the cache directory.
     *
     * @return true if the voice message is in the cache directory.
     */
    fun isInCache(): Boolean

    /**
     * Moves the file to the voice cache directory.
     *
     * @return true if the file was successfully moved.
     */
    fun moveToCache(file: File): Boolean
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
    @Assisted private val mxcUri: String,
) : VoiceMessageCache {

    @ContributesBinding(AppScope::class)
    @AssistedFactory
    fun interface Factory : VoiceMessageCache.Factory {
        override fun create(mxcUri: String): VoiceMessageCacheImpl
    }

    override val cachePath: String = "${cacheDir.path}/$CACHE_VOICE_SUBDIR/${mxcUri2FilePath(mxcUri)}"

    override fun isInCache(): Boolean = File(cachePath).exists()

    override fun moveToCache(file: File): Boolean {
        val dest = File(cachePath).apply { parentFile?.mkdirs() }
        return file.renameTo(dest)
    }
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
