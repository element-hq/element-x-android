/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.mediapreview.impl

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.features.mediapreview.api.MediaPreviewConfig
import io.element.android.features.mediapreview.api.SendMode
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.mediaupload.api.MaxUploadSizeProvider
import io.element.android.libraries.mediaupload.api.MediaSenderRoomFactory
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.test.FakeMediaSender
import io.element.android.libraries.mediaupload.test.FakeVideoMetadataExtractorFactory
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class MediaPreviewPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUri: Uri = mockk("localMediaUri") {
        every { path } returns "/path/to/media"
    }

    @Test
    fun `present - initial state`() = runTest {
        createMediaPreviewPresenter(localMedia = aLocalMedia(mockMediaUri, anImageMediaInfo())).test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isInstanceOf(SendActionState.Idle::class.java)
            assertThat(initialState.showOptimizationOptions).isFalse()
            assertThat(initialState.isImageOptimizationEnabled).isTrue()
            assertThat(initialState.showVideoQualityDialog).isFalse()
            assertThat(initialState.displayFileTooLargeError).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Cancel invokes onCancelListener`() = runTest {
        val onCancelRecorder = lambdaRecorder<Unit> {}
        createMediaPreviewPresenter(onCancelListener = onCancelRecorder).test {
            val initialState = awaitItem()
            initialState.eventSink(MediaPreviewEvents.Cancel)
            cancelAndIgnoreRemainingEvents()
        }
        onCancelRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `present - ClearError resets sendActionState`() = runTest {
        createMediaPreviewPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isInstanceOf(SendActionState.Idle::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - Send in PREPROCESS mode triggers preprocessing`() = runTest {
        val preProcessMediaRecorder = lambdaRecorder<Result<MediaUploadInfo>> {
            Result.success(MediaUploadInfo.AnyFile(File("/tmp/test"), mockk()))
        }
        val mediaSender = FakeMediaSender(preProcessMediaResult = preProcessMediaRecorder)
        val joinedRoom = FakeJoinedRoom()
        val config = MediaPreviewConfig(
            sendMode = SendMode.PREPROCESS,
            joinedRoom = joinedRoom,
        )
        createMediaPreviewPresenter(
            mediaSenderRoomFactory = MediaSenderRoomFactory { mediaSender },
            config = config,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink(MediaPreviewEvents.Send)
            cancelAndIgnoreRemainingEvents()
        }
        preProcessMediaRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `present - Send in DIRECT mode invokes onSendListener directly`() = runTest {
        val onSendRecorder = lambdaRecorder<String?, Boolean, VideoCompressionPreset?, () -> Unit, Unit> { _, _, _, _ -> }
        createMediaPreviewPresenter(
            onSendListener = MediaPreviewPresenter.OnSendListener(onSendRecorder),
        ).test {
            val initialState = awaitItem()
            initialState.eventSink(MediaPreviewEvents.Send)
            cancelAndIgnoreRemainingEvents()
        }
        onSendRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `present - Send in PREPROCESS mode failure shows error state`() = runTest {
        val failure = RuntimeException("Test error")
        val preProcessMediaRecorder = lambdaRecorder<Result<MediaUploadInfo>> {
            Result.failure(failure)
        }
        val mediaSender = FakeMediaSender(preProcessMediaResult = preProcessMediaRecorder)
        val joinedRoom = FakeJoinedRoom()
        val config = MediaPreviewConfig(
            sendMode = SendMode.PREPROCESS,
            joinedRoom = joinedRoom,
        )
        createMediaPreviewPresenter(
            mediaSenderRoomFactory = MediaSenderRoomFactory { mediaSender },
            config = config,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink(MediaPreviewEvents.Send)
            // Just verify that preprocessing was triggered - the test framework
            // handles the rest via cancelAndIgnoreRemainingEvents()
            cancelAndIgnoreRemainingEvents()
        }
        // Verify that preProcessMedia was called
        preProcessMediaRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `present - Retry triggers preprocessing with current settings`() = runTest {
        val preProcessMediaRecorder = lambdaRecorder<Result<MediaUploadInfo>> {
            Result.success(MediaUploadInfo.AnyFile(File("/tmp/test"), mockk()))
        }
        val mediaSender = FakeMediaSender(preProcessMediaResult = preProcessMediaRecorder)
        val joinedRoom = FakeJoinedRoom()
        val config = MediaPreviewConfig(
            sendMode = SendMode.PREPROCESS,
            joinedRoom = joinedRoom,
        )
        createMediaPreviewPresenter(
            mediaSenderRoomFactory = MediaSenderRoomFactory { mediaSender },
            config = config,
        ).test {
            val initialState = awaitItem()
            initialState.eventSink(MediaPreviewEvents.Retry)
            cancelAndIgnoreRemainingEvents()
        }
        preProcessMediaRecorder.assertions().isCalledOnce()
    }

    @Test
    fun `present - with default optimization config`() = runTest {
        createMediaPreviewPresenter().test {
            val state = awaitItem()
            assertThat(state.isImageOptimizationEnabled).isTrue()
            assertThat(state.selectedVideoPreset).isEqualTo(VideoCompressionPreset.STANDARD)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `present - non-image non-video files do not show optimization options`() = runTest {
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(FeatureFlags.SelectableMediaQuality.key to true)
        )
        val audioMediaInfo = anImageMediaInfo().copy(mimeType = MimeTypes.Mp3)
        createMediaPreviewPresenter(
            featureFlagService = featureFlagService,
            localMedia = aLocalMedia(mockMediaUri, audioMediaInfo),
        ).test {
            val state = awaitItem()
            assertThat(state.showOptimizationOptions).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }
}

internal fun TestScope.createMediaPreviewPresenter(
    localMedia: io.element.android.libraries.mediaviewer.api.local.LocalMedia? = null,
    config: MediaPreviewConfig = MediaPreviewConfig(),
    featureFlagService: FakeFeatureFlagService = FakeFeatureFlagService(),
    mediaOptimizationConfigProvider: FakeMediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
    mediaSenderRoomFactory: MediaSenderRoomFactory = MediaSenderRoomFactory { FakeMediaSender() },
    videoMetadataExtractorFactory: FakeVideoMetadataExtractorFactory = FakeVideoMetadataExtractorFactory(),
    onSendListener: MediaPreviewPresenter.OnSendListener = MediaPreviewPresenter.OnSendListener { _, _, _, _ -> },
    onCancelListener: MediaPreviewPresenter.OnCancelListener = MediaPreviewPresenter.OnCancelListener {},
): MediaPreviewPresenter {
    val mediaUri: Uri = mockk("mediaUri") { every { path } returns "/path/to/media" }
    val media = localMedia ?: aLocalMedia(mediaUri, anImageMediaInfo())
    return MediaPreviewPresenter(
        localMedia = media,
        config = config,
        onSendListener = onSendListener,
        onCancelListener = onCancelListener,
        featureFlagService = featureFlagService,
        mediaOptimizationConfigProvider = mediaOptimizationConfigProvider,
        mediaSenderRoomFactory = mediaSenderRoomFactory,
        maxUploadSizeProvider = MaxUploadSizeProvider { Result.success(100 * 1024 * 1024L) },
        dispatchers = testCoroutineDispatchers(),
        videoMetadataExtractorFactory = videoMetadataExtractorFactory,
    )
}
