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

package io.element.android.libraries.mediaupload

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.media.runAndRelease
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.media.ThumbnailInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaType
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.api.ThumbnailProcessingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@ContributesBinding(AppScope::class)
class MediaPreProcessorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageCompressor: ImageCompressor,
    private val videoCompressor: VideoCompressor,
) : MediaPreProcessor {
    companion object {
        /**
         * Used for calculating `inSampleSize` for bitmaps.
         *
         * *Note*: Ideally, this should result in images of up to (but not included) 1280x1280 being sent. However, images with very different width and height
         * values may surpass this limit. (i.e.: an image of `480x3000px` would have `inSampleSize=1` and be sent as is).
         */
        private const val IMAGE_SCALE_REF_SIZE = 640

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
        private val VIDEO_THUMB_FRAME = 5.seconds.inWholeMicroseconds
    }

    private val contentResolver = context.contentResolver

    override suspend fun process(
        uri: Uri,
        mediaType: MediaType,
        deleteOriginal: Boolean,
    ): Result<MediaUploadInfo> = runCatching {
        // Camera returns an 'octet-stream' mimetype, so it needs to be overridden
        val mimeType = contentResolver.getType(uri)
        val mimeTypeOrDefault = if (mimeType == MimeTypes.OctetStream) {
            when (mediaType) {
                MediaType.Image -> MimeTypes.Jpeg
                MediaType.Video -> MimeTypes.Mp4
                MediaType.Audio -> MimeTypes.Ogg
                else -> mimeType
            }
        } else {
            mimeType
        }
        val compressBeforeSending = mediaType in sequenceOf(MediaType.Image, MediaType.Video)
        val result = if (compressBeforeSending && mimeType != MimeTypes.Gif) {
            when (mediaType) {
                MediaType.Image -> processImage(uri)
                MediaType.Video -> processVideo(uri, mimeTypeOrDefault)
                MediaType.Audio -> processAudio(uri, mimeTypeOrDefault)
                else -> error("Cannot compress file of type: $mimeType")
            }
        } else {
            val file = copyToTmpFile(uri)
            // Remove image metadata here too
            if (mimeType.isMimeTypeImage() && mimeType != MimeTypes.Gif) {
                removeSensitiveImageMetadata(file)
            }
            val info = FileInfo(
                mimetype = mimeType,
                size = file.length(),
                thumbnailInfo = null,
                thumbnailSource = null,
            )
            MediaUploadInfo.AnyFile(file, info)
        }
        if (deleteOriginal) {
            contentResolver.delete(uri, null, null)
        }
        result
    }.mapFailure { MediaPreProcessor.Failure(it) }

    private suspend fun processImage(uri: Uri): MediaUploadInfo {
        val compressedFileResult = contentResolver.openInputStream(uri).use { input ->
            imageCompressor.compressToTmpFile(
                inputStream = requireNotNull(input),
                resizeMode = ResizeMode.Approximate(IMAGE_SCALE_REF_SIZE, IMAGE_SCALE_REF_SIZE),
            ).getOrThrow()
        }

        removeSensitiveImageMetadata(compressedFileResult.file)

        val thumbnailResult = compressedFileResult.file.inputStream().use { generateImageThumbnail(it) }
        val processingResult = compressedFileResult.toImageInfo(MimeTypes.Jpeg, thumbnailResult.file.path, thumbnailResult.info)
        return MediaUploadInfo.Image(compressedFileResult.file, processingResult, thumbnailResult)
    }

    private suspend fun processVideo(uri: Uri, mimeType: String?): MediaUploadInfo {
        val thumbnailInfo = extractVideoThumbnail(uri)
        val resultFile = videoCompressor.compress(uri)
            .onEach {
                // TODO handle progress
            }
            .filterIsInstance<VideoTranscodingEvent.Completed>()
            .first()
            .file

        val videoProcessingInfo = extractVideoMetadata(resultFile, mimeType, thumbnailInfo.file.path, thumbnailInfo)
        return MediaUploadInfo.Video(resultFile, videoProcessingInfo, thumbnailInfo)
    }

    private suspend fun processAudio(uri: Uri, mimeType: String?): MediaUploadInfo {
        val file = copyToTmpFile(uri)
        return MediaMetadataRetriever().runAndRelease {
            setDataSource(context, Uri.fromFile(file))

            val info = AudioInfo(
                duration = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L,
                size = file.length(),
                mimeType = mimeType,
            )

            MediaUploadInfo.Audio(file, info)
        }
    }

    private suspend fun generateImageThumbnail(inputStream: InputStream): ThumbnailProcessingInfo {
        val thumbnailResult = imageCompressor
            .compressToTmpFile(
                inputStream = inputStream,
                resizeMode = ResizeMode.Strict(THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT),
            ).getOrThrow()

        return thumbnailResult.toThumbnailProcessingInfo(MimeTypes.Jpeg)
    }

    private fun removeSensitiveImageMetadata(file: File) {
        // Remove GPS info, user comments and subject location tags
        val exifInterface = ExifInterface(file)
        // See ExifInterface.TAG_GPS_INFO_IFD_POINTER
        exifInterface.setAttribute("GPSInfoIFDPointer", null)
        exifInterface.setAttribute(ExifInterface.TAG_USER_COMMENT, null)
        exifInterface.setAttribute(ExifInterface.TAG_SUBJECT_LOCATION, null)
        tryOrNull { exifInterface.saveAttributes() }
    }

    private suspend fun createTmpFileWithInput(inputStream: InputStream): File? {
        return withContext(Dispatchers.IO) {
            tryOrNull {
                val tmpFile = context.createTmpFile()
                tmpFile.outputStream().use { inputStream.copyTo(it) }
                tmpFile
            }
        }
    }

    private fun extractVideoMetadata(file: File, mimeType: String?, thumbnailUrl: String?, thumbnailInfo: ThumbnailProcessingInfo?): VideoInfo =
        MediaMetadataRetriever().runAndRelease {
            setDataSource(context, Uri.fromFile(file))

            VideoInfo(
                duration = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L,
                width = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toLong() ?: 0L,
                height = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toLong() ?: 0L,
                mimetype = mimeType,
                size = file.length(),
                thumbnailInfo = thumbnailInfo?.info,
                thumbnailSource = thumbnailUrl?.let { MediaSource(it) },
                blurhash = thumbnailInfo?.blurhash,
            )
        }

    private suspend fun extractVideoThumbnail(uri: Uri): ThumbnailProcessingInfo =
        MediaMetadataRetriever().runAndRelease {
            setDataSource(context, uri)
            val bitmap = requireNotNull(getFrameAtTime(VIDEO_THUMB_FRAME))
            val inputStream = ByteArrayOutputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
                ByteArrayInputStream(it.toByteArray())
            }

            val result = imageCompressor.compressToTmpFile(
                inputStream = inputStream,
                resizeMode = ResizeMode.Strict(THUMB_MAX_WIDTH, THUMB_MAX_HEIGHT),
            )

            result.getOrThrow().toThumbnailProcessingInfo(MimeTypes.Jpeg)
        }

    private suspend fun copyToTmpFile(uri: Uri): File {
        return contentResolver.openInputStream(uri)?.use { createTmpFileWithInput(it) }
            ?: error("Could not copy the contents of $uri to a temporary file")
    }
}

fun ImageCompressionResult.toImageInfo(mimeType: String, thumbnailUrl: String?, thumbnailInfo: ThumbnailInfo?) = ImageInfo(
    width = width.toLong(),
    height = height.toLong(),
    mimetype = mimeType,
    size = size,
    thumbnailInfo = thumbnailInfo,
    thumbnailSource = thumbnailUrl?.let { MediaSource(it) },
    blurhash = blurhash,
)

fun ImageCompressionResult.toThumbnailProcessingInfo(mimeType: String) = ThumbnailProcessingInfo(
    file = file,
    info = ThumbnailInfo(
        width = width.toLong(),
        height = height.toLong(),
        mimetype = mimeType,
        size = size,
    ),
    blurhash = blurhash,
)
