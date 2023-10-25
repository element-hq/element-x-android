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

import com.google.common.truth.Truth
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageMediaRepositoryImpl
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class VoiceMessageMediaRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `moveToVoiceCache() should move the file to the voice cache dir`() {
        val rootPath = temporaryFolder.root.path
        val file = File("$rootPath/myFile.txt").apply { createNewFile() }
        val cacheDir = File("$rootPath/cacheDir").apply { if (!exists()) mkdirs() }
        val mxcUri = "mxc://matrix.org/1234567890abcdefg"
        val cache = VoiceMessageMediaRepositoryImpl(cacheDir, mxcUri)

        Truth.assertThat(cache.downloadToCache(file))
            .isTrue()
        Truth.assertThat(File("$rootPath/cacheDir/temp/voice/matrix.org/1234567890abcdefg").exists())
            .isTrue()
    }

    @Test
    fun `voiceCachePath() should point to cacheDir-temp-voice-mxcUri2fileName`() {
        val rootPath = temporaryFolder.root.path
        val cacheDir = File("$rootPath/cacheDir")
        val mxcUri = "mxc://matrix.org/1234567890abcdefg"
        val cache = VoiceMessageMediaRepositoryImpl(cacheDir, mxcUri)

        Truth.assertThat(cache.cachedFilePath)
            .isEqualTo("$rootPath/cacheDir/temp/voice/matrix.org/1234567890abcdefg")
    }

    @Test
    fun `isInVoiceCache() should return true if the file exists`() {
        val rootPath = temporaryFolder.root.path
        val cacheDir = File("$rootPath/cacheDir")
        val mxcUri = "mxc://matrix.org/1234567890abcdefg"
        val file = File("$rootPath/cacheDir/temp/voice/matrix.org/1234567890abcdefg").apply {
            parentFile?.mkdirs()
            createNewFile()
        }
        val cache = VoiceMessageMediaRepositoryImpl(cacheDir, mxcUri)

        Truth.assertThat(cache.isInCache())
            .isTrue()
    }

    @Test
    fun `isInVoiceCache() should return false if the file does not exist`() {
        val rootPath = temporaryFolder.root.path
        val cacheDir = File("$rootPath/cacheDir")
        val mxcUri = "mxc://matrix.org/1234567890abcdefg"
        val cache = VoiceMessageMediaRepositoryImpl(cacheDir, mxcUri)

        Truth.assertThat(cache.isInCache())
            .isFalse()
    }

    @Test(expected = IllegalStateException::class)
    fun `isInVoiceCache() throws IllegalStateException on bogus mxc uri`() {
        val cacheDir = File("")
        val mxcUri = "bogus"
        val cache = VoiceMessageMediaRepositoryImpl(cacheDir, mxcUri)

        cache.isInCache()
    }
}
