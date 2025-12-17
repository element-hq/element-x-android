/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voicerecorder.test

import java.io.File

class FakeFileSystem {
    // Map of file to file content
    val files = mutableMapOf<File, String>()

    fun createFile(file: File) {
        if (files.containsKey(file)) {
            return
        }

        files[file] = ""
    }

    fun appendToFile(file: File, buffer: ShortArray, readSize: Int) {
        val content = files[file]
            ?: error("File ${file.path} does not exist")

        files[file] = content + buffer.sliceArray(0 until readSize).contentToString()
    }

    fun deleteFile(file: File) {
        files.remove(file)
    }
}
