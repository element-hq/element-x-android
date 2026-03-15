/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.player

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.AudioMessageType
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.VideoMessageType
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

private const val PAGINATION_TIMEOUT_MS = 10_000L
private const val ARTWORK_THUMBNAIL_SIZE = 256L

class MediaPlaylistManager(
    private val matrixClientProvider: MatrixClientProvider,
    private val localMediaFactory: LocalMediaFactory,
    private val coroutineScope: CoroutineScope,
    private val onPlayableItemsChanged: () -> Unit = {},
) {
    data class PlayableItem(
        val eventId: EventId,
        val mediaSource: MediaSource,
        val thumbnailSource: MediaSource?,
        val filename: String,
        val mimeType: String,
        val senderName: String?,
        val senderAvatar: String?,
    )

    data class SkipResult(
        val mediaItem: MediaItem,
        val eventId: EventId,
    )

    private val skipMutex = Mutex()
    private var currentSessionId: String? = null
    private var currentRoomId: String? = null
    var currentEventId: EventId? = null
        private set

    private var room: JoinedRoom? = null
    private var timeline: Timeline? = null
    private var mediaLoader: MatrixMediaLoader? = null
    private var playableItems: List<PlayableItem> = emptyList()
    private var collectionJob: Job? = null

    val hasNext: Boolean
        get() {
            val eventId = currentEventId ?: return false
            val index = playableItems.indexOfFirst { it.eventId == eventId }
            return index >= 0 && index < playableItems.size - 1
        }

    val hasPrevious: Boolean
        get() {
            val eventId = currentEventId ?: return false
            val index = playableItems.indexOfFirst { it.eventId == eventId }
            return index > 0
        }

    fun initialize(sessionId: String, roomId: String, eventId: String) {
        if (sessionId == currentSessionId && roomId == currentRoomId) {
            currentEventId = EventId(eventId)
            return
        }
        release()
        currentSessionId = sessionId
        currentRoomId = roomId
        currentEventId = EventId(eventId)

        collectionJob = coroutineScope.launch {
            try {
                val client = matrixClientProvider.getOrRestore(UserId(sessionId)).getOrNull() ?: return@launch
                mediaLoader = client.matrixMediaLoader
                val joinedRoom = client.getJoinedRoom(RoomId(roomId)) ?: return@launch
                room = joinedRoom

                val mediaTimeline = joinedRoom.createTimeline(
                    CreateTimelineParams.MediaOnlyFocused(EventId(eventId))
                ).getOrNull() ?: return@launch
                timeline = mediaTimeline

                mediaTimeline.timelineItems.collect { items ->
                    playableItems = items
                        .filterIsInstance<MatrixTimelineItem.Event>()
                        .mapNotNull { toPlayableItem(it) }
                    onPlayableItemsChanged()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize media playlist")
            }
        }
    }

    private fun toPlayableItem(item: MatrixTimelineItem.Event): PlayableItem? {
        val eventId = item.eventId ?: return null
        val content = item.event.content as? MessageContent ?: return null
        val senderName = (item.event.senderProfile as? ProfileDetails.Ready)?.displayName
        val senderAvatar = (item.event.senderProfile as? ProfileDetails.Ready)?.avatarUrl

        return when (val type = content.type) {
            is AudioMessageType -> PlayableItem(
                eventId = eventId,
                mediaSource = type.source,
                thumbnailSource = null,
                filename = type.filename,
                mimeType = type.info?.mimetype ?: "audio/*",
                senderName = senderName,
                senderAvatar = senderAvatar,
            )
            is VideoMessageType -> PlayableItem(
                eventId = eventId,
                mediaSource = type.source,
                thumbnailSource = type.info?.thumbnailSource,
                filename = type.filename,
                mimeType = type.info?.mimetype ?: "video/*",
                senderName = senderName,
                senderAvatar = senderAvatar,
            )
            else -> null
        }
    }

    suspend fun skipToNext(): SkipResult? = skipMutex.withLock {
        val eventId = currentEventId ?: return null
        val currentIndex = playableItems.indexOfFirst { it.eventId == eventId }
        if (currentIndex < 0) return null

        val nextIndex = currentIndex + 1
        if (nextIndex < playableItems.size) {
            return buildSkipResult(playableItems[nextIndex])
        }

        // Try paginating forward for more items
        val tl = timeline ?: return null
        if (tl.forwardPaginationStatus.value.hasMoreToLoad) {
            tl.paginate(Timeline.PaginationDirection.FORWARDS)
            val newItems = waitForNewPlayableItems(currentIndex, forward = true) ?: return null
            return buildSkipResult(newItems)
        }
        return null
    }

    suspend fun skipToPrevious(): SkipResult? = skipMutex.withLock {
        val eventId = currentEventId ?: return null
        val currentIndex = playableItems.indexOfFirst { it.eventId == eventId }
        if (currentIndex < 0) return null

        val prevIndex = currentIndex - 1
        if (prevIndex >= 0) {
            return buildSkipResult(playableItems[prevIndex])
        }

        // Try paginating backward for more items
        val tl = timeline ?: return null
        if (tl.backwardPaginationStatus.value.hasMoreToLoad) {
            tl.paginate(Timeline.PaginationDirection.BACKWARDS)
            val newItem = waitForNewPlayableItems(currentIndex, forward = false) ?: return null
            return buildSkipResult(newItem)
        }
        return null
    }

    private suspend fun waitForNewPlayableItems(currentIndex: Int, forward: Boolean): PlayableItem? {
        val tl = timeline ?: return null
        return withTimeoutOrNull(PAGINATION_TIMEOUT_MS) {
            tl.timelineItems.first { items ->
                val newPlayable = items
                    .filterIsInstance<MatrixTimelineItem.Event>()
                    .mapNotNull { toPlayableItem(it) }
                if (forward) {
                    newPlayable.size > playableItems.size &&
                        newPlayable.getOrNull(currentIndex + 1) != null
                } else {
                    newPlayable.size > playableItems.size
                }
            }
            // After flow emitted, playableItems should be updated by the collection job
            val eventId = currentEventId ?: return@withTimeoutOrNull null
            val newIndex = playableItems.indexOfFirst { it.eventId == eventId }
            if (newIndex < 0) return@withTimeoutOrNull null
            if (forward) {
                playableItems.getOrNull(newIndex + 1)
            } else {
                playableItems.getOrNull(newIndex - 1)
            }
        }
    }

    private suspend fun buildSkipResult(item: PlayableItem): SkipResult? {
        val loader = mediaLoader ?: return null
        return try {
            val mediaFile = loader.downloadMediaFile(
                source = item.mediaSource,
                mimeType = item.mimeType,
                filename = item.filename,
            ).getOrNull() ?: return null

            val mediaInfo = MediaInfo(
                filename = item.filename,
                caption = null,
                mimeType = item.mimeType,
                fileSize = null,
                formattedFileSize = "",
                fileExtension = item.filename.substringAfterLast('.', ""),
                senderId = null,
                senderName = item.senderName,
                senderAvatar = null,
                dateSent = null,
                dateSentFull = null,
                waveform = null,
                duration = null,
            )
            val localMedia = localMediaFactory.createFromMediaFile(mediaFile, mediaInfo)

            // Download artwork for the notification
            val artworkSource = item.thumbnailSource
                ?: item.senderAvatar?.let { MediaSource(it) }
            val artworkBytes = artworkSource?.let { source ->
                loader.loadMediaThumbnail(source, ARTWORK_THUMBNAIL_SIZE, ARTWORK_THUMBNAIL_SIZE).getOrNull()
            }

            val extras = Bundle().apply {
                putString("sessionId", currentSessionId)
                putString("roomId", currentRoomId)
                putString("eventId", item.eventId.value)
            }
            val metadata = MediaMetadata.Builder()
                .setTitle(item.filename)
                .setArtist(item.senderName)
                .apply {
                    if (artworkBytes != null) {
                        setArtworkData(artworkBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                    }
                }
                .setExtras(extras)
                .build()
            val mediaItem = MediaItem.Builder()
                .setMediaId(item.eventId.value)
                .setUri(localMedia.uri)
                .setMediaMetadata(metadata)
                .build()

            currentEventId = item.eventId
            SkipResult(mediaItem = mediaItem, eventId = item.eventId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to download media for skip")
            null
        }
    }

    fun release() {
        collectionJob?.cancel()
        collectionJob = null
        timeline?.close()
        timeline = null
        room = null
        mediaLoader = null
        playableItems = emptyList()
        currentSessionId = null
        currentRoomId = null
        currentEventId = null
    }
}
