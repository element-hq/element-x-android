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

/**
 * Returns this file name with [suffix] inserted before its extension, i.e. `"image.png"` with the
 * suffix `"_1"` gives `"image_1.png"`. A name without extension simply gets the suffix appended.
 */
fun String.withFileNameSuffix(suffix: String): String {
    val extension = substringAfterLast('.', "")
    return if (extension.isEmpty()) {
        "$this$suffix"
    } else {
        "${substringBeforeLast('.')}$suffix.$extension"
    }
}

/**
 * Runs [saveAs] with [fileName], and runs it once more with a suffixed name if the platform failed to
 * build a unique name for it.
 *
 * `MediaStore` de-duplicates the display name of a new file itself, but gives up after 32 conflicts
 * and throws `IllegalStateException("Failed to build unique file")`, which makes saving a file with a
 * common name such as `image.png` fail for good.
 * See https://github.com/element-hq/element-x-android/issues/6371
 */
fun <T> saveWithUniqueFileName(
    fileName: String,
    uniqueSuffix: () -> String = { "_${System.currentTimeMillis()}" },
    saveAs: (String) -> T,
): T {
    return try {
        saveAs(fileName)
    } catch (nameConflict: IllegalStateException) {
        Timber.w(nameConflict, "Unable to build a unique file name, retrying with a suffixed one")
        saveAs(fileName.withFileNameSuffix(uniqueSuffix()))
    }
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
