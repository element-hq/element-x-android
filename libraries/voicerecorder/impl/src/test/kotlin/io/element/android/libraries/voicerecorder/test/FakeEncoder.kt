/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.test

import io.element.android.libraries.voicerecorder.impl.audio.Encoder
import java.io.File

class FakeEncoder(
    private val fakeFileSystem: FakeFileSystem
) : Encoder {
    private var curFile: File? = null
    override fun init(file: File) {
        curFile = file
    }

    override fun encode(buffer: ShortArray, readSize: Int) {
        val file = curFile
            ?: error("Encoder not initialized")

        fakeFileSystem.appendToFile(file, buffer, readSize)
    }

    override fun release() {
        curFile = null
    }
}
