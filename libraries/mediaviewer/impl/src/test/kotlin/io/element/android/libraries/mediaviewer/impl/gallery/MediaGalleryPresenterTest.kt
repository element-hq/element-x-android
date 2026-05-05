/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.libraries.mediaviewer.impl.gallery

import android.net.Uri
import app.cash.turbine.ReceiveTurbine
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
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
import io.element.android.libraries.matrix.test.room.powerlevels.FakeRoomPermissions
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.datasource.FakeMediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.model.aMediaItemImage
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
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
        val configureLambda = lambdaRecorder<Unit> { }
        val startLambda = lambdaRecorder<Unit> { }
        val presenter = createMediaGalleryPresenter(
            localMediaActions = FakeLocalMediaActions(
                configureResult = configureLambda,
            ),
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
        configureLambda.assertions().isCalledOnce()
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
            initialState.eventSink(MediaGalleryEvent.ChangeMode(MediaGalleryMode.Files))
            val state = awaitItem()
            assertThat(state.mode).isEqualTo(MediaGalleryMode.Files)
            state.eventSink(MediaGalleryEvent.ChangeMode(MediaGalleryMode.Images))
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
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = canDeleteOwn
                    ),
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
            initialState.eventSink(MediaGalleryEvent.OpenInfo(item))
            val state = awaitItem()
            assertThat(state.mediaBottomSheetState).isEqualTo(
                MediaBottomSheetState.Details(
                    eventId = AN_EVENT_ID,
                    canDelete = canDeleteOwn,
                    mediaInfo = item.mediaInfo,
                    thumbnailSource = item.mediaSource,
                )
            )
            // Close the bottom sheet
            state.eventSink(MediaGalleryEvent.CloseBottomSheet)
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
                    roomPermissions = FakeRoomPermissions(
                        canRedactOther = canDeleteOther
                    ),
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
            initialState.eventSink(MediaGalleryEvent.OpenInfo(item))
            val state = awaitItem()
            assertThat(state.mediaBottomSheetState).isEqualTo(
                MediaBottomSheetState.Details(
                    eventId = AN_EVENT_ID,
                    canDelete = canDeleteOther,
                    mediaInfo = item.mediaInfo,
                    thumbnailSource = item.mediaSource,
                )
            )
            // Close the bottom sheet
            state.eventSink(MediaGalleryEvent.CloseBottomSheet)
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
            initialState.eventSink(MediaGalleryEvent.ConfirmDelete(AN_EVENT_ID, item.mediaInfo, item.thumbnailSource))
            val deleteState = awaitItem()
            assertThat(deleteState.mediaBottomSheetState).isEqualTo(
                MediaBottomSheetState.DeleteConfirmation(
                    eventId = AN_EVENT_ID,
                    mediaInfo = item.mediaInfo,
                    thumbnailSource = item.thumbnailSource,
                )
            )
            // Close the bottom sheet
            deleteState.eventSink(MediaGalleryEvent.CloseBottomSheet)
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
            initialState.eventSink(MediaGalleryEvent.Delete(AN_EVENT_ID))
            deleteItemLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - share item - item not found`() = runTest {
        val presenter = createMediaGalleryPresenter()
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvent.Share(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - share item - item found`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        mediaGalleryDataSource.emitGroupedMediaItems(
            AsyncData.Success(
                aGroupedMediaItems(
                    imageAndVideoItems = listOf(aMediaItemImage(eventId = AN_EVENT_ID)),
                    fileItems = emptyList(),
                )
            )
        )
        val presenter = createMediaGalleryPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvent.Share(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - share item - item found - download error`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        mediaGalleryDataSource.emitGroupedMediaItems(
            AsyncData.Success(
                aGroupedMediaItems(
                    imageAndVideoItems = listOf(aMediaItemImage(eventId = AN_EVENT_ID)),
                    fileItems = emptyList(),
                )
            )
        )
        val presenter = createMediaGalleryPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
            matrixMediaLoader = FakeMatrixMediaLoader().apply { shouldFail = true },
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvent.Share(AN_EVENT_ID))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.snackbarMessage).isInstanceOf(SnackbarMessage::class.java)
        }
    }

    @Test
    fun `present - save on disk - item not found`() = runTest {
        val presenter = createMediaGalleryPresenter()
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvent.SaveOnDisk(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - save on disk - item found`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val saveOnDiskResult = lambdaRecorder<LocalMedia, Result<Unit>> { _ -> Result.success(Unit) }
        val media = aMediaItemImage(eventId = AN_EVENT_ID)
        mediaGalleryDataSource.emitGroupedMediaItems(
            AsyncData.Success(
                aGroupedMediaItems(
                    imageAndVideoItems = listOf(media),
                    fileItems = emptyList(),
                )
            )
        )
        val presenter = createMediaGalleryPresenter(
            localMediaActions = FakeLocalMediaActions(
                saveOnDiskResult = saveOnDiskResult,
            ),
            mediaGalleryDataSource = mediaGalleryDataSource,
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvent.SaveOnDisk(AN_EVENT_ID))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.snackbarMessage?.messageResId).isEqualTo(CommonStrings.common_file_saved_on_disk_android)
            saveOnDiskResult.assertions().isCalledOnce().with(
                value(
                    LocalMedia(
                        uri = mockMediaUri,
                        info = media.mediaInfo,
                    )
                )
            )
        }
    }

    @Test
    fun `present - open with closes the bottom sheet and invokes the navigator`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        val openWithResult = lambdaRecorder<LocalMedia, Result<Unit>> { _ -> Result.success(Unit) }
        val item = aMediaItemImage(
            eventId = AN_EVENT_ID,
            senderId = A_USER_ID,
        )
        mediaGalleryDataSource.emitGroupedMediaItems(
            AsyncData.Success(
                aGroupedMediaItems(
                    imageAndVideoItems = listOf(item),
                    fileItems = emptyList(),
                )
            )
        )
        val presenter = createMediaGalleryPresenter(
            localMediaActions = FakeLocalMediaActions(
                openResult = openWithResult,
            ),
            mediaGalleryDataSource = mediaGalleryDataSource,
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(FakeTimeline()) },
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                ),
            ),
        )
        presenter.test {
            skipItems(1)
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvent.OpenInfo(item))
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.Details::class.java)
            withBottomSheetState.eventSink(MediaGalleryEvent.OpenWith(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            advanceUntilIdle()
            openWithResult.assertions().isCalledOnce().with(
                value(
                    LocalMedia(
                        uri = mockMediaUri,
                        info = item.mediaInfo,
                    )
                )
            )
        }
    }

    @Test
    fun `present - save on disk - item found - download error`() = runTest {
        val mediaGalleryDataSource = FakeMediaGalleryDataSource(
            startLambda = { },
        )
        mediaGalleryDataSource.emitGroupedMediaItems(
            AsyncData.Success(
                aGroupedMediaItems(
                    imageAndVideoItems = listOf(aMediaItemImage(eventId = AN_EVENT_ID)),
                    fileItems = emptyList(),
                )
            )
        )
        val presenter = createMediaGalleryPresenter(
            mediaGalleryDataSource = mediaGalleryDataSource,
            matrixMediaLoader = FakeMatrixMediaLoader().apply { shouldFail = true },
        )
        presenter.test {
            val initialState = awaitFirstItem()
            initialState.eventSink(MediaGalleryEvent.SaveOnDisk(AN_EVENT_ID))
            skipItems(1)
            val finalState = awaitItem()
            assertThat(finalState.snackbarMessage).isInstanceOf(SnackbarMessage::class.java)
        }
    }

    @Test
    fun `present - view in timeline closes the bottom sheet and invokes the navigator`() = runTest {
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaGalleryNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaGalleryPresenter(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(FakeTimeline()) },
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                ),
            ),
            navigator = navigator,
        )
        presenter.test {
            val initialState = awaitFirstItem()
            val item = aMediaItemImage(
                eventId = AN_EVENT_ID,
                senderId = A_USER_ID,
            )
            initialState.eventSink(MediaGalleryEvent.OpenInfo(item))
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.Details::class.java)
            withBottomSheetState.eventSink(MediaGalleryEvent.ViewInTimeline(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            onViewInTimelineClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    @Test
    fun `present - forward closes the bottom sheet and invokes the navigator`() = runTest {
        val onForwardClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaGalleryNavigator(
            onForwardClickLambda = onForwardClickLambda,
        )
        val presenter = createMediaGalleryPresenter(
            room = FakeJoinedRoom(
                createTimelineResult = { Result.success(FakeTimeline()) },
                baseRoom = FakeBaseRoom(
                    roomPermissions = FakeRoomPermissions(
                        canRedactOwn = true
                    ),
                ),
            ),
            navigator = navigator,
        )
        presenter.test {
            val initialState = awaitFirstItem()
            val item = aMediaItemImage(
                eventId = AN_EVENT_ID,
                senderId = A_USER_ID,
            )
            initialState.eventSink(MediaGalleryEvent.OpenInfo(item))
            val withBottomSheetState = awaitItem()
            assertThat(withBottomSheetState.mediaBottomSheetState).isInstanceOf(MediaBottomSheetState.Details::class.java)
            withBottomSheetState.eventSink(MediaGalleryEvent.Forward(AN_EVENT_ID))
            val finalState = awaitItem()
            assertThat(finalState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            onForwardClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
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
            initialState.eventSink(MediaGalleryEvent.LoadMore(Timeline.PaginationDirection.BACKWARDS))
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
