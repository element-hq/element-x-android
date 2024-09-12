/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
