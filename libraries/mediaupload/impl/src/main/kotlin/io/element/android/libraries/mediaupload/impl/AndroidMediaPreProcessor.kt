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

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.file.getFileName
import io.element.android.libraries.androidutils.file.safeRenameTo
import io.element.android.libraries.androidutils.media.runAndRelease
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ContributesBinding(AppScope::class)
class AndroidMediaPreProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val thumbnailFactory: ThumbnailFactory,
    private val imageCompressor: ImageCompressor,
    private val videoCompressor: VideoCompressor,
    private val coroutineDispatchers: CoroutineDispatchers,
) : MediaPreProcessor {
    companion object {
        /**
         * Used for calculating `inSampleSize` for bitmaps.
         *
         * *Note*: Ideally, this should result in images of up to (but not included) 1280x1280 being sent. However, images with very different width and height
         * values may surpass this limit. (i.e.: an image of `480x3000px` would have `inSampleSize=1` and be sent as is).
         */
        private const val IMAGE_SCALE_REF_SIZE = 640

        private val notCompressibleImageTypes = listOf(MimeTypes.Gif, MimeTypes.WebP)
    }

    private val contentResolver = context.contentResolver

    override suspend fun process(
        uri: Uri,
        mimeType: String,
        deleteOriginal: Boolean,
        compressIfPossible: Boolean,
    ): Result<MediaUploadInfo> = withContext(coroutineDispatchers.computation) {
        runCatching {
            val result = when {
                mimeType.isMimeTypeImage() -> {
                    val shouldBeCompressed = compressIfPossible && mimeType !in notCompressibleImageTypes
                    processImage(uri, mimeType, shouldBeCompressed)
                }
                mimeType.isMimeTypeVideo() -> processVideo(uri, mimeType, compressIfPossible)
                mimeType.isMimeTypeAudio() -> processAudio(uri, mimeType)
                else -> processFile(uri, mimeType)
            }
            if (deleteOriginal) {
                tryOrNull {
                    contentResolver.delete(uri, null, null)
                }
            }
            result.postProcess(uri)
        }
    }.mapFailure { MediaPreProcessor.Failure(it) }

    private suspend fun processFile(uri: Uri, mimeType: String): MediaUploadInfo {
        val file = copyToTmpFile(uri)
        val info = FileInfo(
            mimetype = mimeType,
            size = file.length(),
            thumbnailInfo = null,
            thumbnailSource = null,
        )
        return MediaUploadInfo.AnyFile(file, info)
    }

    private fun MediaUploadInfo.postProcess(uri: Uri): MediaUploadInfo {
        val name = context.getFileName(uri) ?: return this
        val renamedFile = File(context.cacheDir, name).also {
            file.safeRenameTo(it)
        }
        return when (this) {
            is MediaUploadInfo.AnyFile -> copy(file = renamedFile)
            is MediaUploadInfo.Audio -> copy(file = renamedFile)
            is MediaUploadInfo.Image -> copy(file = renamedFile)
            is MediaUploadInfo.Video -> copy(file = renamedFile)
            is MediaUploadInfo.VoiceMessage -> copy(file = renamedFile)
        }
    }

    private suspend fun processImage(uri: Uri, mimeType: String, shouldBeCompressed: Boolean): MediaUploadInfo {
        suspend fun processImageWithCompression(): MediaUploadInfo {
            // Read the orientation metadata from its own stream. Trying to reuse this stream for compression will fail.
            val orientation = contentResolver.openInputStream(uri).use { input ->
                val exifInterface = input?.let { ExifInterface(it) }
                exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            } ?: ExifInterface.ORIENTATION_UNDEFINED

            val compressionResult = imageCompressor.compressToTmpFile(
                inputStreamProvider = { contentResolver.openInputStream(uri)!! },
                resizeMode = ResizeMode.Approximate(IMAGE_SCALE_REF_SIZE, IMAGE_SCALE_REF_SIZE),
                orientation = orientation,
            ).getOrThrow()
            val thumbnailResult = thumbnailFactory.createImageThumbnail(compressionResult.file)
            val imageInfo = compressionResult.toImageInfo(
                mimeType = mimeType,
                thumbnailResult = thumbnailResult
            )
            removeSensitiveImageMetadata(compressionResult.file)
            return MediaUploadInfo.Image(
                file = compressionResult.file,
                imageInfo = imageInfo,
                thumbnailFile = thumbnailResult?.file
            )
        }

        suspend fun processImageWithoutCompression(): MediaUploadInfo {
            val file = copyToTmpFile(uri)
            val thumbnailResult = thumbnailFactory.createImageThumbnail(file)
            val imageInfo = contentResolver.openInputStream(uri).use { input ->
                val bitmap = BitmapFactory.decodeStream(input, null, null)!!
                ImageInfo(
                    width = bitmap.width.toLong(),
                    height = bitmap.height.toLong(),
                    mimetype = mimeType,
                    size = file.length(),
                    thumbnailInfo = thumbnailResult?.info,
                    thumbnailSource = null,
                    blurhash = thumbnailResult?.blurhash,
                )
            }
            removeSensitiveImageMetadata(file)
            return MediaUploadInfo.Image(
                file = file,
                imageInfo = imageInfo,
                thumbnailFile = thumbnailResult?.file
            )
        }

        return if (shouldBeCompressed) {
            processImageWithCompression()
        } else {
            processImageWithoutCompression()
        }
    }

    private suspend fun processVideo(uri: Uri, mimeType: String?, shouldBeCompressed: Boolean): MediaUploadInfo {
        val resultFile = if (shouldBeCompressed) {
            videoCompressor.compress(uri)
                .onEach {
                    // TODO handle progress
                }
                .filterIsInstance<VideoTranscodingEvent.Completed>()
                .first()
                .file
        } else {
            copyToTmpFile(uri)
        }
        val thumbnailInfo = thumbnailFactory.createVideoThumbnail(resultFile)
        val videoInfo = extractVideoMetadata(resultFile, mimeType, thumbnailInfo)
        return MediaUploadInfo.Video(
            file = resultFile,
            videoInfo = videoInfo,
            thumbnailFile = thumbnailInfo?.file
        )
    }

    private suspend fun processAudio(uri: Uri, mimeType: String?): MediaUploadInfo {
        val file = copyToTmpFile(uri)
        return MediaMetadataRetriever().runAndRelease {
            setDataSource(context, Uri.fromFile(file))
            val info = AudioInfo(
                duration = extractDuration(),
                size = file.length(),
                mimetype = mimeType,
            )

            MediaUploadInfo.Audio(file, info)
        }
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
        return withContext(coroutineDispatchers.io) {
            tryOrNull {
                val tmpFile = context.createTmpFile()
                tmpFile.outputStream().use { inputStream.copyTo(it) }
                tmpFile
            }
        }
    }

    private fun extractVideoMetadata(file: File, mimeType: String?, thumbnailResult: ThumbnailResult?): VideoInfo =
        MediaMetadataRetriever().runAndRelease {
            setDataSource(context, Uri.fromFile(file))
            VideoInfo(
                duration = extractDuration(),
                width = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toLong() ?: 0L,
                height = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toLong() ?: 0L,
                mimetype = mimeType,
                size = file.length(),
                thumbnailInfo = thumbnailResult?.info,
                // Will be computed by the rust sdk
                thumbnailSource = null,
                blurhash = thumbnailResult?.blurhash,
            )
        }

    private suspend fun copyToTmpFile(uri: Uri): File {
        return contentResolver.openInputStream(uri)?.use { createTmpFileWithInput(it) }
            ?: error("Could not copy the contents of $uri to a temporary file")
    }
}

private fun ImageCompressionResult.toImageInfo(mimeType: String, thumbnailResult: ThumbnailResult?) = ImageInfo(
    width = width.toLong(),
    height = height.toLong(),
    mimetype = mimeType,
    size = size,
    thumbnailInfo = thumbnailResult?.info,
    // Will be computed by the rust sdk
    thumbnailSource = null,
    blurhash = thumbnailResult?.blurhash,
)

private fun MediaMetadataRetriever.extractDuration(): Duration {
    val durationInMs = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
    return durationInMs.milliseconds
}
