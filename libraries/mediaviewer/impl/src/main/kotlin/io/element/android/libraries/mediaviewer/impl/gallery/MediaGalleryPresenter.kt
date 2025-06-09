/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery

import android.content.ActivityNotFoundException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.libraries.androidutils.R
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOther
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOwn
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.impl.datasource.MediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.local.LocalMediaActions
import io.element.android.libraries.mediaviewer.impl.model.GroupedMediaItems
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.mediaviewer.impl.model.eventId
import io.element.android.libraries.mediaviewer.impl.model.mediaInfo
import io.element.android.libraries.mediaviewer.impl.model.mediaSource
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

class MediaGalleryPresenter @AssistedInject constructor(
    @Assisted private val navigator: MediaGalleryNavigator,
    private val room: BaseRoom,
    private val mediaGalleryDataSource: MediaGalleryDataSource,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaLoader: MatrixMediaLoader,
    private val localMediaActions: LocalMediaActions,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Presenter<MediaGalleryState> {
    @AssistedFactory
    interface Factory {
        fun create(
            navigator: MediaGalleryNavigator,
        ): MediaGalleryPresenter
    }

    @Composable
    override fun present(): MediaGalleryState {
        val coroutineScope = rememberCoroutineScope()
        var mode by remember { mutableStateOf(MediaGalleryMode.Images) }

        val roomInfo by room.roomInfoFlow.collectAsState()

        var mediaBottomSheetState by remember { mutableStateOf<MediaBottomSheetState>(MediaBottomSheetState.Hidden) }

        val groupedMediaItems by remember {
            mediaGalleryDataSource.groupedMediaItemsFlow()
        }
            .collectAsState(AsyncData.Uninitialized)

        LaunchedEffect(Unit) {
            mediaGalleryDataSource.start()
        }

        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()
        localMediaActions.Configure()

        fun handleEvents(event: MediaGalleryEvents) {
            when (event) {
                is MediaGalleryEvents.ChangeMode -> {
                    mode = event.mode
                }
                is MediaGalleryEvents.LoadMore -> coroutineScope.launch {
                    mediaGalleryDataSource.loadMore(event.direction)
                }
                is MediaGalleryEvents.Delete -> coroutineScope.launch {
                    mediaGalleryDataSource.deleteItem(event.eventId)
                }
                is MediaGalleryEvents.SaveOnDisk -> coroutineScope.launch {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    groupedMediaItems.dataOrNull().find(event.eventId)?.let {
                        saveOnDisk(it)
                    }
                }
                is MediaGalleryEvents.Share -> coroutineScope.launch {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    groupedMediaItems.dataOrNull().find(event.eventId)?.let {
                        share(it)
                    }
                }
                is MediaGalleryEvents.ViewInTimeline -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                    navigator.onViewInTimelineClick(event.eventId)
                }
                is MediaGalleryEvents.OpenInfo -> coroutineScope.launch {
                    mediaBottomSheetState = MediaBottomSheetState.MediaDetailsBottomSheetState(
                        eventId = event.mediaItem.eventId(),
                        canDelete = when (event.mediaItem.mediaInfo().senderId) {
                            null -> false
                            room.sessionId -> room.canRedactOwn().getOrElse { false } && event.mediaItem.eventId() != null
                            else -> room.canRedactOther().getOrElse { false } && event.mediaItem.eventId() != null
                        },
                        mediaInfo = event.mediaItem.mediaInfo(),
                        thumbnailSource = when (event.mediaItem) {
                            is MediaItem.Image -> event.mediaItem.thumbnailSource ?: event.mediaItem.mediaSource
                            is MediaItem.Video -> event.mediaItem.thumbnailSource ?: event.mediaItem.mediaSource
                            is MediaItem.Audio -> null
                            is MediaItem.File -> null
                            is MediaItem.Voice -> null
                        },
                    )
                }
                is MediaGalleryEvents.ConfirmDelete -> {
                    mediaBottomSheetState = MediaBottomSheetState.MediaDeleteConfirmationState(
                        eventId = event.eventId,
                        mediaInfo = event.mediaInfo,
                        thumbnailSource = event.thumbnailSource,
                    )
                }
                MediaGalleryEvents.CloseBottomSheet -> {
                    mediaBottomSheetState = MediaBottomSheetState.Hidden
                }
            }
        }

        return MediaGalleryState(
            roomName = roomInfo.name.orEmpty(),
            mode = mode,
            groupedMediaItems = groupedMediaItems,
            mediaBottomSheetState = mediaBottomSheetState,
            snackbarMessage = snackbarMessage,
            eventSink = ::handleEvents
        )
    }

    private suspend fun downloadMedia(mediaItem: MediaItem.Event): Result<LocalMedia> {
        return mediaLoader.downloadMediaFile(
            source = mediaItem.mediaSource(),
            mimeType = mediaItem.mediaInfo().mimeType,
            filename = mediaItem.mediaInfo().filename
        )
            .mapCatchingExceptions { mediaFile ->
                localMediaFactory.createFromMediaFile(
                    mediaFile = mediaFile,
                    mediaInfo = mediaItem.mediaInfo()
                )
            }
    }

    private suspend fun saveOnDisk(mediaItem: MediaItem.Event) {
        downloadMedia(mediaItem)
            .mapCatchingExceptions { localMedia ->
                localMediaActions.saveOnDisk(localMedia)
            }
            .onSuccess {
                val snackbarMessage = SnackbarMessage(CommonStrings.common_file_saved_on_disk_android)
                snackbarDispatcher.post(snackbarMessage)
            }
            .onFailure {
                val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                snackbarDispatcher.post(snackbarMessage)
            }
    }

    private suspend fun share(mediaItem: MediaItem.Event) {
        downloadMedia(mediaItem)
            .mapCatchingExceptions { localMedia ->
                localMediaActions.share(localMedia)
            }
            .onFailure {
                val snackbarMessage = SnackbarMessage(mediaActionsError(it))
                snackbarDispatcher.post(snackbarMessage)
            }
    }

    private fun mediaActionsError(throwable: Throwable): Int {
        return if (throwable is ActivityNotFoundException) {
            R.string.error_no_compatible_app_found
        } else {
            CommonStrings.error_unknown
        }
    }
}

private fun GroupedMediaItems?.find(eventId: EventId?): MediaItem.Event? {
    if (this == null || eventId == null) {
        return null
    }
    return (imageAndVideoItems + fileItems).filterIsInstance<MediaItem.Event>()
        .firstOrNull { it.eventId() == eventId }
}
