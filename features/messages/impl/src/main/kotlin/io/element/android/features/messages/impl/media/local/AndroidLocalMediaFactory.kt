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

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.file.getFileName
import io.element.android.libraries.androidutils.file.getFileSize
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.api.media.toFile
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidLocalMediaFactory @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalMediaFactory {

    override fun createFromMediaFile(mediaFile: MediaFile, mimeType: String?): LocalMedia {
        val uri = mediaFile.toFile().toUri()
        return createFromUri(uri, mimeType)
    }

    override fun createFromUri(uri: Uri, mimeType: String?): LocalMedia {
        val resolvedMimeType = mimeType ?: context.contentResolver.getType(uri) ?: MimeTypes.OctetStream
        val fileName = context.getFileName(uri)
        val fileSize = context.getFileSize(uri)
        return LocalMedia(
            uri = uri,
            mimeType = resolvedMimeType,
            name = fileName,
            size = fileSize
        )
    }
}
