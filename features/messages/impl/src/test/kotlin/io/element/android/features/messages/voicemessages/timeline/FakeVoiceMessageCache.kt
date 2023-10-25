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

package io.element.android.features.messages.voicemessages.timeline

import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageCache
import java.io.File

/**
 * A fake implementation of [VoiceMessageCache] for testing purposes.
 */
class FakeVoiceMessageCache : VoiceMessageCache {

    private var _cachePath: String = ""
    private var _isInCache: Boolean = false
    private var _moveToCache: Boolean = false

    override val cachedFilePath: String
        get() = _cachePath

    override fun isInCache(): Boolean = _isInCache

    override fun downloadToCache(file: File): Boolean = _moveToCache

    fun givenCachePath(cachePath: String) {
        _cachePath = cachePath
    }

    fun givenIsInCache(isInCache: Boolean) {
        _isInCache = isInCache
    }

    fun givenMoveToCache(moveToCache: Boolean) {
        _moveToCache = moveToCache
    }
}
