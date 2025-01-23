/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaFile
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.mediaviewer.impl.gallery.MediaGalleryDataSource
import io.element.android.libraries.mediaviewer.impl.gallery.MediaGalleryMode
import io.element.android.libraries.mediaviewer.impl.gallery.MediaItem
import io.element.android.libraries.mediaviewer.impl.gallery.eventId
import io.element.android.libraries.mediaviewer.impl.gallery.mediaInfo
import io.element.android.libraries.mediaviewer.impl.gallery.mediaSource
import io.element.android.libraries.mediaviewer.impl.gallery.thumbnailSource
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class MediaViewerDataSource(
    private val galleryMode: MediaGalleryMode,
    private val dispatcher: CoroutineDispatcher,
    private val galleryDataSource: MediaGalleryDataSource,
    private val mediaLoader: MatrixMediaLoader,
    private val localMediaFactory: LocalMediaFactory,
    private val systemClock: SystemClock,
) {
    // List of media files that are currently being loaded
    private val mediaFiles: MutableList<MediaFile> = mutableListOf()

    // Map of sourceUrl to local media state
    private val localMediaStates: MutableMap<String, MutableState<AsyncData<LocalMedia>>> =
        mutableMapOf()

    fun setup() {
        galleryDataSource.start()
    }

    fun dispose() {
        mediaFiles.forEach { it.close() }
        mediaFiles.clear()
        localMediaStates.clear()
    }

    @Composable
    fun collectAsState(): State<PersistentList<MediaViewerPageData>> {
        return remember { dataFlow() }.collectAsState(initialData())
    }

    @VisibleForTesting
    fun dataFlow(): Flow<PersistentList<MediaViewerPageData>> {
        return galleryDataSource.groupedMediaItemsFlow()
            .map { groupedItems ->
                when (groupedItems) {
                    AsyncData.Uninitialized,
                    is AsyncData.Loading -> {
                        persistentListOf(
                            MediaViewerPageData.Loading(
                                direction = Timeline.PaginationDirection.BACKWARDS,
                                timestamp = systemClock.epochMillis(),
                            )
                        )
                    }
                    is AsyncData.Failure -> {
                        persistentListOf(
                            MediaViewerPageData.Failure(groupedItems.error),
                        )
                    }
                    is AsyncData.Success -> {
                        withContext(dispatcher) {
                            val mediaItems = groupedItems.data.getItems(galleryMode)
                            buildMediaViewerPageList(mediaItems)
                        }
                    }
                }
            }
    }

    private fun initialData(): PersistentList<MediaViewerPageData> {
        val initialMediaItems =
            galleryDataSource.getLastData().dataOrNull()?.getItems(galleryMode).orEmpty()
        return buildMediaViewerPageList(initialMediaItems)
    }

    /**
     * Build a list of [MediaViewerPageData] from a list of [MediaItem].
     * In particular, create a mutable state of AsyncData<LocalMedia> for each media item, which
     * will be used to render the downloaded media (see [loadMedia] which will update this value).
     */
    private fun buildMediaViewerPageList(groupedItems: List<MediaItem>) = buildList {
        groupedItems.forEach { mediaItem ->
            when (mediaItem) {
                is MediaItem.DateSeparator -> Unit
                is MediaItem.Event -> {
                    val sourceUrl = mediaItem.mediaSource().url
                    val localMedia = localMediaStates.getOrPut(sourceUrl) {
                        mutableStateOf(AsyncData.Uninitialized)
                    }
                    add(
                        MediaViewerPageData.MediaViewerData(
                            eventId = mediaItem.eventId(),
                            mediaInfo = mediaItem.mediaInfo(),
                            mediaSource = mediaItem.mediaSource(),
                            thumbnailSource = mediaItem.thumbnailSource(),
                            downloadedMedia = localMedia,
                        )
                    )
                }
                is MediaItem.LoadingIndicator -> add(
                    MediaViewerPageData.Loading(
                        direction = mediaItem.direction,
                        timestamp = systemClock.epochMillis(),
                    )
                )
            }
        }
    }.toPersistentList()

    fun clearLoadingError(data: MediaViewerPageData.MediaViewerData) {
        localMediaStates[data.mediaSource.url]?.value = AsyncData.Uninitialized
    }

    suspend fun loadMore(direction: Timeline.PaginationDirection) {
        galleryDataSource.loadMore(direction)
    }

    suspend fun loadMedia(data: MediaViewerPageData.MediaViewerData) {
        Timber.d("loadMedia for ${data.eventId}")
        val localMediaState = localMediaStates.getOrPut(data.mediaSource.url) {
            mutableStateOf(AsyncData.Uninitialized)
        }
        localMediaState.value = AsyncData.Loading()
        mediaLoader
            .downloadMediaFile(
                source = data.mediaSource,
                mimeType = data.mediaInfo.mimeType,
                filename = data.mediaInfo.filename
            )
            .onSuccess { mediaFile ->
                mediaFiles.add(mediaFile)
            }
            .mapCatching { mediaFile ->
                localMediaFactory.createFromMediaFile(
                    mediaFile = mediaFile,
                    mediaInfo = data.mediaInfo
                )
            }
            .onSuccess {
                localMediaState.value = AsyncData.Success(it)
            }
            .onFailure {
                localMediaState.value = AsyncData.Failure(it)
            }
    }
}
