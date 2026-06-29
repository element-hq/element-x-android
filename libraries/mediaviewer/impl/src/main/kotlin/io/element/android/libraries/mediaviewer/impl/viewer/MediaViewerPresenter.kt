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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.collectLatest
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

    private val eventId = inputs.eventId()
    private val mediaSource = inputs.mediaSource()

    @Composable
    override fun present(): MediaViewerState {
        val coroutineScope = rememberCoroutineScope()
        val currentIndex = remember {
            val firstIndex = if (inputs is MediaViewerEntryPoint.Params.EventGallery) {
                // Order is reversed so we have to reverse the index
                inputs.galleryItems.lastIndex - inputs.galleryInfo.initialIndex
            } else {
                dataSource.findEventIndex(eventId, mediaSource) ?: 0
            }
            mutableIntStateOf(firstIndex)
        }
        val data = dataSource.produceState { flow ->
            flow.collectLatest { new ->
                val existingItem = value.getOrNull(currentIndex.intValue)
                val newItem = new.getOrNull(currentIndex.intValue)
                if (existingItem is MediaViewerPageData.MediaViewerData && existingItem.eventId == eventId && newItem != existingItem) {
                    currentIndex.intValue = dataSource.findEventIndex(eventId, mediaSource) ?: 0
                } else if (currentIndex.intValue > 0 && value.firstOrNull() is MediaViewerPageData.Loading &&
                    new.firstOrNull() !is MediaViewerPageData.Loading) {
                    // Restore index based on the eventId after the initial items have been loaded
                    currentIndex.intValue = dataSource.findEventIndex(eventId, mediaSource) ?: 0
                }
                value = new
            }
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        // Add both forward and backward pagination state checks to display a snackbar when there is no more items to load in either direction
        NoMoreItemsSnackBarDisplayer(currentIndex, data, Timeline.PaginationDirection.FORWARDS)
        NoMoreItemsSnackBarDisplayer(currentIndex, data, Timeline.PaginationDirection.BACKWARDS)

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
                is MediaViewerEvent.CancelLoadingMedia -> {
                    dataSource.cancelLoadingMedia(event.data)
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
                        fromPinnedEvents = when (inputs) {
                            is MediaViewerEntryPoint.Params.RoomMedia -> when (val myMode = inputs.mode) {
                                is MediaViewerEntryPoint.MediaViewerMode.EventGallery -> myMode.fromPinnedMessages
                                is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios -> myMode.getTimelineMode() == Timeline.Mode.PinnedEvents
                                is MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos -> myMode.getTimelineMode() == Timeline.Mode.PinnedEvents
                            }
                            is MediaViewerEntryPoint.Params.EventGallery -> inputs.fromPinnedMessages
                            is MediaViewerEntryPoint.Params.Avatar -> false
                        },
                    )
                }
                is MediaViewerEvent.OpenInfo -> coroutineScope.launch {
                    mediaBottomSheetState = MediaBottomSheetState.Details(
                        fromGallery = false,
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
            initiallySelectedEventId = eventId,
            listData = data.value,
            currentIndex = currentIndex.intValue,
            snackbarMessage = snackbarMessage,
            canShowInfo = inputs !is MediaViewerEntryPoint.Params.Avatar,
            mediaBottomSheetState = mediaBottomSheetState,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun NoMoreItemsSnackBarDisplayer(
        currentIndex: IntState,
        data: State<ImmutableList<MediaViewerPageData>>,
        direction: Timeline.PaginationDirection,
    ) {
        var previousIndex by remember { mutableIntStateOf(currentIndex.intValue) }
        var previousDataSize by remember { mutableIntStateOf(data.value.size) }
        var wasLoading: Boolean? by remember { mutableStateOf(null) }
        LaunchedEffect(currentIndex.intValue, data.value) {
            fun isLoading(index: Int, data: List<MediaViewerPageData>, direction: Timeline.PaginationDirection): Boolean {
                return when (direction) {
                    Timeline.PaginationDirection.BACKWARDS -> index == data.lastIndex && data.lastOrNull() is MediaViewerPageData.Loading
                    Timeline.PaginationDirection.FORWARDS -> index == 0 && data.firstOrNull() is MediaViewerPageData.Loading
                }
            }
            // Reset the effect when the user navigate to another item so we only take into account index changes caused by data changes
            if (previousIndex != currentIndex.intValue) {
                wasLoading = null
                previousIndex = currentIndex.intValue
            }
            // If we were navigating backwards and the data size grew, we can discard the previous value: it means we received new items
            if (direction == Timeline.PaginationDirection.BACKWARDS && previousDataSize < data.value.size) {
                wasLoading = null
            }

            val isLoading = isLoading(currentIndex.intValue, data.value, direction)

            if (wasLoading == true && !isLoading) {
                showNoMoreItemsSnackbar()
            }

            previousDataSize = data.value.size
            wasLoading = isLoading
        }
    }

    private fun showNoMoreItemsSnackbar() {
        if (inputs is MediaViewerEntryPoint.Params.RoomMedia) {
            val messageResId = when (inputs.mode) {
                is MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos -> R.string.screen_media_details_no_more_media_to_show
                is MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios -> R.string.screen_media_details_no_more_files_to_show
                // Should not happen
                is MediaViewerEntryPoint.MediaViewerMode.EventGallery -> R.string.screen_media_details_no_more_media_to_show
            }
            val message = SnackbarMessage(messageResId)
            snackbarDispatcher.post(message)
        }
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
}

private fun MediaViewerEntryPoint.Params.eventId() = when (this) {
    is MediaViewerEntryPoint.Params.Avatar -> null
    is MediaViewerEntryPoint.Params.EventGallery -> eventId
    is MediaViewerEntryPoint.Params.RoomMedia -> eventId
}

private fun MediaViewerEntryPoint.Params.mediaSource() = when (this) {
    is MediaViewerEntryPoint.Params.Avatar -> mediaSource
    is MediaViewerEntryPoint.Params.EventGallery -> null
    is MediaViewerEntryPoint.Params.RoomMedia -> mediaSource
}
