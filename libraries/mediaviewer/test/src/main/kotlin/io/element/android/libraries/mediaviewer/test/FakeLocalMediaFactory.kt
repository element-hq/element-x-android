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

package io.element.android.libraries.mediaviewer.test

import android.net.Uri
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractorWithoutValidation

class FakeLocalMediaFactory(
    private val localMediaUri: Uri,
    private val fileExtensionExtractor: FileExtensionExtractor = FileExtensionExtractorWithoutValidation()
) : LocalMediaFactory {

    var fallbackMimeType: String = MimeTypes.OctetStream
    var fallbackName: String = "File name"
    var fallbackFileSize = "0B"

    override fun createFromMediaFile(mediaFile: MediaFile, mediaInfo: MediaInfo): LocalMedia {
        return aLocalMedia(uri = localMediaUri, mediaInfo = mediaInfo)
    }

    override fun createFromUri(uri: Uri, mimeType: String?, name: String?, formattedFileSize: String?): LocalMedia {
        val safeName = name ?: fallbackName
        val mediaInfo = MediaInfo(
            name = safeName,
            mimeType = mimeType ?: fallbackMimeType,
            formattedFileSize = formattedFileSize ?: fallbackFileSize,
            fileExtension = fileExtensionExtractor.extractFromName(safeName)
        )
        return aLocalMedia(uri, mediaInfo)
    }
}
