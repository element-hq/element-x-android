/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.file.getFileName
import io.element.android.libraries.androidutils.file.getFileSize
import io.element.android.libraries.androidutils.file.getMimeType
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.api.media.toFile
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class AndroidLocalMediaFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileSizeFormatter: FileSizeFormatter,
    private val fileExtensionExtractor: FileExtensionExtractor,
) : LocalMediaFactory {
    override fun createFromMediaFile(
        mediaFile: MediaFile,
        mediaInfo: MediaInfo,
    ): LocalMedia = createFromUri(
        uri = mediaFile.toFile().toUri(),
        mimeType = mediaInfo.mimeType,
        name = mediaInfo.filename,
        caption = mediaInfo.caption,
        formattedFileSize = mediaInfo.formattedFileSize,
    )

    override fun createFromUri(
        uri: Uri,
        mimeType: String?,
        name: String?,
        formattedFileSize: String?
    ): LocalMedia = createFromUri(
        uri = uri,
        mimeType = mimeType,
        name = name,
        caption = null,
        formattedFileSize = formattedFileSize,
    )

    private fun createFromUri(
        uri: Uri,
        mimeType: String?,
        name: String?,
        caption: String?,
        formattedFileSize: String?
    ): LocalMedia {
        val resolvedMimeType = mimeType ?: context.getMimeType(uri) ?: MimeTypes.OctetStream
        val fileName = name ?: context.getFileName(uri) ?: ""
        val fileSize = formattedFileSize ?: fileSizeFormatter.format(context.getFileSize(uri))
        val fileExtension = fileExtensionExtractor.extractFromName(fileName)
        return LocalMedia(
            uri = uri,
            info = MediaInfo(
                mimeType = resolvedMimeType,
                filename = fileName,
                caption = caption,
                formattedFileSize = fileSize,
                fileExtension = fileExtension
            )
        )
    }
}
