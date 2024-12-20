/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import android.net.Uri
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_ROOM_NAME
import io.element.android.libraries.matrix.test.A_USER_ID
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.libraries.matrix.test.media.FakeMatrixMediaLoader
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.timeline.FakeTimeline
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.gallery.ui.aMediaItemImage
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaActions
import io.element.android.libraries.mediaviewer.test.FakeLocalMediaFactory
import io.element.android.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
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
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaGalleryNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaGalleryPresenter(
            navigator = navigator,
            room = FakeMatrixRoom(
                displayName = A_ROOM_NAME,
                mediaTimelineResult = { Result.success(FakeTimeline()) },
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            assertThat(initialState.mode).isEqualTo(MediaGalleryMode.Images)
            assertThat(initialState.mediaBottomSheetState).isEqualTo(MediaBottomSheetState.Hidden)
            assertThat(initialState.roomName).isEqualTo(A_ROOM_NAME)
            assertThat(initialState.groupedMediaItems.dataOrNull()).isEqualTo(
                GroupedMediaItems(
                    imageAndVideoItems = persistentListOf(),
                    fileItems = persistentListOf(),
                )
            )
            assertThat(initialState.snackbarMessage).isNull()
        }
    }

    @Test
    fun `present - change mode`() = runTest {
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaGalleryNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaGalleryPresenter(
            navigator = navigator,
            room = FakeMatrixRoom(
                displayName = A_ROOM_NAME,
                mediaTimelineResult = { Result.success(FakeTimeline()) },
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
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

    private suspend fun TestScope.`present - bottom sheet state - own message`(canDeleteOwn: Boolean) {
        val presenter = createMediaGalleryPresenter(
            room = FakeMatrixRoom(
                sessionId = A_USER_ID,
                displayName = A_ROOM_NAME,
                mediaTimelineResult = { Result.success(FakeTimeline()) },
                canRedactOwnResult = { Result.success(canDeleteOwn) }
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
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

    private suspend fun TestScope.`present - bottom sheet state - other message`(canDeleteOther: Boolean) {
        val presenter = createMediaGalleryPresenter(
            room = FakeMatrixRoom(
                sessionId = A_USER_ID,
                displayName = A_ROOM_NAME,
                mediaTimelineResult = { Result.success(FakeTimeline()) },
                canRedactOtherResult = { Result.success(canDeleteOther) }
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
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
            room = FakeMatrixRoom(
                displayName = A_ROOM_NAME,
                mediaTimelineResult = { Result.success(FakeTimeline()) },
            )
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
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
    fun `present - view in timeline invokes the navigator`() = runTest {
        val onViewInTimelineClickLambda = lambdaRecorder<EventId, Unit> { }
        val navigator = FakeMediaGalleryNavigator(
            onViewInTimelineClickLambda = onViewInTimelineClickLambda,
        )
        val presenter = createMediaGalleryPresenter(
            room = FakeMatrixRoom(
                mediaTimelineResult = { Result.success(FakeTimeline()) },
            ),
            navigator = navigator,
        )
        presenter.test {
            skipItems(2)
            val initialState = awaitItem()
            initialState.eventSink(MediaGalleryEvents.ViewInTimeline(AN_EVENT_ID))
            onViewInTimelineClickLambda.assertions().isCalledOnce().with(value(AN_EVENT_ID))
        }
    }

    private fun TestScope.createMediaGalleryPresenter(
        matrixMediaLoader: FakeMatrixMediaLoader = FakeMatrixMediaLoader(),
        localMediaActions: FakeLocalMediaActions = FakeLocalMediaActions(),
        snackbarDispatcher: SnackbarDispatcher = SnackbarDispatcher(),
        navigator: MediaGalleryNavigator = FakeMediaGalleryNavigator(),
        room: MatrixRoom = FakeMatrixRoom(
            liveTimeline = FakeTimeline(),
        ),
    ): MediaGalleryPresenter {
        return MediaGalleryPresenter(
            navigator = navigator,
            room = room,
            timelineMediaItemsFactory = TimelineMediaItemsFactory(
                dispatchers = testCoroutineDispatchers(),
                virtualItemFactory = VirtualItemFactory(
                    dateFormatter = FakeDateFormatter(),
                ),
                eventItemFactory = EventItemFactory(
                    fileSizeFormatter = FakeFileSizeFormatter(),
                    fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
                    dateFormatter = FakeDateFormatter(),
                ),
            ),
            localMediaFactory = localMediaFactory,
            mediaLoader = matrixMediaLoader,
            localMediaActions = localMediaActions,
            snackbarDispatcher = snackbarDispatcher,
            mediaItemsPostProcessor = MediaItemsPostProcessor(),
        )
    }
}
