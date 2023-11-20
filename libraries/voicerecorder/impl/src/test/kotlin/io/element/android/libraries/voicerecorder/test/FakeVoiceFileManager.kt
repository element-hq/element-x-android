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

package io.element.android.libraries.voicerecorder.test

import io.element.android.libraries.voicerecorder.impl.file.VoiceFileConfig
import io.element.android.libraries.voicerecorder.impl.file.VoiceFileManager
import java.io.File

class FakeVoiceFileManager(
    private val fakeFileSystem: FakeFileSystem,
    private val config: VoiceFileConfig,
    private val fileId: String,
) : VoiceFileManager {
    override fun createFile(): File {
        val file = File("${config.cacheSubdir}/$fileId.${config.fileExt}")
        fakeFileSystem.createFile(file)
        return file
    }

    override fun deleteFile(file: File) {
        fakeFileSystem.deleteFile(file)
    }
}
