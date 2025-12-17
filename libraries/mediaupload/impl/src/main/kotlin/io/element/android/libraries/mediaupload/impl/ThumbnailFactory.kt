/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
import android.media.ThumbnailUtils
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import androidx.core.net.toUri
import com.vanniktech.blurhash.BlurHash
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.bitmap.resizeToMax
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.media.runAndRelease
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume

/**
 * Max width of thumbnail images.
 * See [the Matrix spec](https://spec.matrix.org/latest/client-server-api/?ref=blog.gitter.im#thumbnails).
 */
private const val THUMB_MAX_WIDTH = 800

/**
 * Max height of thumbnail images.
 * See [the Matrix spec](https://spec.matrix.org/latest/client-server-api/?ref=blog.gitter.im#thumbnails).
 */
private const val THUMB_MAX_HEIGHT = 600

/**
 * Frame of the video to be used for generating a thumbnail.
 */
private const val VIDEO_THUMB_FRAME = 0L

@Inject
class ThumbnailFactory(
    @ApplicationContext private val context: Context,
    private val sdkIntProvider: BuildVersionSdkIntProvider
) {
    @SuppressLint("NewApi")
    suspend fun createImageThumbnail(
        file: File,
        mimeType: String,
    ): ThumbnailResult? {
        return createThumbnail(mimeType = mimeType) { cancellationSignal ->
            try {
                // This API works correctly with GIF
                if (sdkIntProvider.isAtLeast(Build.VERSION_CODES.Q)) {
                    try {
                        ThumbnailUtils.createImageThumbnail(
                            file,
                            Size(THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT),
                            cancellationSignal
                        )
                    } catch (ioException: IOException) {
                        Timber.w(ioException, "Failed to create thumbnail for $file")
                        null
                    }
                } else {
                    @Suppress("DEPRECATION")
                    ThumbnailUtils.createImageThumbnail(
                        file.path,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                    )
                }
            } catch (throwable: Throwable) {
                Timber.w(throwable, "Failed to create thumbnail for $file")
                null
            }
        }
    }

    suspend fun createVideoThumbnail(file: File): ThumbnailResult? {
        return createThumbnail(mimeType = MimeTypes.Jpeg) {
            MediaMetadataRetriever().runAndRelease {
                setDataSource(context, file.toUri())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    getScaledFrameAtTime(VIDEO_THUMB_FRAME, OPTION_CLOSEST_SYNC, THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT)
                } else {
                    getFrameAtTime(VIDEO_THUMB_FRAME)?.resizeToMax(THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT)
                }
            }
        }
    }

    private suspend fun createThumbnail(
        mimeType: String,
        bitmapFactory: (CancellationSignal) -> Bitmap?,
    ): ThumbnailResult? = suspendCancellableCoroutine { continuation ->
        val cancellationSignal = CancellationSignal()
        continuation.invokeOnCancellation {
            cancellationSignal.cancel()
        }
        val bitmapThumbnail: Bitmap? = bitmapFactory(cancellationSignal)
        if (bitmapThumbnail == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        val format = mimeTypeToCompressFormat(mimeType)
        val extension = mimeTypeToCompressFileExtension(mimeType)
        val thumbnailFile = context.createTmpFile(extension = extension)
        thumbnailFile.outputStream().use { outputStream ->
            bitmapThumbnail.compress(format, 78, outputStream)
        }
        val blurhash = BlurHash.encode(bitmapThumbnail, 3, 3)
        val thumbnailResult = ThumbnailResult(
            file = thumbnailFile,
            info = ThumbnailInfo(
                width = bitmapThumbnail.width.toLong(),
                height = bitmapThumbnail.height.toLong(),
                mimetype = mimeTypeToThumbnailMimeType(mimeType),
                size = thumbnailFile.length()
            ),
            blurhash = blurhash
        )
        bitmapThumbnail.recycle()
        continuation.resume(thumbnailResult)
    }
}

data class ThumbnailResult(
    val file: File,
    val info: ThumbnailInfo,
    val blurhash: String,
)
