/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.mediapreview.api.MediaPreviewConfig
import io.element.android.features.mediapreview.api.SendMode
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeImage
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.mediaupload.api.MaxUploadSizeProvider
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.api.MediaSenderRoomFactory
import io.element.android.libraries.mediaupload.api.VideoMetadataExtractor
import io.element.android.libraries.mediaupload.api.compressorHelper
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.rememberMarkdownTextEditorState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToLong

@AssistedInject
class MediaPreviewPresenter(
    @Assisted private val localMedia: LocalMedia,
    @Assisted private val config: MediaPreviewConfig,
    @Assisted private val onSendListener: OnSendListener,
    @Assisted private val onCancelListener: OnCancelListener,
    private val featureFlagService: FeatureFlagService,
    private val maxUploadSizeProvider: MaxUploadSizeProvider,
    private val mediaOptimizationConfigProvider: MediaOptimizationConfigProvider,
    private val mediaSenderRoomFactory: MediaSenderRoomFactory,
    private val dispatchers: CoroutineDispatchers,
    private val videoMetadataExtractorFactory: VideoMetadataExtractor.Factory,
) : Presenter<MediaPreviewState> {
    @AssistedFactory
    interface Factory {
        fun create(
            localMedia: LocalMedia,
            config: MediaPreviewConfig,
            onSendListener: OnSendListener,
            onCancelListener: OnCancelListener,
        ): MediaPreviewPresenter
    }

    fun interface OnSendListener {
        fun onSend(
            caption: String?,
            optimizeImage: Boolean,
            videoPreset: VideoCompressionPreset?,
            onComplete: () -> Unit,
        )
    }

    fun interface OnCancelListener {
        fun onCancel()
    }

    private var mediaSender: io.element.android.libraries.mediaupload.api.MediaSender? = null

    @Composable
    override fun present(): MediaPreviewState {
        val coroutineScope = rememberCoroutineScope()
        val sendMode = config.sendMode

        val sendActionState = remember {
            mutableStateOf<SendActionState>(SendActionState.Idle)
        }

        val markdownTextEditorState = rememberMarkdownTextEditorState(initialText = config.initialCaption, initialFocus = false)
        val textEditorState by rememberUpdatedState(
            TextEditorState.Markdown(markdownTextEditorState, isRoomEncrypted = null)
        )

        val currentCaption: () -> String = {
            (textEditorState as TextEditorState.Markdown).state.text.value().toString()
        }

        var preprocessMediaJob by remember { mutableStateOf<Job?>(null) }

        val mediaMimeType = localMedia.info.mimeType
        val isImageFile = mediaMimeType.isMimeTypeImage()
        val isVideoFile = mediaMimeType.isMimeTypeVideo()
        val selectableMediaQualityEnabled by produceState(initialValue = false) {
            value = featureFlagService.isFeatureEnabled(FeatureFlags.SelectableMediaQuality)
        }
        val showOptimization = selectableMediaQualityEnabled && (isImageFile || isVideoFile)

        val maxUploadSize by produceState(initialValue = 100L * 1024 * 1024) {
            value = maxUploadSizeProvider.getMaxUploadSize().getOrElse { 100L * 1024 * 1024 }
        }

        val videoSizeEstimations = if (isVideoFile) {
            videoMetadataExtractorFactory.create(localMedia.uri).use { extractor ->
                val videoDimensions = extractor.getSize().getOrElse { null }
                val duration = extractor.getDuration().getOrElse { null }

                if (videoDimensions != null && duration != null) {
                    VideoCompressionPreset.entries.map { preset ->
                        val bitRateAsBytes = preset.compressorHelper().calculateOptimalBitrate(videoDimensions, 30) / 8f
                        val durationInSeconds = duration.inWholeSeconds.toFloat()
                        val calculatedSize = (bitRateAsBytes * durationInSeconds * 1.1f).roundToLong()
                            .coerceAtLeast(1024L)
                        VideoUploadEstimation(
                            preset = preset,
                            sizeInBytes = calculatedSize,
                            canUpload = calculatedSize <= maxUploadSize
                        )
                    }.toImmutableList()
                } else {
                    emptyList()
                }
            }
        } else {
            emptyList()
        }

        val defaultOptimizationConfig by produceState(initialValue = MediaOptimizationConfig()) {
            value = mediaOptimizationConfigProvider.get()
        }
        var isImageOptimizationEnabled by remember { mutableStateOf(defaultOptimizationConfig.compressImages) }
        var selectedVideoPreset by remember { mutableStateOf(defaultOptimizationConfig.videoCompressionPreset) }
        var showVideoQualityDialog by remember { mutableStateOf(false) }
        var displayFileTooLargeError by remember { mutableStateOf(false) }

        fun handleEvent(event: MediaPreviewEvents) {
            when (event) {
                is MediaPreviewEvents.Send -> {
                    if (sendMode == SendMode.PREPROCESS) {
                        val caption = currentCaption()
                        preprocessMediaJob = coroutineScope.launch {
                            val optimizationConfig = MediaOptimizationConfig(
                                compressImages = isImageOptimizationEnabled,
                                videoCompressionPreset = selectedVideoPreset,
                            )

                            preProcessMedia(
                                mediaOptimizationConfig = optimizationConfig,
                                displayProgress = true,
                                sendActionState = sendActionState,
                                caption = caption,
                            )
                        }
                    } else {
                        val caption = currentCaption()
                        onSendListener.onSend(
                            caption = caption,
                            optimizeImage = isImageOptimizationEnabled,
                            videoPreset = selectedVideoPreset,
                            onComplete = { onCancelListener.onCancel() }
                        )
                    }
                }
                MediaPreviewEvents.Cancel -> {
                    preprocessMediaJob?.cancel()
                    mediaSender?.cleanUp()
                    onCancelListener.onCancel()
                }
                is MediaPreviewEvents.ToggleImageOptimization -> {
                    isImageOptimizationEnabled = event.enabled
                }
                is MediaPreviewEvents.SelectVideoQuality -> {
                    selectedVideoPreset = event.preset
                    showVideoQualityDialog = false
                }
                MediaPreviewEvents.ShowVideoQualityDialog -> {
                    showVideoQualityDialog = true
                }
                MediaPreviewEvents.DismissVideoQualityDialog -> {
                    showVideoQualityDialog = false
                }
                MediaPreviewEvents.Retry -> {
                    val caption = currentCaption()
                    preprocessMediaJob = coroutineScope.launch {
                        preProcessMedia(
                            mediaOptimizationConfig = MediaOptimizationConfig(
                                compressImages = isImageOptimizationEnabled,
                                videoCompressionPreset = selectedVideoPreset,
                            ),
                            displayProgress = true,
                            sendActionState = sendActionState,
                            caption = caption,
                        )
                    }
                }
                MediaPreviewEvents.ClearError -> {
                    sendActionState.value = SendActionState.Idle
                }
            }
        }

        return MediaPreviewState(
            localMedia = localMedia,
            textEditorState = textEditorState,
            sendActionState = sendActionState.value,
            showOptimizationOptions = showOptimization,
            isImageOptimizationEnabled = isImageOptimizationEnabled,
            selectedVideoPreset = selectedVideoPreset,
            showVideoQualityDialog = showVideoQualityDialog,
            displayFileTooLargeError = displayFileTooLargeError,
            videoSizeEstimations = videoSizeEstimations,
            maxUploadSize = maxUploadSize,
            eventSink = ::handleEvent,
        )
    }

    private suspend fun CoroutineScope.preProcessMedia(
        mediaOptimizationConfig: MediaOptimizationConfig,
        displayProgress: Boolean,
        sendActionState: MutableState<SendActionState>,
        caption: String,
    ) {
        val sender = mediaSender ?: mediaSenderRoomFactory.create(config.joinedRoom!!).also { mediaSender = it }
        sendActionState.value = SendActionState.Sending.Processing(displayProgress = displayProgress)
        sender.preProcessMedia(
            uri = localMedia.uri,
            mimeType = localMedia.info.mimeType,
            mediaOptimizationConfig = mediaOptimizationConfig,
        ).fold(
            onSuccess = { mediaUploadInfo ->
                Timber.d("Media preprocessing finished, ready to upload")
                sendActionState.value = SendActionState.Sending.ReadyToUpload(mediaUploadInfo)
                launch(dispatchers.io) {
                    sendPreProcessedMedia(
                        mediaUploadInfo = mediaUploadInfo,
                        caption = caption,
                        optimizeImage = mediaOptimizationConfig.compressImages,
                        videoPreset = mediaOptimizationConfig.videoCompressionPreset,
                        sendActionState = sendActionState,
                    )
                }
            },
            onFailure = { error ->
                Timber.e(error, "Failed to pre-process media")
                if (error is CancellationException) {
                    throw error
                }
                sendActionState.value = SendActionState.Failure(error, null)
            }
        )
    }

    private suspend fun sendPreProcessedMedia(
        mediaUploadInfo: io.element.android.libraries.mediaupload.api.MediaUploadInfo,
        caption: String?,
        optimizeImage: Boolean,
        videoPreset: VideoCompressionPreset,
        sendActionState: MutableState<SendActionState>,
    ) {
        val sender = mediaSender ?: mediaSenderRoomFactory.create(config.joinedRoom!!).also { mediaSender = it }
        runCatchingExceptions {
            sendActionState.value = SendActionState.Sending.Uploading(mediaUploadInfo)
            sender.sendPreProcessedMedia(
                mediaUploadInfo = mediaUploadInfo,
                caption = caption,
                formattedCaption = null,
                inReplyToEventId = config.inReplyToEventId,
            ).getOrThrow()
        }.fold(
            onSuccess = {
                sender.cleanUp()
                sendActionState.value = SendActionState.Done
                onSendListener.onSend(
                    caption = caption,
                    optimizeImage = optimizeImage,
                    videoPreset = videoPreset,
                    onComplete = { onCancelListener.onCancel() }
                )
            },
            onFailure = { error ->
                if (error is CancellationException) {
                    throw error
                }
                Timber.e(error, "Failed to send attachment")
                sendActionState.value = SendActionState.Failure(error, mediaUploadInfo)
            }
        )
    }
}
