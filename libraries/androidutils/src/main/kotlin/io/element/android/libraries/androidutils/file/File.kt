/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.file

import android.content.Context
import androidx.annotation.WorkerThread
import io.element.android.libraries.core.data.tryOrNull
import timber.log.Timber
import java.io.File
import java.util.UUID

fun File.safeDelete() {
    if (exists().not()) return
    tryOrNull(
        onException = {
            Timber.e(it, "Error, unable to delete file $path")
        },
        operation = {
            if (delete().not()) {
                Timber.w("Warning, unable to delete file $path")
            }
        }
    )
}

fun File.safeRenameTo(dest: File) {
    tryOrNull(
        onException = {
            Timber.e(it, "Error, unable to rename file $path to ${dest.path}")
        },
        operation = {
            if (renameTo(dest).not()) {
                Timber.w("Warning, unable to rename file $path to ${dest.path}")
            }
        }
    )
}

fun Context.createTmpFile(baseDir: File = cacheDir, extension: String? = null): File {
    val suffix = extension?.let { ".$extension" }
    return File.createTempFile(UUID.randomUUID().toString(), suffix, baseDir).apply { mkdirs() }
}

/* ==========================================================================================
 * Size
 * ========================================================================================== */

@WorkerThread
fun File.getSizeOfFiles(): Long {
    return walkTopDown()
        .onEnter {
            Timber.v("Get size of ${it.absolutePath}")
            true
        }
        .sumOf { it.length() }
}
