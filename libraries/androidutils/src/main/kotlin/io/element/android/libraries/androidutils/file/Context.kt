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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile

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

private fun Context.getContentFileSize(uri: Uri): Long? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.SIZE).let(cursor::getLong)
    }
}.getOrNull()

private fun Context.getContentFileName(uri: Uri): String? = runCatching {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        cursor.moveToFirst()
        return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
    }
}.getOrNull()
