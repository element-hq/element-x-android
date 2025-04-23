/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import android.net.Uri
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.aRoomInfo
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.impl.datasource.FakeMediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MediaGalleryPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val mockMediaUri: Uri = mockk("localMediaUri")
    private val localMediaFactory = FakeLocalMediaFactory(mockMediaUri)

    @Test
    fun `present - initial state`() = runTest {
        val startLambda = lambdaRecorder<Unit> { }
        val presenter = createMediaGalleryPresenter(
            mediaGalleryDataSource = FakeMediaGalleryDataSource(
                startLambda = startLambda,
            ),
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(initialRoomInfo = aRoomInfo(name = A_ROOM_NAME)),
                createTimelineResult = { Result.success(FakeTimeline()) },
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.mode).isEqualTo(MediaGalleryMode.Images)
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            assertThat(initialState.roomName).isEqualTo(A_ROOM_NAME)
            assertThat(initialState.groupedMediaItems.isUninitialized()).isTrue()
            assertThat(initialState.snackbarMessage).isNull()
        }
        startLambda.assertions().isCalledOnce()
    }

    @Test
    fun `present - change mode`() = runTest {
        val presenter = createMediaGalleryPresenter(
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(initialRoomInfo = aRoomInfo(name = A_ROOM_NAME)),
                createTimelineResult = { Result.success(FakeTimeline()) },
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.mode).isEqualTo(MediaGalleryMode.Images)
            initialState.eventSink(MediaGalleryEvents.ChangeMode(MediaGalleryMode.Files))
            val state = awaitItem()
            assertThat(state.mode).isEqualTo(MediaGalleryMode.Files)
            state.eventSink(MediaGalleryEvents.ChangeMode(MediaGalleryMode.Images))
            val imageModeState = awaitItem()
            assertThat(imageModeState.mode).isEqualTo(MediaGalleryMode.Images)
        }
    }

    @Test
    fun `present - bottom sheet state - own message and can delete own`() = runTest {
        `present - bottom sheet state - own message`(canDeleteOwn = true)
    }

    @Test
    fun `present - bottom sheet state - own message and cannot delete own`() = runTest {
        `present - bottom sheet state - own message`(canDeleteOwn = false)
    }

    private suspend fun `present - bottom sheet state - own message`(canDeleteOwn: Boolean) {
        val presenter = createMediaGalleryPresenter(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(FakeTimeline()) },
                baseRoom = FakeBaseRoom(
                    sessionId = A_USER_ID,
                    initialRoomInfo = aRoomInfo(name = A_ROOM_NAME),
                    canRedactOwnResult = { Result.success(canDeleteOwn) }
                ),
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            val item = aMediaItemImage(
                eventId = AN_EVENT_ID,
                senderId = A_USER_ID,
            )
            initialState.eventSink(MediaGalleryEvents.OpenInfo(item))
            val state = awaitItem()
            assertThat(state.mediaBottomSheetState).isEqualTo(
                MediaBottomSheetState.MediaDetailsBottomSheetState(
                    eventId = AN_EVENT_ID,
                    canDelete = canDeleteOwn,
                    mediaInfo = item.mediaInfo,
                    thumbnailSource = item.mediaSource,
                )
            )
            // Close the bottom sheet
            state.eventSink(MediaGalleryEvents.CloseBottomSheet)
            val closedState = awaitItem()
            assertThat(closedState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - bottom sheet state - other message and can delete other`() = runTest {
        `present - bottom sheet state - other message`(canDeleteOther = true)
    }

    @Test
    fun `present - bottom sheet state - other message and cannot delete other`() = runTest {
        `present - bottom sheet state - other message`(canDeleteOther = false)
    }

    private suspend fun `present - bottom sheet state - other message`(canDeleteOther: Boolean) {
        val presenter = createMediaGalleryPresenter(
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(
                sessionId = A_USER_ID,
                initialRoomInfo = aRoomInfo(name = A_ROOM_NAME),
                    canRedactOtherResult = { Result.success(canDeleteOther) },
                ),
                createTimelineResult = { Result.success(FakeTimeline()) }
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            val item = aMediaItemImage(
                eventId = AN_EVENT_ID,
                senderId = A_USER_ID_2,
            )
            initialState.eventSink(MediaGalleryEvents.OpenInfo(item))
            val state = awaitItem()
            assertThat(state.mediaBottomSheetState).isEqualTo(
                MediaBottomSheetState.MediaDetailsBottomSheetState(
                    eventId = AN_EVENT_ID,
                    canDelete = canDeleteOther,
                    mediaInfo = item.mediaInfo,
                    thumbnailSource = item.mediaSource,
                )
            )
            // Close the bottom sheet
            state.eventSink(MediaGalleryEvents.CloseBottomSheet)
            val closedState = awaitItem()
            assertThat(closedState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - delete bottom sheet`() = runTest {
        val presenter = createMediaGalleryPresenter(
            room = FakeJoinedRoom(
                baseRoom = FakeBaseRoom(initialRoomInfo = aRoomInfo(name = A_ROOM_NAME)),
                createTimelineResult = { Result.success(FakeTimeline()) },
            )
        )
        presenter.test {
            val initialState = awaitFirstItem()
            // Delete bottom sheet
            val item = aMediaItemImage()
            initialState.eventSink(MediaGalleryEvents.ConfirmDelete(AN_EVENT_ID, item.mediaInfo, item.thumbnailSource))
            val deleteState = awaitItem()
            assertThat(deleteState.mediaBottomSheetState).isEqualTo(
                MediaBottomSheetState.MediaDeleteConfirmationState(
                    eventId = AN_EVENT_ID,
                    mediaInfo = item.mediaInfo,
                    thumbnailSource = item.thumbnailSource,
                )
            )
            // Close the bottom sheet
            deleteState.eventSink(MediaGalleryEvents.CloseBottomSheet)
            val deleteClosedState = awaitItem()
            assertThat(deleteClosedState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
        }
    }

    @Test
    fun `present - delete item`() = runTest {
        val deleteItemLambda = lambdaRecorder<EventId, Unit> { }
        val presenter = createMediaGalleryPresenter(
            mediaGalleryDataSource = FakeMediaGalleryDataSource(
                startLambda = { },
                deleteItemLambda = deleteItemLambda,
            ),
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvents.Delete(AN_EVENT_ID))
            deleteItemLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - share item`() = runTest {
        val presenter = createMediaGalleryPresenter()
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvents.Share(AN_EVENT_ID))
        }
        // TODO Add more test on this part
    }

    @Test
    fun `present - save on disk`() = runTest {
        val presenter = createMediaGalleryPresenter()
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvents.SaveOnDisk(AN_EVENT_ID))
        }
        // TODO Add more test on this part
    }

    @Test
    fun `present - view in timeline invokes the navigator`() = runTest {
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaGalleryNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaGalleryPresenter(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(FakeTimeline()) },
            ),
            navigator = navigator,
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvents.ViewInTimeline(AN_EVENT_ID))
            onViewInTimelineClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - load more`() = runTest {
        val loadMoreLambda = lambdaRecorder<Timeline.PaginationDirection, Unit> { }
        val presenter = createMediaGalleryPresenter(
            mediaGalleryDataSource = FakeMediaGalleryDataSource(
                startLambda = { },
                loadMoreLambda = loadMoreLambda,
            ),
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvents.LoadMore(Timeline.PaginationDirection.BACKWARDS))
            loadMoreLambda.assertions().isCalledOnce().with(value(Timeline.PaginationDirection.BACKWARDS))
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.awaitFirstItem(): T {
        return awaitItem()
    }

    private fun createMediaGalleryPresenter(
        matrixMediaLoader: FakeMatrixMediaLoader = FakeMatrixMediaLoader(),
        mediaGalleryDataSource: MediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        ),
        localMediaActions: FakeLocalMediaActions = FakeLocalMediaActions(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        navigator: MediaGalleryNavigator = FakeMediaGalleryNavigator(),
        room: JoinedRoom = FakeJoinedRoom(
            liveTimeline = FakeTimeline(),
        ),
    ): MediaGalleryPresenter {
        return MediaGalleryPresenter(
            navigator = navigator,
            room = room,
            mediaGalleryDataSource = mediaGalleryDataSource,
            localMediaFactory = localMediaFactory,
            mediaLoader = matrixMediaLoader,
            localMediaActions = localMediaActions,
            snackbarDispatcher = snackbarDispatcher,
        )
    }
}
