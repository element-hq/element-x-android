/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.attachments.video

import android.net.Uri
import android.util.Size
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.test.attachments.video.FakeVideoMetadataExtractor
import io.element.android.features.messages.test.attachments.video.FakeVideoMetadataExtractorFactory
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.mediaupload.api.MaxUploadSizeProvider
import io.element.android.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.minutes

@RunWith(AndroidJUnit4::class)
class DefaultMediaOptimizationSelectorPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUrl: Uri = mockk("localMediaUri")

    @Test
    fun `present - initial state`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter()
        presenter.test {
            awaitItem().run {
                // Loading
                assertThat(videoSizeEstimations).isInstanceOf(AsyncData.Loading::class.java)
                assertThat(maxUploadSize).isInstanceOf(AsyncData.Loading::class.java)
                // Not loaded yet
                assertThat(isImageOptimizationEnabled).isNull()
                assertThat(selectedVideoPreset).isNull()
                assertThat(displayMediaSelectorViews).isNull()
                assertThat(displayVideoPresetSelectorDialog).isFalse()
            }

            // The data will load after the first recomposition
            awaitItem().run {
                assertThat(videoSizeEstimations).isInstanceOf(AsyncData.Success::class.java)
                assertThat(maxUploadSize).isInstanceOf(AsyncData.Success::class.java)
                assertThat(isImageOptimizationEnabled).isTrue()
                assertThat(selectedVideoPreset).isEqualTo(VideoCompressionPreset.STANDARD)
                assertThat(displayMediaSelectorViews).isTrue()
                assertThat(displayVideoPresetSelectorDialog).isFalse()
            }
        }
    }

    @Test
    fun `present - if media info is not video, the video state won't load`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            localMedia = aLocalMedia(mockMediaUrl, anImageMediaInfo())
        )
        presenter.test {
            // Skip loading state
            skipItems(1)

            // The data will load after the first recomposition
            awaitItem().run {
                assertThat(videoSizeEstimations).isInstanceOf(AsyncData.Uninitialized::class.java)
                assertThat(selectedVideoPreset).isNull()
            }
        }
    }

    @Test
    fun `present - OpenVideoPresetSelectorDialog displays it, DismissVideoPresetSelectorDialog hides it`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter()
        presenter.test {
            // Skip loading state
            val eventSink = awaitItem().eventSink

            assertThat(awaitItem().displayVideoPresetSelectorDialog).isFalse()

            eventSink(MediaOptimizationSelectorEvent.OpenVideoPresetSelectorDialog)

            assertThat(awaitItem().displayVideoPresetSelectorDialog).isTrue()

            eventSink(MediaOptimizationSelectorEvent.DismissVideoPresetSelectorDialog)

            assertThat(awaitItem().displayVideoPresetSelectorDialog).isFalse()
        }
    }

    @Test
    fun `present - SelectVideoPreset sets it and dismisses the dialog`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter()
        presenter.test {
            // Skip loading state
            val eventSink = awaitItem().eventSink

            assertThat(awaitItem().displayVideoPresetSelectorDialog).isFalse()

            eventSink(MediaOptimizationSelectorEvent.OpenVideoPresetSelectorDialog)

            assertThat(awaitItem().displayVideoPresetSelectorDialog).isTrue()

            eventSink(MediaOptimizationSelectorEvent.SelectVideoPreset(VideoCompressionPreset.LOW))

            assertThat(awaitItem().selectedVideoPreset).isEqualTo(VideoCompressionPreset.LOW)
            assertThat(awaitItem().displayVideoPresetSelectorDialog).isFalse()
        }
    }

    @Test
    fun `present - SelectVideoPreset won't do anything if there is no metadata`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            mediaExtractorFactory = FakeVideoMetadataExtractorFactory(FakeVideoMetadataExtractor(sizeResult = Result.failure(AN_EXCEPTION))),
        )
        presenter.test {
            // Skip loading state
            val eventSink = awaitItem().eventSink

            assertThat(awaitItem().videoSizeEstimations.dataOrNull()).isNull()

            eventSink(MediaOptimizationSelectorEvent.SelectVideoPreset(VideoCompressionPreset.LOW))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - SelectVideoPreset won't select the preset if it won't allow to upload the video`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            mediaExtractorFactory = FakeVideoMetadataExtractorFactory(
                FakeVideoMetadataExtractor(
                    sizeResult = Result.success(Size(10_000, 10_000)),
                    duration = Result.success(10.minutes)
                )
            ),
        )
        presenter.test {
            // Skip loading and loaded states
            val eventSink = awaitItem().eventSink
            skipItems(1)

            // No video results could be uploaded
            awaitItem().run {
                val videoSizeEstimations = videoSizeEstimations.dataOrNull()
                assertThat(videoSizeEstimations).isNotNull()
                assertThat(videoSizeEstimations!!.none { it.canUpload }).isTrue()
            }

            eventSink(MediaOptimizationSelectorEvent.SelectVideoPreset(VideoCompressionPreset.HIGH))

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `present - SelectImageOptimization sets the new value`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            localMedia = aLocalMedia(mockMediaUrl, anImageMediaInfo()),
        )
        presenter.test {
            // Skip loading state
            val eventSink = awaitItem().eventSink

            assertThat(awaitItem().isImageOptimizationEnabled).isTrue()

            eventSink(MediaOptimizationSelectorEvent.SelectImageOptimization(false))

            assertThat(awaitItem().isImageOptimizationEnabled).isFalse()
        }
    }

    @Test
    fun `present - max upload size will default to 100MB if we can't get it`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            maxUploadSizeProvider = MaxUploadSizeProvider { Result.failure(AN_EXCEPTION) }
        )
        presenter.test {
            // Skip loading and loaded state
            skipItems(1)
            assertThat(awaitItem().maxUploadSize.dataOrNull()).isEqualTo(1024 * 1024 * 100)
        }
    }

    @Test
    fun `present - with feature flag disabled won't display the media quality selector views`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.SelectableMediaQuality.key to false)),
        )
        presenter.test {
            // Skip loading and loaded state
            skipItems(1)
            assertThat(awaitItem().displayMediaSelectorViews).isFalse()
        }
    }

    @Test
    fun `present - sendAsFile hides selector views and disables image compression for images`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            localMedia = aLocalMedia(mockMediaUrl, anImageMediaInfo()),
            // Even with the feature flag on, sendAsFile must hide the selector.
            featureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.SelectableMediaQuality.key to true)),
            // And it must override the user's "optimize images" preference.
            mediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
            sendAsFile = true,
        )
        presenter.test {
            // Initial loading state
            skipItems(1)
            awaitItem().run {
                assertThat(displayMediaSelectorViews).isFalse()
                assertThat(isImageOptimizationEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - sendAsFile picks HIGH video preset when the video fits the upload limit`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            // Plenty of room: even HIGH preset will fit.
            maxUploadSizeProvider = MaxUploadSizeProvider { Result.success(Long.MAX_VALUE) },
            mediaExtractorFactory = FakeVideoMetadataExtractorFactory(
                FakeVideoMetadataExtractor(
                    sizeResult = Result.success(Size(1920, 1080)),
                    duration = Result.success(10.minutes)
                )
            ),
            sendAsFile = true,
        )
        presenter.test {
            // Initial loading state, then the one with size estimations loaded.
            skipItems(1)
            awaitItem().run {
                assertThat(displayMediaSelectorViews).isFalse()
                assertThat(selectedVideoPreset).isEqualTo(VideoCompressionPreset.HIGH)
            }
        }
    }

    @Test
    fun `present - sendAsFile picks lower video preset when HIGH exceeds the upload limit`() = runTest {
        val presenter = createDefaultMediaOptimizationSelectorPresenter(
            maxUploadSizeProvider = MaxUploadSizeProvider { Result.success(250_000_000L) },
            mediaExtractorFactory = FakeVideoMetadataExtractorFactory(
                FakeVideoMetadataExtractor(
                    sizeResult = Result.success(Size(1920, 1080)),
                    duration = Result.success(10.minutes)
                )
            ),
            sendAsFile = true,
        )
        presenter.test {
            // Initial loading state, then the one with size estimations loaded.
            skipItems(1)
            awaitItem().run {
                assertThat(displayMediaSelectorViews).isFalse()
                assertThat(selectedVideoPreset).isEqualTo(VideoCompressionPreset.STANDARD)
            }
        }
    }

    private fun createDefaultMediaOptimizationSelectorPresenter(
        localMedia: LocalMedia = aLocalMedia(mockMediaUrl, aVideoMediaInfo()),
        maxUploadSizeProvider: MaxUploadSizeProvider = MaxUploadSizeProvider { Result.success(1_000L) },
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.SelectableMediaQuality.key to true)),
        mediaExtractorFactory: FakeVideoMetadataExtractorFactory = FakeVideoMetadataExtractorFactory(),
        mediaOptimizationConfigProvider: FakeMediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
        videoCompressionPresetSelector: VideoCompressionPresetSelector = VideoCompressionPresetSelector(),
        sendAsFile: Boolean = false,
    ): DefaultMediaOptimizationSelectorPresenter {
        return DefaultMediaOptimizationSelectorPresenter(
            localMedia = localMedia,
            sendAsFile = sendAsFile,
            maxUploadSizeProvider = maxUploadSizeProvider,
            featureFlagService = featureFlagService,
            mediaExtractorFactory = mediaExtractorFactory,
            mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
            videoCompressionPresetSelector = videoCompressionPresetSelector,
        )
    }
}
