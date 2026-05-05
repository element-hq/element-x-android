/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import android.content.ActivityNotFoundException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.R
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaActions
import io.element.android.libraries.mediaviewer.impl.model.MediaPermissions
import io.element.android.libraries.mediaviewer.impl.model.mediaPermissions
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import io.element.android.libraries.androidutils.R as UtilsR

@AssistedInject
class MediaViewerPresenter(
    @Assisted private val inputs: MediaViewerEntryPoint.Params,
    @Assisted private val navigator: MediaViewerNavigator,
    @Assisted private val dataSource: MediaViewerDataSource,
    private val room: JoinedRoom,
    private val localMediaActions: LocalMediaActions,
) : Presenter<MediaViewerState> {
    @AssistedFactory
    fun interface Factory {
        fun create(
            inputs: MediaViewerEntryPoint.Params,
            navigator: MediaViewerNavigator,
            dataSource: MediaViewerDataSource,
        ): MediaViewerPresenter
    }

    // Use a local snackbarDispatcher because this presenter is used in an Overlay Node
    private val snackbarDispatcher = SnackbarDispatcher()

    @Composable
    override fun present(): MediaViewerState {
        val coroutineScope = rememberCoroutineScope()
        val data = dataSource.collectAsState()
        val currentIndex = remember { mutableIntStateOf(searchIndex(data.value, inputs.eventId)) }
        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        NoMoreItemsBackwardSnackBarDisplayer(currentIndex, data)
        NoMoreItemsForwardSnackBarDisplayer(currentIndex, data)

        val permissions by room.permissionsAsState(MediaPermissions.DEFAULT) { perms ->
            perms.mediaPermissions()
        }
        var mediaBottomSheetState by remember { mutableStateOf<MediaBottomSheetState>(MediaBottomSheetState.Hidden) }

        DisposableEffect(Unit) {
            dataSource.setup(coroutineScope)
            onDispose {
                dataSource.dispose()
            }
        }
        localMediaActions.Configure()

        fun handleEvent(event: MediaViewerEvent) {
            when (event) {
                is MediaViewerEvent.LoadMedia -> {
                    coroutineScope.downloadMedia(data = event.data)
                }
                is MediaViewerEvent.ClearLoadingError -> {
                    dataSource.clearLoadingError(event.data)
                }
                is MediaViewerEvent.SaveOnDisk -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.saveOnDisk(event.data.downloadedMedia.value)
                }
                is MediaViewerEvent.Share -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.share(event.data.downloadedMedia.value)
                }
                is MediaViewerEvent.OpenWith -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.open(event.data.downloadedMedia.value)
                }
                is MediaViewerEvent.Delete -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.delete(event.eventId)
                }
                is MediaViewerEvent.ViewInTimeline -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    navigator.onViewInTimelineClick(event.eventId)
                }
                is MediaViewerEvent.Forward -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    navigator.onForwardClick(
                        eventId = event.eventId,
                        fromPinnedEvents = inputs.mode.getTimelineMode() == Timeline.Mode.PinnedEvents,
                    )
                }
                is MediaViewerEvent.OpenInfo -> coroutineScope.launch {
                    mediaBottomSheetState = MediaBottomSheetState.Details(
                        eventId = event.data.eventId,
                        canDelete = when (event.data.mediaInfo.senderId) {
                            null -> false
                            room.sessionId -> permissions.canRedactOwn && event.data.eventId != null
                            else -> permissions.canRedactOther && event.data.eventId != null
                        },
                        mediaInfo = event.data.mediaInfo,
                        thumbnailSource = event.data.thumbnailSource,
                    )
                }
                is MediaViewerEvent.ConfirmDelete -> {
                    mediaBottomSheetState = MediaBottomSheetState.DeleteConfirmation(
                        eventId = event.eventId,
                        mediaInfo = event.data.mediaInfo,
                        thumbnailSource = event.data.thumbnailSource ?: event.data.mediaSource,
                    )
                }
                MediaViewerEvent.CloseBottomSheet -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                }
                is MediaViewerEvent.OnNavigateTo -> {
                    currentIndex.intValue = event.index
                }
                is MediaViewerEvent.LoadMore -> coroutineScope.launch {
                    dataSource.loadMore(event.direction)
                }
            }
        }

        return MediaViewerState(
            initiallySelectedEventId = inputs.eventId,
            listData = data.value,
            currentIndex = currentIndex.intValue,
            snackbarMessage = snackbarMessage,
            canShowInfo = inputs.canShowInfo,
            mediaBottomSheetState = mediaBottomSheetState,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun NoMoreItemsBackwardSnackBarDisplayer(
        currentIndex: IntState,
        data: State<ImmutableList<MediaViewerPageData>>,
    ) {
        val isRenderingLoadingBackward by remember {
            derivedStateOf {
                currentIndex.intValue == 0 &&
                    data.value.size > 1 &&
                    data.value.firstOrNull() is MediaViewerPageData.Loading &&
                    (data.value.firstOrNull() as? MediaViewerPageData.Loading)?.direction == Timeline.PaginationDirection.BACKWARDS
            }
        }
        if (isRenderingLoadingBackward) {
            LaunchedEffect(Unit) {
                // Observe the loading data vanishing
                snapshotFlow {
                    val first = data.value.firstOrNull()
                    first is MediaViewerPageData.Loading && first.direction == Timeline.PaginationDirection.BACKWARDS
                }
                    .distinctUntilChanged()
                    .filter { !it }
                    .onEach { showNoMoreItemsSnackbar() }
                    .launchIn(this)
            }
        }
    }

    @Composable
    private fun NoMoreItemsForwardSnackBarDisplayer(
        currentIndex: IntState,
        data: State<ImmutableList<MediaViewerPageData>>,
    ) {
        val isRenderingLoadingForward by remember {
            derivedStateOf {
                currentIndex.intValue == data.value.lastIndex &&
                    data.value.size > 1 &&
                    data.value.lastOrNull() is MediaViewerPageData.Loading &&
                    (data.value.lastOrNull() as? MediaViewerPageData.Loading)?.direction == Timeline.PaginationDirection.FORWARDS
            }
        }
        if (isRenderingLoadingForward) {
            LaunchedEffect(Unit) {
                // Observe the loading data vanishing
                snapshotFlow {
                    val last = data.value.lastOrNull()
                    last is MediaViewerPageData.Loading && last.direction == Timeline.PaginationDirection.FORWARDS
                }
                    .distinctUntilChanged()
                    .filter { !it }
                    .onEach { showNoMoreItemsSnackbar() }
                    .launchIn(this)
            }
        }
    }

    private fun showNoMoreItemsSnackbar() {
        val messageResId = when (inputs.mode) {
            MediaViewerEntryPoint.MediaViewerMode.SingleMedia,
            is MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos -> R.string.screen_media_details_no_more_media_to_show
            is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios -> R.string.screen_media_details_no_more_files_to_show
        }
        val message = SnackbarMessage(messageResId)
        snackbarDispatcher.post(message)
    }

    private fun CoroutineScope.downloadMedia(
        data: MediaViewerPageData.MediaViewerData,
    ) = launch {
        dataSource.loadMedia(data)
    }

    private fun CoroutineScope.saveOnDisk(localMedia: AsyncData<LocalMedia>) = launch {
        if (localMedia is AsyncData.Success) {
            localMediaActions.saveOnDisk(localMedia.data)
                .onSuccess {
                    val snackbarMessage = SnackbarMessage(CommonStrings.common_file_saved_on_disk_android)
                    snackbarDispatcher.post(snackbarMessage)
                }
                .onFailure {
                    val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                    snackbarDispatcher.post(snackbarMessage)
                }
        }
    }

    private fun CoroutineScope.delete(eventId: EventId) = launch {
        room.liveTimeline.redactEvent(eventId.toEventOrTransactionId(), null)
            .onFailure {
                val snackbarMessage = SnackbarMessage(CommonStrings.error_unknown)
                snackbarDispatcher.post(snackbarMessage)
            }
            .onSuccess {
                navigator.onItemDeleted()
            }
    }

    private fun CoroutineScope.share(localMedia: AsyncData<LocalMedia>) = launch {
        if (localMedia is AsyncData.Success) {
            localMediaActions.share(localMedia.data)
                .onFailure {
                    val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                    snackbarDispatcher.post(snackbarMessage)
                }
        }
    }

    private fun CoroutineScope.open(localMedia: AsyncData<LocalMedia>) = launch {
        if (localMedia is AsyncData.Success) {
            localMediaActions.open(localMedia.data)
                .onFailure {
                    val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                    snackbarDispatcher.post(snackbarMessage)
                }
        }
    }

    private fun mediaActionsError(throwable: Throwable): Int {
        return if (throwable is ActivityNotFoundException) {
            UtilsR.string.error_no_compatible_app_found
        } else {
            CommonStrings.error_unknown
        }
    }

    private fun searchIndex(data: List<MediaViewerPageData>, eventId: EventId?): Int {
        if (eventId == null) {
            return 0
        }
        return data.indexOfFirst {
            (it as? MediaViewerPageData.MediaViewerData)?.eventId == eventId
        }.coerceAtLeast(0)
    }
}
