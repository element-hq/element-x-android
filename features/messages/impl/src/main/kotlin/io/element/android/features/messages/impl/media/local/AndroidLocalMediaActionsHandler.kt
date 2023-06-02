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

package io.element.android.features.messages.impl.media.local

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidLocalMediaActionsHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineDispatchers: CoroutineDispatchers,
) : LocalMediaActionsHandler {

    override suspend fun saveOnDisk(localMedia: LocalMedia): Result<Unit> = withContext(coroutineDispatchers.io) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveOnDiskUsingMediaStore(localMedia)
            } else {
                saveOnDiskUsingExternalStorageApi(localMedia)
            }
        }
    }

    override suspend fun share(localMedia: LocalMedia): Result<Unit> {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveOnDiskUsingMediaStore(localMedia: LocalMedia) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, localMedia.name)
            put(MediaStore.MediaColumns.MIME_TYPE, localMedia.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val outputUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (outputUri != null) {
            localMedia.openStream()?.use { input ->
                resolver.openOutputStream(outputUri).use { output ->
                    input.copyTo(output!!, DEFAULT_BUFFER_SIZE)
                }
            }
        }
    }

    private fun saveOnDiskUsingExternalStorageApi(localMedia: LocalMedia) {
        val target = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            localMedia.name ?: ""
        )
        localMedia.openStream()?.use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun LocalMedia.openStream(): InputStream? {
        return context.contentResolver.openInputStream(uri)
    }
}
