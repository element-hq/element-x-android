/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
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
import com.squareup.anvil.annotations.ContributesBinding
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes.isMimeTypeVideo
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.mediaupload.api.MaxUploadSizeProvider
import io.element.android.libraries.mediaupload.api.compressorHelper
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import timber.log.Timber
import kotlin.math.roundToLong

class DefaultMediaOptimizationSelectorPresenter @AssistedInject constructor(
    @Assisted private val localMedia: LocalMedia,
    private val maxUploadSizeProvider: MaxUploadSizeProvider,
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val featureFlagService: FeatureFlagService,
    mediaExtractorFactory: VideoMetadataExtractor.Factory,
) : MediaOptimizationSelectorPresenter {
    @ContributesBinding(SessionScope::class)
    @AssistedFactory
    interface Factory : MediaOptimizationSelectorPresenter.Factory {
        override fun create(
            localMedia: LocalMedia,
        ): DefaultMediaOptimizationSelectorPresenter
    }

    private val mediaExtractor = mediaExtractorFactory.create(localMedia.uri)

    @Composable
    override fun present(): MediaOptimizationSelectorState {
        val displayMediaSelectorViews by produceState<Boolean?>(null) {
            value = featureFlagService.isFeatureEnabled(FeatureFlags.SelectableMediaQuality)
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

            val (videoDimensions, durationMs) = mediaExtractor.use {
                val size = it.getSize()
                    .getOrElse { exception ->
                        value = AsyncData.Failure(exception)
                        return@produceState
                    }

                val durationMs = it.getDuration()
                    .getOrElse { exception ->
                        value = AsyncData.Failure(exception)
                        return@produceState
                    }
                size to durationMs
            }

            val sizeEstimations = VideoCompressionPreset.entries
                .map { preset ->
                    val bitRateAsBytes = preset.compressorHelper().calculateOptimalBitrate(videoDimensions, 30) / 8f
                    val durationInSeconds = durationMs.toFloat() / 1_000
                    val calculatedSize = (bitRateAsBytes * durationInSeconds * 1.1f).roundToLong() // Adding 10% overhead for safety
                    VideoUploadEstimation(
                        preset = preset,
                        sizeInBytes = calculatedSize,
                        canUpload = calculatedSize <= (maxUploadSize as AsyncData.Success).data
                    )
                }
                .toPersistentList()
                .also { sizes ->
                    Timber.d(sizes.joinToString("\n") { "Calculated size for ${it.preset}: ${it.sizeInBytes} MB. Max upload size: $maxUploadSize" })
                }

            value = AsyncData.Success(sizeEstimations)
        }

        var selectedImageOptimization by remember { mutableStateOf<AsyncData<Boolean>>(AsyncData.Loading()) }
        var selectedVideoOptimizationPreset by remember { mutableStateOf<AsyncData<VideoCompressionPreset>>(AsyncData.Loading()) }

        LaunchedEffect(videoSizeEstimations.dataOrNull()) {
            selectedImageOptimization = AsyncData.Success(sessionPreferencesStore.doesOptimizeImages().first())
            // Find the best video preset based on the default preset and the video size estimations
            // Since the estimation for the current preset may be way too large to upload, we check the ones that provide lower file sizes
            selectedVideoOptimizationPreset = findBestVideoPreset(
                defaultVideoPreset = sessionPreferencesStore.getVideoCompressionPreset().first(),
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
            maxUploadSize = maxUploadSize,
            videoSizeEstimations = videoSizeEstimations,
            isImageOptimizationEnabled = selectedImageOptimization.dataOrNull(),
            selectedVideoPreset = selectedVideoOptimizationPreset.dataOrNull(),
            displayMediaSelectorViews = displayMediaSelectorViews,
            displayVideoPresetSelectorDialog = displayVideoPresetSelectorDialog,
            eventSink = { handleEvent(it) },
        )
    }

    private fun findBestVideoPreset(
        defaultVideoPreset: VideoCompressionPreset,
        videoSizeEstimations: AsyncData<ImmutableList<VideoUploadEstimation>>,
    ): AsyncData<VideoCompressionPreset> {
        val estimations = videoSizeEstimations.dataOrNull() ?: return AsyncData.Loading()
        // This will find the best video preset that can be used to produce a video that can be uploaded
        val bestEstimation = estimations.find { it.preset.ordinal >= defaultVideoPreset.ordinal && it.canUpload }?.preset
        return if (bestEstimation != null) {
            AsyncData.Success(bestEstimation)
        } else {
            AsyncData.Failure(
                IllegalStateException("No suitable video preset found for default preset: $defaultVideoPreset")
            )
        }
    }
}
