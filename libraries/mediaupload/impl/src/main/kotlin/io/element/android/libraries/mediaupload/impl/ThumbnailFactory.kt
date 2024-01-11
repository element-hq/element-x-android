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

package io.element.android.libraries.mediaupload.impl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import androidx.core.net.toUri
import com.vanniktech.blurhash.BlurHash
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.media.runAndRelease
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
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

class ThumbnailFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sdkIntProvider: BuildVersionSdkIntProvider
) {
    @SuppressLint("NewApi")
    suspend fun createImageThumbnail(file: File): ThumbnailResult? {
        return createThumbnail { cancellationSignal ->
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
        return createThumbnail {
            MediaMetadataRetriever().runAndRelease {
                setDataSource(context, file.toUri())
                getFrameAtTime(VIDEO_THUMB_FRAME)
            }
        }
    }

    private suspend fun createThumbnail(bitmapFactory: (CancellationSignal) -> Bitmap?): ThumbnailResult? = suspendCancellableCoroutine { continuation ->
        val cancellationSignal = CancellationSignal()
        continuation.invokeOnCancellation {
            cancellationSignal.cancel()
        }
        val bitmapThumbnail: Bitmap? = bitmapFactory(cancellationSignal)
        if (bitmapThumbnail == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        val thumbnailFile = context.createTmpFile(extension = "jpeg")
        thumbnailFile.outputStream().use { outputStream ->
            bitmapThumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }
        val blurhash = BlurHash.encode(bitmapThumbnail, 3, 3)
        val thumbnailResult = ThumbnailResult(
            file = thumbnailFile,
            info = ThumbnailInfo(
                height = bitmapThumbnail.height.toLong(),
                width = bitmapThumbnail.width.toLong(),
                mimetype = MimeTypes.Jpeg,
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
