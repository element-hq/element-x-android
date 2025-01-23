/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import android.content.ActivityNotFoundException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOther
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOwn
import io.element.android.libraries.matrix.api.timeline.item.event.toEventOrTransactionId
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaActions
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import io.element.android.libraries.androidutils.R as UtilsR

class MediaViewerPresenter @AssistedInject constructor(
    @Assisted private val inputs: MediaViewerEntryPoint.Params,
    @Assisted private val navigator: MediaViewerNavigator,
    @Assisted private val dataSource: MediaViewerDataSource,
    private val room: MatrixRoom,
    private val localMediaActions: LocalMediaActions,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Presenter<MediaViewerState> {
    @AssistedFactory
    interface Factory {
        fun create(
            inputs: MediaViewerEntryPoint.Params,
            navigator: MediaViewerNavigator,
            dataSource: MediaViewerDataSource,
        ): MediaViewerPresenter
    }

    @Composable
    override fun present(): MediaViewerState {
        val coroutineScope = rememberCoroutineScope()
        val data by dataSource.collectAsState()
        var currentIndex by remember { mutableIntStateOf(searchIndex(data, inputs.eventId)) }
        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        var mediaBottomSheetState by remember { mutableStateOf<MediaBottomSheetState>(MediaBottomSheetState.Hidden) }

        DisposableEffect(Unit) {
            dataSource.setup()
            onDispose {
                dataSource.dispose()
            }
        }
        localMediaActions.Configure()

        fun handleEvents(event: MediaViewerEvents) {
            when (event) {
                is MediaViewerEvents.LoadMedia -> {
                    coroutineScope.downloadMedia(data = event.data)
                }
                is MediaViewerEvents.ClearLoadingError -> {
                    dataSource.clearLoadingError(event.data)
                }
                is MediaViewerEvents.SaveOnDisk -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.saveOnDisk(event.data.downloadedMedia.value)
                }
                is MediaViewerEvents.Share -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.share(event.data.downloadedMedia.value)
                }
                is MediaViewerEvents.OpenWith -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.open(event.data.downloadedMedia.value)
                }
                is MediaViewerEvents.Delete -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    coroutineScope.delete(event.eventId)
                }
                is MediaViewerEvents.ViewInTimeline -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    navigator.onViewInTimelineClick(event.eventId)
                }
                is MediaViewerEvents.OpenInfo -> coroutineScope.launch {
                    mediaBottomSheetState = MediaBottomSheetState.MediaDetailsBottomSheetState(
                        eventId = event.data.eventId,
                        canDelete = when (event.data.mediaInfo.senderId) {
                            null -> false
                            room.sessionId -> room.canRedactOwn().getOrElse { false } && event.data.eventId != null
                            else -> room.canRedactOther().getOrElse { false } && event.data.eventId != null
                        },
                        mediaInfo = event.data.mediaInfo,
                        thumbnailSource = event.data.thumbnailSource,
                    )
                }
                is MediaViewerEvents.ConfirmDelete -> {
                    mediaBottomSheetState = MediaBottomSheetState.MediaDeleteConfirmationState(
                        eventId = event.eventId,
                        mediaInfo = event.data.mediaInfo,
                        thumbnailSource = event.data.thumbnailSource ?: event.data.mediaSource,
                    )
                }
                MediaViewerEvents.CloseBottomSheet -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                }
                is MediaViewerEvents.OnNavigateTo -> {
                    currentIndex = event.index
                }
                is MediaViewerEvents.LoadMore -> coroutineScope.launch {
                    dataSource.loadMore(event.direction)
                }
            }
        }

        return MediaViewerState(
            listData = data,
            currentIndex = currentIndex,
            snackbarMessage = snackbarMessage,
            canShowInfo = inputs.canShowInfo,
            mediaBottomSheetState = mediaBottomSheetState,
            eventSink = ::handleEvents
        )
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
