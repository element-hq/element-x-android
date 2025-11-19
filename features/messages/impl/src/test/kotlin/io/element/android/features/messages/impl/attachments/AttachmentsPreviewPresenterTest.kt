/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.messages.impl.attachments

import android.net.Uri
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewEvents
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewPresenter
import io.element.android.features.messages.impl.attachments.preview.OnDoneListener
import io.element.android.features.messages.impl.attachments.preview.SendActionState
import io.element.android.features.messages.impl.attachments.video.MediaOptimizationSelectorState
import io.element.android.features.messages.impl.attachments.video.VideoUploadEstimation
import io.element.android.features.messages.impl.fixtures.aMediaAttachment
import io.element.android.features.messages.test.attachments.video.FakeMediaOptimizationSelectorPresenterFactory
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.A_CAPTION
import io.element.android.libraries.matrix.test.media.FakeMediaUploadHandler
import io.element.android.libraries.matrix.test.permalink.FakePermalinkBuilder
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaupload.api.MediaOptimizationConfig
import io.element.android.libraries.mediaupload.api.MediaPreProcessor
import io.element.android.libraries.mediaupload.api.MediaSenderFactory
import io.element.android.libraries.mediaupload.api.MediaUploadInfo
import io.element.android.libraries.mediaupload.impl.DefaultMediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.test.viewer.aLocalMedia
import io.element.android.libraries.preferences.api.store.VideoCompressionPreset
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.awaitLastSequentialItem
import io.element.android.tests.testutils.consumeItemsUntilPredicate
import io.element.android.tests.testutils.fake.FakeTemporaryUriDeleter
import io.element.android.tests.testutils.lambda.any
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@Suppress("LargeClass")
@RunWith(RobolectricTestRunner::class)
class AttachmentsPreviewPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUrl: Uri = mockk("localMediaUri") {
        every { path } returns "/path/to/media"
    }

    @Test
    fun `present - initial state`() = runTest {
        createAttachmentsPreviewPresenter().test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
        }
    }

    @Test
    fun `present - send media success scenario`() = runTest {
        val sendFileResult =
            lambdaRecorder<File, FileInfo, String?, String?, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendFileLambda = sendFileResult
            },
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val mediaPreProcessor = FakeMediaPreProcessor()
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = true))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.ReadyToUpload(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendFileResult.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
            assertThat(mediaPreProcessor.cleanUpCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - send media after pre-processing success scenario`() = runTest {
        val sendFileResult =
            lambdaRecorder<File, FileInfo, String?, String?, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendFileLambda = sendFileResult
            },
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val mediaPreProcessor = FakeMediaPreProcessor(processLatch)
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            // Pre-processing finishes
            processLatch.complete(Unit)
            advanceUntilIdle()
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.ReadyToUpload(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendFileResult.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
            assertThat(mediaPreProcessor.cleanUpCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - send media before pre-processing success scenario`() = runTest {
        val sendFileResult =
            lambdaRecorder<File, FileInfo, String?, String?, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendFileLambda = sendFileResult
            },
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val mediaPreProcessor = FakeMediaPreProcessor(processLatch)
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            // Pre-processing finishes
            processLatch.complete(Unit)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = true))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.ReadyToUpload(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendFileResult.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
            assertThat(mediaPreProcessor.cleanUpCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - send media with pre-processing failure after user sends media`() = runTest {
        val room = FakeJoinedRoom()
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = FakeMediaPreProcessor().apply {
                givenResult(Result.failure(Exception()))
            },
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            // Pre-processing finishes
            processLatch.complete(Unit)
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Failure::class.java)
        }
    }

    @Test
    fun `present - send media with pre-processing failure before user sends media`() = runTest {
        val room = FakeJoinedRoom()
        val onDoneListener = lambdaRecorder<Unit> { }
        val processLatch = CompletableDeferred<Unit>()
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = FakeMediaPreProcessor().apply {
                givenResult(Result.failure(Exception()))
            },
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            // Pre-processing finishes
            processLatch.complete(Unit)
            advanceUntilIdle()
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Failure::class.java)
        }
    }

    @Test
    fun `present - cancel scenario`() = runTest {
        val onDoneListener = lambdaRecorder<Unit> { }
        val deleteCallback = lambdaRecorder<Uri?, Unit> {}
        val mediaPreProcessor = FakeMediaPreProcessor()
        val presenter = createAttachmentsPreviewPresenter(
            mediaPreProcessor = mediaPreProcessor,
            temporaryUriDeleter = FakeTemporaryUriDeleter(deleteCallback),
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.CancelAndDismiss)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            deleteCallback.assertions().isCalledOnce()
            onDoneListener.assertions().isCalledOnce()
            assertThat(mediaPreProcessor.cleanUpCallCount).isEqualTo(1)
        }
    }

    @Test
    fun `present - send image with caption success scenario`() = runTest {
        val sendImageResult =
            lambdaRecorder { _: File, _: File?, _: ImageInfo, _: String?, _: String?, _: EventId? ->
                Result.success(FakeMediaUploadHandler())
            }
        val mediaPreProcessor = FakeMediaPreProcessor().apply {
            givenImageResult()
        }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendImageLambda = sendImageResult
            },
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.textEditorState.setMarkdown(A_CAPTION)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Sending.ReadyToUpload::class.java)
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Sending.Uploading::class.java)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendImageResult.assertions().isCalledOnce().with(
                any(),
                any(),
                any(),
                value(A_CAPTION),
                any(),
                any(),
            )
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send video with caption success scenario`() = runTest {
        val sendVideoResult =
            lambdaRecorder { _: File, _: File?, _: VideoInfo, _: String?, _: String?, _: EventId? ->
                Result.success(FakeMediaUploadHandler())
            }
        val mediaPreProcessor = FakeMediaPreProcessor().apply {
            givenVideoResult()
        }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendVideoLambda = sendVideoResult
            },
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.textEditorState.setMarkdown(A_CAPTION)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Sending.ReadyToUpload::class.java)
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Sending.Uploading::class.java)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendVideoResult.assertions().isCalledOnce().with(
                any(),
                any(),
                any(),
                value(A_CAPTION),
                any(),
                any(),
            )
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send audio with caption success scenario`() = runTest {
        val sendAudioResult =
            lambdaRecorder<File, AudioInfo, String?, String?, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.success(FakeMediaUploadHandler())
            }
        val mediaPreProcessor = FakeMediaPreProcessor().apply {
            givenAudioResult()
        }
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendAudioLambda = sendAudioResult
            },
        )
        val onDoneListener = lambdaRecorder<Unit> { }
        val presenter = createAttachmentsPreviewPresenter(
            room = room,
            mediaPreProcessor = mediaPreProcessor,
            onDoneListener = { onDoneListener() },
        )
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.textEditorState.setMarkdown(A_CAPTION)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Sending.ReadyToUpload::class.java)
            assertThat(awaitItem().sendActionState).isInstanceOf(SendActionState.Sending.Uploading::class.java)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Done)
            sendAudioResult.assertions().isCalledOnce().with(
                any(),
                any(),
                value(A_CAPTION),
                any(),
                any(),
            )
            onDoneListener.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - send media failure scenario`() = runTest {
        val failure = MediaPreProcessor.Failure(null)
        val sendFileResult =
            lambdaRecorder<File, FileInfo, String?, String?, EventId?, Result<FakeMediaUploadHandler>> { _, _, _, _, _ ->
                Result.failure(failure)
            }
        val onDoneListenerResult = lambdaRecorder<Unit> {}
        val room = FakeJoinedRoom(
            liveTimeline = FakeTimeline().apply {
                sendFileLambda = sendFileResult
            },
        )
        val presenter = createAttachmentsPreviewPresenter(room = room, onDoneListener = onDoneListenerResult)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.ReadyToUpload(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(mediaUploadInfo))

            // Check that the onDoneListener is called so the screen would be dismissed
            onDoneListenerResult.assertions().isCalledOnce()

            val failureState = awaitItem()
            assertThat(failureState.sendActionState).isEqualTo(SendActionState.Failure(failure, mediaUploadInfo))
            sendFileResult.assertions().isCalledOnce()
            failureState.eventSink(AttachmentsPreviewEvents.CancelAndClearSendState)
            val clearedState = awaitLastSequentialItem()
            assertThat(clearedState.sendActionState).isEqualTo(SendActionState.Sending.ReadyToUpload(mediaUploadInfo))
        }
    }

    @Test
    fun `present - dismissing the progress dialog stops media upload`() = runTest {
        val onDoneListenerResult = lambdaRecorder<Unit> {}
        val presenter = createAttachmentsPreviewPresenter(
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline().apply {
                    sendFileLambda = { _, _, _, _, _ ->
                        Result.success(FakeMediaUploadHandler())
                    }
                }
            ),
            onDoneListener = onDoneListenerResult,
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.sendActionState).isEqualTo(SendActionState.Idle)
            initialState.eventSink(AttachmentsPreviewEvents.SendAttachment)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Processing(displayProgress = false))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.ReadyToUpload(mediaUploadInfo))
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.Uploading(mediaUploadInfo))
            initialState.eventSink(AttachmentsPreviewEvents.CancelAndClearSendState)
            assertThat(awaitItem().sendActionState).isEqualTo(SendActionState.Sending.ReadyToUpload(mediaUploadInfo))
            // The sending is cancelled and the state is kept at ReadyToUpload
            ensureAllEventsConsumed()

            // Check that the onDoneListener is called so the screen would be dismissed
            onDoneListenerResult.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - file too large will display error`() = runTest {
        val onDoneListenerResult = lambdaRecorder<Unit> {}

        val localMedia = aLocalMedia(uri = Uri.EMPTY, mediaInfo = anApkMediaInfo())
        val maxUploadSize = 999L // Set a max upload size smaller than the file size

        val presenter = createAttachmentsPreviewPresenter(
            localMedia = localMedia,
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline().apply {
                    sendFileLambda = { _, _, _, _, _ ->
                        Result.success(FakeMediaUploadHandler())
                    }
                }
            ),
            onDoneListener = onDoneListenerResult,
            mediaOptimizationSelectorPresenterFactory = FakeMediaOptimizationSelectorPresenterFactory {
                MediaOptimizationSelectorState(
                    // Set a max upload size smaller than the file size
                    maxUploadSize = AsyncData.Success(maxUploadSize),
                    videoSizeEstimations = AsyncData.Uninitialized,
                    isImageOptimizationEnabled = null,
                    selectedVideoPreset = null,
                    displayMediaSelectorViews = false,
                    displayVideoPresetSelectorDialog = false,
                    eventSink = {},
                )
            }
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(localMedia.info.fileSize).isGreaterThan(maxUploadSize)

            consumeItemsUntilPredicate { it.mediaOptimizationSelectorState.maxUploadSize.isSuccess() }

            assertThat(awaitItem().displayFileTooLargeError).isTrue()
        }
    }

    @Test
    fun `present - video size estimations too large will display error`() = runTest {
        val onDoneListenerResult = lambdaRecorder<Unit> {}

        val localMedia = aLocalMedia(uri = Uri.EMPTY, mediaInfo = aVideoMediaInfo())

        val presenter = createAttachmentsPreviewPresenter(
            localMedia = localMedia,
            room = FakeJoinedRoom(
                liveTimeline = FakeTimeline().apply {
                    sendFileLambda = { _, _, _, _, _ ->
                        Result.success(FakeMediaUploadHandler())
                    }
                }
            ),
            onDoneListener = onDoneListenerResult,
            mediaOptimizationSelectorPresenterFactory = FakeMediaOptimizationSelectorPresenterFactory {
                MediaOptimizationSelectorState(
                    // Set a max upload size smaller than the file size
                    maxUploadSize = AsyncData.Success(Long.MAX_VALUE),
                    videoSizeEstimations = AsyncData.Success(
                        persistentListOf(
                            VideoUploadEstimation(
                                preset = VideoCompressionPreset.LOW,
                                // The important field is canUpload, it will normally be based on the sizeInBytes
                                canUpload = false,
                                sizeInBytes = 0L,
                            ),
                            VideoUploadEstimation(
                                preset = VideoCompressionPreset.STANDARD,
                                canUpload = false,
                                sizeInBytes = 0L,
                            ),
                            VideoUploadEstimation(
                                preset = VideoCompressionPreset.HIGH,
                                canUpload = false,
                                sizeInBytes = 0L,
                            ),
                        )
                    ),
                    isImageOptimizationEnabled = null,
                    selectedVideoPreset = null,
                    displayMediaSelectorViews = false,
                    displayVideoPresetSelectorDialog = false,
                    eventSink = {},
                )
            }
        )

        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            consumeItemsUntilPredicate {
                it.mediaOptimizationSelectorState.maxUploadSize.isSuccess() &&
                    it.mediaOptimizationSelectorState.videoSizeEstimations.dataOrNull()?.isNotEmpty() == true
            }

            assertThat(awaitItem().displayFileTooLargeError).isTrue()
        }
    }

    private fun TestScope.createAttachmentsPreviewPresenter(
        localMedia: LocalMedia = aLocalMedia(
            uri = mockMediaUrl,
        ),
        room: JoinedRoom = FakeJoinedRoom(),
        timelineMode: Timeline.Mode = Timeline.Mode.Live,
        permalinkBuilder: PermalinkBuilder = FakePermalinkBuilder(),
        mediaPreProcessor: MediaPreProcessor = FakeMediaPreProcessor(),
        temporaryUriDeleter: TemporaryUriDeleter = FakeTemporaryUriDeleter(),
        onDoneListener: OnDoneListener = OnDoneListener { lambdaError() },
        displayMediaQualitySelectorViews: Boolean = false,
        mediaOptimizationSelectorPresenterFactory: FakeMediaOptimizationSelectorPresenterFactory = FakeMediaOptimizationSelectorPresenterFactory(
            fakePresenter = {
                MediaOptimizationSelectorState(
                    maxUploadSize = AsyncData.Uninitialized,
                    videoSizeEstimations = AsyncData.Uninitialized,
                    isImageOptimizationEnabled = null,
                    selectedVideoPreset = null,
                    displayMediaSelectorViews = displayMediaQualitySelectorViews,
                    displayVideoPresetSelectorDialog = false,
                    eventSink = {},
                )
            }
        ),
    ): AttachmentsPreviewPresenter {
        return AttachmentsPreviewPresenter(
            attachment = aMediaAttachment(localMedia),
            onDoneListener = onDoneListener,
            mediaSenderFactory = MediaSenderFactory { timelineMode ->
                DefaultMediaSender(
                    preProcessor = mediaPreProcessor,
                    room = room,
                    timelineMode = timelineMode,
                    mediaOptimizationConfigProvider = {
                        MediaOptimizationConfig(compressImages = true, videoCompressionPreset = VideoCompressionPreset.STANDARD)
                    }
                )
            },
            permalinkBuilder = permalinkBuilder,
            temporaryUriDeleter = temporaryUriDeleter,
            sessionCoroutineScope = this,
            dispatchers = testCoroutineDispatchers(),
            mediaOptimizationSelectorPresenterFactory = mediaOptimizationSelectorPresenterFactory,
            timelineMode = timelineMode,
            inReplyToEventId = null,
        )
    }

    private val mediaUploadInfo = MediaUploadInfo.AnyFile(
        File("test"),
        FileInfo(
            mimetype = MimeTypes.Any,
            size = 999L,
            thumbnailInfo = null,
            thumbnailSource = null,
        )
    )
}
