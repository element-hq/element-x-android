/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.file.getFileName
import io.element.android.libraries.androidutils.file.safeRenameTo
import io.element.android.libraries.androidutils.hash.hash
import io.element.android.libraries.androidutils.media.runAndRelease
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.extensions.mapFailure
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeAudio
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@ContributesBinding(AppScope::class)
class AndroidMediaPreProcessor(
    @ApplicationContext private val context: Context,
    private val thumbnailFactory: ThumbnailFactory,
    private val imageCompressor: ImageCompressor,
    private val videoCompressor: VideoCompressor,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val temporaryUriDeleter: TemporaryUriDeleter,
) : MediaPreProcessor {
    companion object {
        /**
         * Used for calculating `inSampleSize` for bitmaps.
         *
         * *Note*: Ideally, this should result in images of up to (but not included) 1280x1280 being sent. However, images with very different width and height
         * values may surpass this limit. (i.e.: an image of `480x3000px` would have `inSampleSize=1` and be sent as is).
         */
        private const val IMAGE_SCALE_REF_SIZE = 640

        private val notCompressibleImageTypes = listOf(MimeTypes.Gif, MimeTypes.WebP, MimeTypes.Svg)
    }

    private val contentResolver = context.contentResolver

    private val cacheDir = context.cacheDir
    private val baseTmpFileDir = File(cacheDir, "uploads")

    override suspend fun process(
        uri: Uri,
        mimeType: String,
        deleteOriginal: Boolean,
        mediaOptimizationConfig: MediaOptimizationConfig,
    ): Result<MediaUploadInfo> = withContext(coroutineDispatchers.computation) {
        runCatchingExceptions {
            val result = when {
                // Special case for SVG, since Android can't read its metadata or create a thumbnail, it must be sent as a file
                mimeType == MimeTypes.Svg -> {
                    processFile(uri, mimeType)
                }
                mimeType.isMimeTypeImage() -> {
                    val shouldBeCompressed = mediaOptimizationConfig.compressImages && mimeType !in notCompressibleImageTypes
                    processImage(uri, mimeType, shouldBeCompressed)
                }
                mimeType.isMimeTypeVideo() -> processVideo(uri, mimeType, mediaOptimizationConfig.videoCompressionPreset)
                mimeType.isMimeTypeAudio() -> processAudio(uri, mimeType)
                else -> processFile(uri, mimeType)
            }
            if (deleteOriginal) {
                tryOrNull {
                    Timber.w("Deleting original uri $uri")
                    contentResolver.delete(uri, null, null)
                }
            } else {
                temporaryUriDeleter.delete(uri)
            }
            result.postProcess(uri)
        }
    }.mapFailure { MediaPreProcessor.Failure(it) }

    override fun cleanUp() {
        Timber.d("Cleaning up temporary media files")

        // Clear temporary files created in older versions of the app
        cacheDir.listFiles()?.onEach { file ->
            if (file.isFile) {
                val nameWithoutExtension = file.nameWithoutExtension
                // UUIDs are 36 characters long, so we check if we can take those 36 characters
                val nameWithoutExtensionAndRandom = if (nameWithoutExtension.length > 36) {
                    nameWithoutExtension.substring(0, 36)
                } else {
                    // Not a temp file
                    return@onEach
                }
                val isUUID = tryOrNull { UUID.fromString(nameWithoutExtensionAndRandom) } != null
                if (isUUID && file.extension.isNotEmpty()) {
                    file.delete()
                }
            }
        }
        // Clear temporary files created by this pre-processor in the separate uploads directory
        baseTmpFileDir.listFiles()?.onEach { it.delete() }
    }

    private suspend fun processFile(uri: Uri, mimeType: String): MediaUploadInfo {
        Timber.d("Processing file ${uri.path.orEmpty().hash()}")
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
        Timber.d("Finished processing, post-processing ${uri.path.orEmpty().hash()}")
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
        Timber.d("Processing image ${uri.path.orEmpty().hash()}")
        suspend fun processImageWithCompression(): MediaUploadInfo {
            // Read the orientation metadata from its own stream. Trying to reuse this stream for compression will fail.
            val orientation = contentResolver.openInputStream(uri).use { input ->
                val exifInterface = input?.let { ExifInterface(it) }
                exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
            } ?: ExifInterface.ORIENTATION_UNDEFINED

            val compressionResult = imageCompressor.compressToTmpFile(
                inputStreamProvider = { contentResolver.openInputStream(uri)!! },
                resizeMode = ResizeMode.Approximate(IMAGE_SCALE_REF_SIZE, IMAGE_SCALE_REF_SIZE),
                mimeType = mimeType,
                orientation = orientation,
            ).getOrThrow()
            val thumbnailResult = thumbnailFactory.createImageThumbnail(
                file = compressionResult.file,
                mimeType = mimeType,
            )
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
            val thumbnailResult = thumbnailFactory.createImageThumbnail(
                file = file,
                mimeType = mimeType,
            )
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

    private suspend fun processVideo(uri: Uri, mimeType: String?, videoCompressionPreset: VideoCompressionPreset): MediaUploadInfo {
        Timber.d("Processing video ${uri.path.orEmpty().hash()}")
        val resultFile = runCatchingExceptions {
            videoCompressor.compress(uri, videoCompressionPreset)
                .onEach {
                    if (it is VideoTranscodingEvent.Progress) {
                        Timber.d("Video compression progress: ${it.value}%")
                    } else if (it is VideoTranscodingEvent.Completed) {
                        Timber.d("Video compression completed: ${it.file.path}")
                    }
                }
                .filterIsInstance<VideoTranscodingEvent.Completed>()
                .first()
                .file
        }
            .onFailure {
                Timber.e(it, "Failed to compress video: $uri")
            }
            .getOrNull()

        if (resultFile != null) {
            val thumbnailInfo = thumbnailFactory.createVideoThumbnail(resultFile)
            val videoInfo = extractVideoMetadata(resultFile, mimeType, thumbnailInfo)
            return MediaUploadInfo.Video(
                file = resultFile,
                videoInfo = videoInfo,
                thumbnailFile = thumbnailInfo?.file
            )
        } else {
            Timber.d("Could not transcode video ${uri.path.orEmpty().hash()}, sending original file as plain file")
            // If the video could not be compressed, just use the original one, but send it as a file
            return processFile(uri, MimeTypes.OctetStream)
        }
    }

    private suspend fun processAudio(uri: Uri, mimeType: String?): MediaUploadInfo {
        Timber.d("Processing audio ${uri.path.orEmpty().hash()}")
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
        ExifInterface(file).apply {
            // See ExifInterface.TAG_GPS_INFO_IFD_POINTER
            setAttribute("GPSInfoIFDPointer", null)
            setAttribute(ExifInterface.TAG_USER_COMMENT, null)
            setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, null)

            setAttribute(ExifInterface.TAG_GPS_VERSION_ID, null)
            setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, null)
            setAttribute(ExifInterface.TAG_GPS_ALTITUDE, null)
            setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, null)
            setAttribute(ExifInterface.TAG_GPS_DATESTAMP, null)
            setAttribute(ExifInterface.TAG_GPS_SATELLITES, null)
            setAttribute(ExifInterface.TAG_GPS_STATUS, null)
            setAttribute(ExifInterface.TAG_GPS_MEASURE_MODE, null)
            setAttribute(ExifInterface.TAG_GPS_DOP, null)
            setAttribute(ExifInterface.TAG_GPS_SPEED_REF, null)
            setAttribute(ExifInterface.TAG_GPS_SPEED, null)
            setAttribute(ExifInterface.TAG_GPS_TRACK_REF, null)
            setAttribute(ExifInterface.TAG_GPS_TRACK, null)
            setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION_REF, null)
            setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION, null)
            setAttribute(ExifInterface.TAG_GPS_MAP_DATUM, null)
            setAttribute(ExifInterface.TAG_GPS_DEST_BEARING_REF, null)
            setAttribute(ExifInterface.TAG_GPS_DEST_BEARING, null)
            setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE_REF, null)
            setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE, null)
            setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, null)
            setAttribute(ExifInterface.TAG_GPS_AREA_INFORMATION, null)
            setAttribute(ExifInterface.TAG_GPS_DIFFERENTIAL, null)
            setAttribute(ExifInterface.TAG_GPS_H_POSITIONING_ERROR, null)
            setAttribute(ExifInterface.TAG_GPS_LATITUDE, null)
            setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, null)
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE, null)
            setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, null)
            setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE, null)
            setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, null)
            tryOrNull { saveAttributes() }
        }
    }

    private suspend fun createTmpFileWithInput(inputStream: InputStream): File? {
        return withContext(coroutineDispatchers.io) {
            tryOrNull {
                if (!baseTmpFileDir.exists()) {
                    baseTmpFileDir.mkdirs()
                }
                val tmpFile = context.createTmpFile(baseTmpFileDir)
                tmpFile.outputStream().use { inputStream.copyTo(it) }
                tmpFile
            }
        }
    }

    private fun extractVideoMetadata(file: File, mimeType: String?, thumbnailResult: ThumbnailResult?): VideoInfo =
        MediaMetadataRetriever().runAndRelease {
            setDataSource(context, Uri.fromFile(file))

            val rotation = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toInt() ?: 0
            val rawWidth = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toLong() ?: 0L
            val rawHeight = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toLong() ?: 0L

            val (width, height) = if (rotation == 90 || rotation == 270) rawHeight to rawWidth else rawWidth to rawHeight

            VideoInfo(
                duration = extractDuration(),
                width = width,
                height = height,
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
