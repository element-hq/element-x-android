/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
