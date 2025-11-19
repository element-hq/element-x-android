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
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.test.attachments.video.FakeVideoMetadataExtractor
import io.element.android.features.messages.test.attachments.video.FakeVideoMetadataExtractorFactory
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.mediaupload.api.MaxUploadSizeProvider
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.libraries.preferences.test.InMemorySessionPreferencesStore
import io.element.android.tests.testutils.WarmUpRule
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
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
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Skip loading and loaded state
            skipItems(1)
            assertThat(awaitItem().displayMediaSelectorViews).isFalse()
        }
    }

    private fun createDefaultMediaOptimizationSelectorPresenter(
        localMedia: LocalMedia = aLocalMedia(mockMediaUrl, aVideoMediaInfo()),
        maxUploadSizeProvider: MaxUploadSizeProvider = MaxUploadSizeProvider { Result.success(1_000L) },
        sessionPreferencesStore: InMemorySessionPreferencesStore = InMemorySessionPreferencesStore(),
        featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(mapOf(FeatureFlags.SelectableMediaQuality.key to true)),
        mediaExtractorFactory: FakeVideoMetadataExtractorFactory = FakeVideoMetadataExtractorFactory(),
    ): DefaultMediaOptimizationSelectorPresenter {
        return DefaultMediaOptimizationSelectorPresenter(
            localMedia = localMedia,
            maxUploadSizeProvider = maxUploadSizeProvider,
            sessionPreferencesStore = sessionPreferencesStore,
            featureFlagService = featureFlagService,
            mediaExtractorFactory = mediaExtractorFactory,
        )
    }
}
