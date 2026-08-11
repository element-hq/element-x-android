/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.mediaupload.api.MaxUploadSizeProvider
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.api.compressorHelper
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import kotlin.math.roundToLong

@AssistedInject
class DefaultMediaOptimizationSelectorPresenter(
    @Assisted private val index: Int,
    @Assisted private val localMedia: LocalMedia,
    @Assisted private val sendAsFile: Boolean,
    private val maxUploadSizeProvider: MaxUploadSizeProvider,
    private val featureFlagService: FeatureFlagService,
    private val mediaOptimizationConfigProvider: MediaOptimizationConfigProvider,
    private val videoCompressionPresetSelector: VideoCompressionPresetSelector,
    mediaExtractorFactory: VideoMetadataExtractor.Factory,
) : MediaOptimizationSelectorPresenter {
    companion object {
        /**
         * Approximate bitrate (in bits per second) used by the AAC audio encoder for compressed videos.
         * The video pre-processor doesn't override the audio encoder settings, so this should track
         * whatever the platform default AAC bitrate ends up being. Used to avoid under-estimating the
         * final file size, since the video bitrate alone doesn't account for the audio track.
         */
        private const val ASSUMED_AUDIO_BITRATE = 128_000
    }

    @ContributesBinding(SessionScope::class)
    @AssistedFactory
    interface Factory : MediaOptimizationSelectorPresenter.Factory {
        override fun create(
            index: Int,
            localMedia: LocalMedia,
            sendAsFile: Boolean,
        ): DefaultMediaOptimizationSelectorPresenter
    }

    private val mediaExtractor = mediaExtractorFactory.create(localMedia.uri)

    @Composable
    override fun present(): MediaOptimizationSelectorState {
        val displayMediaSelectorViews by produceState<Boolean?>(null) {
            // When sending as a raw file, never show the optimization selector: images skip
            // recompression, while videos use the highest available best-fit preset.
            value = !sendAsFile && featureFlagService.isFeatureEnabled(FeatureFlags.SelectableMediaQuality)
        }

        var displayVideoPresetSelectorDialog by remember { mutableStateOf(false) }

        val maxUploadSize by produceState(AsyncData.Loading()) {
            maxUploadSizeProvider.getMaxUploadSize().fold(
                onSuccess = { value = AsyncData.Success(it) },
                onFailure = {
                    Timber.e(it, "Failed to retrieve max upload size for video optimization selector")
                    value = AsyncData.Success((100 * 1024 * 1024).toLong()) // Default to 100 MB if we can't retrieve the max upload size
                }
            )
        }

        val mediaMimeType = localMedia.info.mimeType

        val videoSizeEstimations by produceState<AsyncData<ImmutableList<VideoUploadEstimation>>>(
            initialValue = AsyncData.Loading(),
            key1 = maxUploadSize,
        ) {
            if (maxUploadSize !is AsyncData.Success) {
                return@produceState
            }

            if (!mediaMimeType.isMimeTypeVideo()) {
                value = AsyncData.Uninitialized
                return@produceState
            }

            val (videoDimensions, duration) = mediaExtractor.use {
                val size = it.getSize()
                    .getOrElse { exception ->
                        value = AsyncData.Failure(exception)
                        return@produceState
                    }

                val duration = it.getDuration()
                    .getOrElse { exception ->
                        value = AsyncData.Failure(exception)
                        return@produceState
                    }
                size to duration
            }

            val sizeEstimations = VideoCompressionPreset.entries
                .map { preset ->
                    val videoBitRate = preset.compressorHelper().calculateOptimalBitrate(videoDimensions, 30)
                    // The output always includes an AAC audio track too; ignoring it made the estimate
                    // systematically lower than the actual compressed file size.
                    val combinedBitRateAsBytes = (videoBitRate + ASSUMED_AUDIO_BITRATE) / 8f
                    val durationInSeconds = duration.inWholeSeconds.toFloat()
                    val calculatedSize = (combinedBitRateAsBytes * durationInSeconds * 1.1f).roundToLong() // Adding 10% overhead for safety
                    VideoUploadEstimation(
                        preset = preset,
                        sizeInBytes = calculatedSize,
                        canUpload = calculatedSize <= (maxUploadSize as AsyncData.Success).data
                    )
                }
                .toImmutableList()
                .also { sizes ->
                    Timber.d(sizes.joinToString("\n") { "Calculated size for ${it.preset}: ${it.sizeInBytes} MB. Max upload size: $maxUploadSize" })
                }

            value = AsyncData.Success(sizeEstimations)
        }

        var selectedImageOptimization by remember { mutableStateOf<AsyncData<Boolean>>(AsyncData.Loading()) }
        var selectedVideoOptimizationPreset by remember { mutableStateOf<AsyncData<VideoCompressionPreset>>(AsyncData.Loading()) }

        LaunchedEffect(videoSizeEstimations.dataOrNull()) {
            if (sendAsFile) {
                // Send-as-file path: pin to no image compression, and pick the highest-quality
                // video preset that still fits the upload limit (we have no true "do not re-encode
                // video" path in the pre-processor right now).
                selectedImageOptimization = AsyncData.Success(false)
                selectedVideoOptimizationPreset = videoCompressionPresetSelector.selectBestVideoPreset(
                    expectedVideoPreset = VideoCompressionPreset.HIGH,
                    videoSizeEstimations = videoSizeEstimations,
                )
                return@LaunchedEffect
            }
            val mediaOptimizationConfig = mediaOptimizationConfigProvider.get()
            selectedImageOptimization = AsyncData.Success(mediaOptimizationConfig.compressImages)
            // Find the best video preset based on the default preset and the video size estimations
            // Since the estimation for the current preset may be way too large to upload, we check the ones that provide lower file sizes
            selectedVideoOptimizationPreset = videoCompressionPresetSelector.selectBestVideoPreset(
                expectedVideoPreset = mediaOptimizationConfig.videoCompressionPreset,
                videoSizeEstimations = videoSizeEstimations,
            )
        }

        fun handleEvent(event: MediaOptimizationSelectorEvent) {
            when (event) {
                is MediaOptimizationSelectorEvent.SelectImageOptimization -> {
                    selectedImageOptimization = AsyncData.Success(event.enabled)
                }
                is MediaOptimizationSelectorEvent.SelectVideoPreset -> {
                    val estimations = videoSizeEstimations.dataOrNull()
                    if (estimations != null) {
                        val preset = estimations.find { it.preset == event.preset }
                        if (preset == null) {
                            Timber.e("Selected video preset ${event.preset} is not available in the estimations")
                            return
                        }
                        if (!preset.canUpload) {
                            Timber.w("Selected video preset ${event.preset} exceeds max upload size")
                            return
                        }
                    } else {
                        Timber.e("Video size estimations are not available")
                        return
                    }
                    selectedVideoOptimizationPreset = AsyncData.Success(event.preset)
                    displayVideoPresetSelectorDialog = false
                }
                is MediaOptimizationSelectorEvent.OpenVideoPresetSelectorDialog -> {
                    displayVideoPresetSelectorDialog = true
                }
                is MediaOptimizationSelectorEvent.DismissVideoPresetSelectorDialog -> {
                    displayVideoPresetSelectorDialog = false
                }
            }
        }

        return MediaOptimizationSelectorState(
            index = index,
            maxUploadSize = maxUploadSize,
            videoSizeEstimations = videoSizeEstimations,
            isImageOptimizationEnabled = selectedImageOptimization.dataOrNull(),
            selectedVideoPreset = selectedVideoOptimizationPreset.dataOrNull(),
            displayMediaSelectorViews = displayMediaSelectorViews,
            displayVideoPresetSelectorDialog = displayVideoPresetSelectorDialog,
            eventSink = ::handleEvent,
        )
    }
}/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaupload.impl

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Size
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.InAppMp4Muxer
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.file.createTmpFile
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@Inject
class VideoCompressor(
    @ApplicationContext private val context: Context,
) {
    @OptIn(UnstableApi::class)
    fun compress(uri: Uri, videoCompressionPreset: VideoCompressionPreset): Flow<VideoTranscodingEvent> = callbackFlow {
        val metadata = getVideoMetadata(uri)

        val videoCompressorConfig = VideoCompressorConfigFactory.create(
            metadata = metadata,
            preset = videoCompressionPreset,
        )

        val tmpFile = context.createTmpFile(extension = "mp4")

        val width = metadata?.width ?: Int.MAX_VALUE
        val height = metadata?.height ?: Int.MAX_VALUE

        val videoResizeEffect = run {
            val outputSize = videoCompressorConfig.videoCompressorHelper.getOutputSize(Size(width, height))
            if (metadata?.rotation == 90 || metadata?.rotation == 270) {
                // If the video is rotated, we need to swap width and height
                Presentation.createForWidthAndHeight(
                    outputSize.height,
                    outputSize.width,
                    Presentation.LAYOUT_SCALE_TO_FIT,
                )
            } else {
                // Otherwise, we can use the original width and height
                Presentation.createForWidthAndHeight(
                    outputSize.width,
                    outputSize.height,
                    Presentation.LAYOUT_SCALE_TO_FIT,
                )
            }
        }

        // If we are resizing, we also want to reduce set frame rate to the default value (30fps)
        val newFrameRate = videoCompressorConfig.newFrameRate

        // If we need to resize the video, we also want to recalculate the bitrate
        val newBitrate = videoCompressorConfig.newBitRate

        // Remove all video metadata
        val removeMetadataMuxer = InAppMp4Muxer.Factory { metadataEntries ->
            metadataEntries.removeAll { true }
        }
        val inputMediaItem = MediaItem.fromUri(uri)
        val outputMediaItem = EditedMediaItem.Builder(inputMediaItem)
            .setFrameRate(newFrameRate)
            .setEffects(Effects(emptyList(), listOf(videoResizeEffect)))
            .build()

        val encoderFactory = DefaultEncoderFactory.Builder(context)
            .setRequestedVideoEncoderSettings(
                VideoEncoderSettings.Builder()
                    // Use VBR which is generally better for quality and compatibility, although slightly worse for file size
                    .setBitrateMode(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
                    .setBitrate(newBitrate)
                    .build()
            )
            .build()

        val videoTransformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .setPortraitEncodingEnabled(false)
            .setEncoderFactory(encoderFactory)
            .setMuxerFactory(removeMetadataMuxer)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    trySend(VideoTranscodingEvent.Completed(tmpFile))
                    close()
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    Timber.e(exportException, "Video transcoding failed")
                    tmpFile.safeDelete()
                    close(exportException)
                }

                override fun onFallbackApplied(
                    composition: Composition,
                    originalTransformationRequest: TransformationRequest,
                    fallbackTransformationRequest: TransformationRequest
                ) = Unit
            })
            .build()

        val progressJob = launch(Dispatchers.Main) {
            val progressHolder = ProgressHolder()
            while (isActive) {
                val state = videoTransformer.getProgress(progressHolder)
                if (state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    channel.send(VideoTranscodingEvent.Progress(progressHolder.progress.toFloat()))
                }
                delay(500)
            }
        }

        withContext(Dispatchers.Main) {
            videoTransformer.start(outputMediaItem, tmpFile.path)
        }

        awaitClose {
            progressJob.cancel()
        }
    }

    private fun getVideoMetadata(uri: Uri): VideoFileMetadata? {
        return runCatchingExceptions {
            MediaMetadataRetriever().use {
                it.setDataSource(context, uri)

                val width = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: -1
                val height = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: -1
                val bitrate = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull() ?: -1
                val frameRate = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toIntOrNull() ?: -1
                val rotation = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

                val (actualWidth, actualHeight) = if (width == -1 || height == -1) {
                    // Try getting the first frame instead
                    val bitmap = it.getFrameAtTime(0) ?: return null
                    bitmap.width to bitmap.height
                } else {
                    width to height
                }

                VideoFileMetadata(
                    width = actualWidth,
                    height = actualHeight,
                    bitrate = bitrate,
                    frameRate = frameRate,
                    rotation = rotation,
                )
            }
        }.onFailure {
            Timber.e(it, "Failed to get video dimensions")
        }.getOrNull()
    }
}

internal data class VideoFileMetadata(
    val width: Int,
    val height: Int,
    val bitrate: Long,
    val frameRate: Int,
    val rotation: Int,
)

sealed interface VideoTranscodingEvent {
    data class Progress(val value: Float) : VideoTranscodingEvent
    data class Completed(val file: File) : VideoTranscodingEvent
}
