/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.test

import android.net.Uri
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractorWithoutValidation
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia

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
            filename = safeName,
            caption = null,
            mimeType = mimeType ?: fallbackMimeType,
            formattedFileSize = formattedFileSize ?: fallbackFileSize,
            fileExtension = fileExtensionExtractor.extractFromName(safeName)
        )
        return aLocalMedia(uri, mediaInfo)
    }
}
