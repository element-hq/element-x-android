/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.file.getFileName
import io.element.android.libraries.androidutils.file.getFileSize
import io.element.android.libraries.androidutils.file.getMimeType
import io.element.android.libraries.androidutils.filesize.FileSizeFormatter
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.api.media.toFile
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractor

@ContributesBinding(AppScope::class)
class AndroidLocalMediaFactory(
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
        senderId = mediaInfo.senderId,
        senderName = mediaInfo.senderName,
        senderAvatar = mediaInfo.senderAvatar,
        dateSent = mediaInfo.dateSent,
        dateSentFull = mediaInfo.dateSentFull,
        waveform = mediaInfo.waveform,
        duration = mediaInfo.duration,
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
        senderId = null,
        senderName = null,
        senderAvatar = null,
        dateSent = null,
        dateSentFull = null,
        waveform = null,
        duration = null,
    )

    private fun createFromUri(
        uri: Uri,
        mimeType: String?,
        name: String?,
        caption: String?,
        formattedFileSize: String?,
        senderId: UserId?,
        senderName: String?,
        senderAvatar: String?,
        dateSent: String?,
        dateSentFull: String?,
        waveform: List<Float>?,
        duration: String?,
    ): LocalMedia {
        val fileName = name ?: context.getFileName(uri) ?: ""
        val resolvedMimeType = resolveMimeType(
            uri = uri,
            mimeType = mimeType,
            fileName = fileName,
        )
        val fileSize = context.getFileSize(uri)
        val calculatedFormattedFileSize = formattedFileSize ?: fileSizeFormatter.format(fileSize)
        val fileExtension = fileExtensionExtractor.extractFromName(fileName)
        return LocalMedia(
            uri = uri,
            info = MediaInfo(
                mimeType = resolvedMimeType,
                filename = fileName,
                fileSize = fileSize,
                caption = caption,
                formattedFileSize = calculatedFormattedFileSize,
                fileExtension = fileExtension,
                senderId = senderId,
                senderName = senderName,
                senderAvatar = senderAvatar,
                dateSent = dateSent,
                dateSentFull = dateSentFull,
                waveform = waveform,
                duration = duration,
            )
        )
    }

    private fun resolveMimeType(
        uri: Uri,
        mimeType: String?,
        fileName: String,
    ): String {
        val explicitMimeType = mimeType.takeUnless { it.isNullOrBlank() || it == MimeTypes.OctetStream }
        if (explicitMimeType != null) return explicitMimeType

        val resolverMimeType = context.getMimeType(uri).takeUnless { it.isNullOrBlank() || it == MimeTypes.OctetStream }
        if (resolverMimeType != null) return resolverMimeType

        val decodedImageMimeType = decodeImageMimeType(uri)
        if (decodedImageMimeType != null) return decodedImageMimeType

        val extensionMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtensionExtractor.extractFromName(fileName)
        )
        if (!extensionMimeType.isNullOrBlank()) return extensionMimeType

        return MimeTypes.OctetStream
    }

    private fun decodeImageMimeType(uri: Uri): String? {
        return tryOrNull {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeStream(inputStream, null, options)
                options.outMimeType
            }
        }
    }
}
