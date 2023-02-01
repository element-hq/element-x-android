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

package io.element.android.libraries.androidutils.file

import timber.log.Timber
import java.io.File
import java.util.zip.GZIPOutputStream

/**
 * GZip a file.
 *
 * @param file the input file
 * @return the gzipped file
 */
fun compressFile(file: File): File? {
    Timber.v("## compressFile() : compress ${file.name}")

    val dstFile = file.resolveSibling(file.name + ".gz")

    if (dstFile.exists()) {
        dstFile.safeDelete()
    }

    return try {
        GZIPOutputStream(dstFile.outputStream()).use { gos ->
            file.inputStream().use {
                it.copyTo(gos, 2048)
            }
        }

        Timber.v("## compressFile() : ${file.length()} compressed to ${dstFile.length()} bytes")
        dstFile
    } catch (e: Exception) {
        Timber.e(e, "## compressFile() failed")
        null
    } catch (oom: OutOfMemoryError) {
        Timber.e(oom, "## compressFile() failed")
        null
    }
}
