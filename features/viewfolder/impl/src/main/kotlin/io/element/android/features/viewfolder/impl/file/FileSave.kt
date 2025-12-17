/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.file

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

interface FileSave {
    suspend fun save(
        path: String,
    )
}

@ContributesBinding(AppScope::class)
class DefaultFileSave(
    @ApplicationContext private val context: Context,
    private val dispatchers: CoroutineDispatchers,
) : FileSave {
    override suspend fun save(
        path: String,
    ) {
        withContext(dispatchers.io) {
            runCatchingExceptions {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveOnDiskUsingMediaStore(path)
                } else {
                    saveOnDiskUsingExternalStorageApi(path)
                }
            }.onSuccess {
                Timber.v("Save on disk succeed")
                withContext(dispatchers.main) {
                    context.toast("Save on disk succeed")
                }
            }.onFailure {
                Timber.e(it, "Save on disk failed")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveOnDiskUsingMediaStore(path: String) {
        val file = File(path)
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, MimeTypes.OctetStream)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val outputUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (outputUri != null) {
            file.inputStream().use { input ->
                resolver.openOutputStream(outputUri).use { output ->
                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                }
            }
        }
    }

    private fun saveOnDiskUsingExternalStorageApi(path: String) {
        val file = File(path)
        val target = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            file.name
        )
        file.inputStream().use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
    }
}
