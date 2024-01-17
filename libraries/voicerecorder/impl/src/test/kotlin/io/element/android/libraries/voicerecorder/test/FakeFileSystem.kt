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
