/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile
import io.element.android.libraries.core.extensions.runCatchingExceptions

fun Context.getMimeType(uri: Uri): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> contentResolver.getType(uri)
    else -> null
}

fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
    ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
    ContentResolver.SCHEME_FILE -> uri.toFile().name
    else -> null
}

fun Context.getFileSize(uri: Uri): Long {
    return when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileSize(uri)
        ContentResolver.SCHEME_FILE -> uri.toFile().length()
        else -> 0
    } ?: 0
}

private fun Context.getContentFileSize(uri: Uri): Long? = runCatchingExceptions {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.SIZE).let(cursor::getLong)
    }
}.getOrNull()

private fun Context.getContentFileName(uri: Uri): String? = runCatchingExceptions {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()
