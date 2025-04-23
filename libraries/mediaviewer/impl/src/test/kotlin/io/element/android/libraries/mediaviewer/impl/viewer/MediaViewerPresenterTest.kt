/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.mediaviewer.impl.viewer

import android.net.Uri
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import io.element.android.libraries.matrix.test.media.aMediaSource
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.datasource.FakeMediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemLoadingIndicator
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.services.toolbox.test.systemclock.FakeSystemClock
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private val TESTED_MEDIA_INFO = anApkMediaInfo(
    senderId = A_USER_ID,
)

@Suppress("LargeClass")
class MediaViewerPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUri: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)
    private val aUrl = "aUrl"

    private val anImage = aMediaItemImage(
        mediaSourceUrl = aUrl,
    )
    private val aBackwardLoadingIndicator = aMediaItemLoadingIndicator(
        direction = Timeline.PaginationDirection.BACKWARDS
    )
    private val aForwardLoadingIndicator = aMediaItemLoadingIndicator(
        direction = Timeline.PaginationDirection.FORWARDS
    )

    @Test
    fun `present - initial state null Event`() = runTest {
        val presenter = createMediaViewerPresenter(
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - initial state cannot show info`() = runTest {
        val presenter = createMediaViewerPresenter(
            canShowInfo = false,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isFalse()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - initial state Event`() = runTest {
        val presenter = createMediaViewerPresenter(
            eventId = AN_EVENT_ID,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                canRedactOwnResult = { Result.success(true) },
            )
        )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - initial state Event from other`() = runTest {
        val presenter = createMediaViewerPresenter(
            eventId = AN_EVENT_ID,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                sessionId = A_SESSION_ID_2,
                canRedactOtherResult = { Result.success(false) },
            )
        )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            assertThat(initialState.currentIndex).isEqualTo(0)
            assertThat(initialState.snackbarMessage).isNull()
            assertThat(initialState.canShowInfo).isTrue()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - data source update`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage()
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.listData).isEmpty()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitFirstItem()
            assertThat(updatedState.listData).hasSize(1)
            val item = updatedState.listData.first() as MediaViewerPageData.MediaViewerData
            assertThat(item.eventId).isNull()
            assertThat(item.mediaInfo).isEqualTo(anImage.mediaInfo)
            assertThat(item.mediaSource).isEqualTo(anImage.mediaSource)
            assertThat(item.thumbnailSource).isEqualTo(anImage.thumbnailSource)
            assertThat(item.downloadedMedia.value).isEqualTo(AsyncData.Uninitialized)
        }
    }

    @Test
    fun `present - load media`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.LoadMedia(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - open info`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
            room = FakeJoinedRoom(baseRoom = FakeBaseRoom(
                canRedactOwnResult = { Result.success(true) },
            ))
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.OpenInfo(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
            val withInfoState = awaitItem()
            assertThat(withInfoState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.MediaDetailsBottomSheetState::class.java)
            withInfoState.eventSink(
                MediaViewerEvents.CloseBottomSheet
            )
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - clear loading error`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.ClearLoadingError(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - share`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.Share(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - save on disk`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.SaveOnDisk(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - open with`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.OpenWith(
                    aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
        }
    }

    @Test
    fun `present - delete and cancel`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.ConfirmDelete(
                    eventId = AN_EVENT_ID,
                    data = aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.MediaDeleteConfirmationState::class.java)
            withBottomSheetState.eventSink(
                MediaViewerEvents.CloseBottomSheet
            )
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - delete`() = runTest {
        val redactEventLambda = lambdaRecorder<EventOrTransactionId, String?, Result<Unit>> { _, _ ->
            Result.success(Unit)
        }
        val timeline = FakeTimeline().apply {
            this.redactEventLambda = redactEventLambda
        }
        val onItemDeletedLambda = lambdaRecorder<Unit> { }
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            room = FakeJoinedRoom(
                liveTimeline = timeline,
                baseRoom = FakeBaseRoom(canRedactOwnResult = { Result.success(true) }),
            ),
            mediaGalleryDataSource = mediaGalleryDataSource,
            mediaViewerNavigator = FakeMediaViewerNavigator(
                onItemDeletedLambda = onItemDeletedLambda
            )
        )
        val anImage = aMediaItemImage(
            eventId = AN_EVENT_ID,
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.ConfirmDelete(
                    eventId = AN_EVENT_ID,
                    data = aMediaViewerPageData(
                        mediaSource = MediaSource(aUrl)
                    )
                )
            )
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.MediaDeleteConfirmationState::class.java)
            updatedState.eventSink(
                MediaViewerEvents.Delete(
                    eventId = AN_EVENT_ID,
                )
            )
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            redactEventLambda.assertions()
                .isCalledOnce()
                .with(
                    value(AN_EVENT_ID.toEventOrTransactionId()),
                    value(null),
                )
            onItemDeletedLambda.assertions().isCalledOnce()
        }
    }

    @Test
    fun `present - on navigate to`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        val anImage2 = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage, anImage2),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.OnNavigateTo(1)
            )
            val finalState = awaitItem()
            assertThat(finalState.currentIndex).isEqualTo(1)
        }
    }

    @Test
    fun `present - snackbar displayed when there is no more items forward images and videos`() {
        `present - snackbar displayed when there is no more items forward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode = Timeline.Mode.MEDIA),
            expectedSnackbarResId = R.string.screen_media_details_no_more_media_to_show,
        )
    }

    @Test
    fun `present - snackbar displayed when there is no more items forward files and audio`() {
        `present - snackbar displayed when there is no more items forward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode = Timeline.Mode.MEDIA),
            expectedSnackbarResId = R.string.screen_media_details_no_more_files_to_show,
        )
    }

    private fun `present - snackbar displayed when there is no more items forward`(
        mode: MediaViewerEntryPoint.MediaViewerMode,
        expectedSnackbarResId: Int,
    ) = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mode = mode,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                            fileItems = persistentListOf(),
                        )
                    }
                )
            )
            val updatedState = awaitItem()
            // User navigate to the first item (forward loading indicator)
            updatedState.eventSink(
                MediaViewerEvents.OnNavigateTo(0)
            )
            // data source claims that there is no more items to load forward
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(anImage, aBackwardLoadingIndicator),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(anImage, aBackwardLoadingIndicator),
                            fileItems = persistentListOf(),
                        )
                    }
                )
            )
            skipItems(1)
            val stateWithSnackbar = awaitItem()
            assertThat(stateWithSnackbar.snackbarMessage!!.messageResId).isEqualTo(expectedSnackbarResId)
        }
    }

    @Test
    fun `present - snackbar displayed when there is no more items backward images and videos`() {
        `present - snackbar displayed when there is no more items backward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode = Timeline.Mode.MEDIA),
            expectedSnackbarResId = R.string.screen_media_details_no_more_media_to_show,
        )
    }

    @Test
    fun `present - snackbar displayed when there is no more items backward files and audio`() {
        `present - snackbar displayed when there is no more items backward`(
            mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode = Timeline.Mode.MEDIA),
            expectedSnackbarResId = R.string.screen_media_details_no_more_files_to_show,
        )
    }

    private fun `present - snackbar displayed when there is no more items backward`(
        mode: MediaViewerEntryPoint.MediaViewerMode,
        expectedSnackbarResId: Int,
    ) = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mode = mode,
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                            fileItems = persistentListOf(),
                        )
                    }
                )
            )
            val updatedState = awaitItem()
            // User navigate to the last item (backward loading indicator)
            updatedState.eventSink(
                MediaViewerEvents.OnNavigateTo(2)
            )
            skipItems(1)
            // data source claims that there is no more items to load backward
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    if (mode is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios) {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(),
                            fileItems = persistentListOf(aForwardLoadingIndicator, anImage),
                        )
                    } else {
                        GroupedMediaItems(
                            imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage),
                            fileItems = persistentListOf(),
                        )
                    }
                )
            )
            skipItems(1)
            val stateWithSnackbar = awaitItem()
            assertThat(stateWithSnackbar.snackbarMessage!!.messageResId).isEqualTo(expectedSnackbarResId)
        }
    }

    @Test
    fun `present - no snackbar displayed when there is no more items but not displaying a loading item`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(aForwardLoadingIndicator, anImage, aBackwardLoadingIndicator),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            // User navigate to the media
            updatedState.eventSink(
                MediaViewerEvents.OnNavigateTo(1)
            )
            skipItems(1)
            // data source claims that there is no more items to load at all
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val finalState = awaitItem()
            assertThat(finalState.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val loadMoreLambda = lambdaRecorder<Timeline.PaginationDirection, Unit> { }
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
            loadMoreLambda = loadMoreLambda,
        )
        val presenter = createMediaViewerPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        val anImage = aMediaItemImage(
            mediaSourceUrl = aUrl,
        )
        presenter.test {
            awaitFirstItem()
            mediaGalleryDataSource.emitGroupedMediaItems(
                AsyncData.Success(
                    GroupedMediaItems(
                        imageAndVideoItems = persistentListOf(anImage),
                        fileItems = persistentListOf(),
                    )
                )
            )
            val updatedState = awaitItem()
            updatedState.eventSink(
                MediaViewerEvents.LoadMore(Timeline.PaginationDirection.BACKWARDS)
            )
            loadMoreLambda.assertions().isCalledOnce().with(value(Timeline.PaginationDirection.BACKWARDS))
        }
    }

    @Test
    fun `present - view in timeline hide the bottom sheet and invokes the navigator`() = runTest {
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaViewerNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaViewerPresenter(
            mediaViewerNavigator = navigator,
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(canRedactOwnResult = { Result.success(true) }),
            )
        )
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(MediaViewerEvents.OpenInfo(aMediaViewerPageData()))
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.MediaDetailsBottomSheetState::class.java)
            initialState.eventSink(MediaViewerEvents.ViewInTimeline(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            onViewInTimelineClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        return awaitItem()
    }

    private fun TestScope.createMediaViewerPresenter(
        eventId: EventId? = null,
        mode: MediaViewerEntryPoint.MediaViewerMode = MediaViewerEntryPoint.MediaViewerMode.SingleMedia,
        matrixMediaLoader: FakeMatrixMediaLoader = FakeMatrixMediaLoader(),
        localMediaActions: FakeLocalMediaActions = FakeLocalMediaActions(),
        mediaGalleryDataSource: MediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        ),
        canShowInfo: Boolean = true,
        mediaViewerNavigator: MediaViewerNavigator = FakeMediaViewerNavigator(),
        room: JoinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline(),
        ),
    ): MediaViewerPresenter {
        return MediaViewerPresenter(
            inputs = MediaViewerEntryPoint.Params(
                mode = mode,
                eventId = eventId,
                mediaInfo = TESTED_MEDIA_INFO,
                mediaSource = aMediaSource(),
                thumbnailSource = null,
                canShowInfo = canShowInfo,
            ),
            navigator = mediaViewerNavigator,
            dataSource = MediaViewerDataSource(
                mode = mode,
                dispatcher = testCoroutineDispatchers().computation,
                galleryDataSource = mediaGalleryDataSource,
                mediaLoader = matrixMediaLoader,
                localMediaFactory = localMediaFactory,
                systemClock = FakeSystemClock(),
                pagerKeysHandler = PagerKeysHandler(),
            ),
            room = room,
            localMediaActions = localMediaActions,
        )
    }
}
