/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import io.element.android.libraries.voiceplayer.api.AutoplayTimelineItemInfo
import io.element.android.libraries.voiceplayer.api.VoiceMessageAutoplayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

private const val TAG = "VoiceAutoplay"

@ContributesBinding(RoomScope::class)
@SingleIn(RoomScope::class)
class DefaultVoiceMessageAutoplayManager(
    private val mediaPlayer: MediaPlayer,
    private val voiceMessagePlayerFactory: VoiceMessagePlayer.Factory,
    private val transitionSoundPlayer: TransitionSoundPlayer,
    @SessionCoroutineScope private val coroutineScope: CoroutineScope,
) : VoiceMessageAutoplayManager {
    override val resetRequests: SharedFlow<EventId> = MutableSharedFlow()

    private var timelineItems: List<AutoplayTimelineItemInfo> = emptyList()
    private var autoplayJob: Job? = null
    private var cancelled = false

    // Track the mediaId that the autoplay manager set, to detect user interruption
    private var expectedAutoplayMediaId: String? = null

    // Track the last isEnded+mediaId we processed, to avoid re-processing the same end event
    private var lastProcessedEndedMediaId: String? = null

    init {
        Timber.tag(TAG).d("AutoplayManager initialized")
        coroutineScope.launch {
            mediaPlayer.state.collect { state ->
                val mediaId = state.mediaId

                // Reset the ended guard whenever we're no longer in an ended state,
                // so the same track can trigger autoplay again on subsequent listens.
                if (!state.isEnded) {
                    lastProcessedEndedMediaId = null
                }

                val shouldAutoplay = state.isEnded && mediaId != null &&
                    !cancelled && mediaId != lastProcessedEndedMediaId
                if (shouldAutoplay) {
                    lastProcessedEndedMediaId = mediaId
                    onVoiceMessageEnded(mediaId)
                }

                // Detect user interruption: if a different track starts playing
                // and it wasn't triggered by autoplay, cancel the chain
                val isUserInterruption = state.isPlaying && mediaId != null &&
                    expectedAutoplayMediaId != null && mediaId != expectedAutoplayMediaId
                if (isUserInterruption) {
                    Timber.tag(TAG).d("User started a different track, cancelling chain")
                    cancelAutoplayInternal()
                }
            }
        }
    }

    override fun updateTimelineItems(items: List<AutoplayTimelineItemInfo>) {
        this.timelineItems = items
    }

    override fun cancelAutoplay() {
        Timber.tag(TAG).d("cancelAutoplay() called externally")
        cancelAutoplayInternal()
    }

    private fun cancelAutoplayInternal() {
        cancelled = true
        expectedAutoplayMediaId = null
        autoplayJob?.cancel()
        autoplayJob = null
    }

    private fun onVoiceMessageEnded(endedMediaId: String) {
        val items = timelineItems
        Timber.tag(TAG).d("onVoiceMessageEnded: mediaId=%s, timeline has %d items", endedMediaId, items.size)

        val endedIndex = items.indexOfFirst { it.eventId?.value == endedMediaId }
        if (endedIndex < 0) {
            Timber.tag(TAG).d("Ended mediaId not found in timeline items")
            return
        }

        // Timeline is newest-first (index 0 = newest).
        // "Next chronologically" (later in time) = lower index.
        // Skip over virtual items (day separators, etc.) that have no eventId.
        val nextItem = findNextEventItem(items, endedIndex)
        if (nextItem == null) {
            Timber.tag(TAG).d("No next event item found after index %d", endedIndex)
            expectedAutoplayMediaId = null
            return
        }

        val nextEventId = nextItem.eventId
        val nextMediaSource = nextItem.mediaSource
        if (!nextItem.isVoiceMessage || nextEventId == null || nextMediaSource == null) {
            Timber.tag(TAG).d("Next event item is not a voice message, stopping chain")
            expectedAutoplayMediaId = null
            return
        }

        Timber.tag(TAG).d("Starting autoplay of next voice message: %s", nextEventId)
        expectedAutoplayMediaId = nextEventId.value
        cancelled = false

        autoplayJob?.cancel()
        autoplayJob = coroutineScope.launch {
            transitionSoundPlayer.playAndAwait()
            val nextPlayer = voiceMessagePlayerFactory.create(
                eventId = nextEventId,
                mediaSource = nextMediaSource,
                mimeType = nextItem.mimeType,
                filename = nextItem.filename,
            )
            nextPlayer.prepare().onSuccess {
                Timber.tag(TAG).d("Prepared next voice message, now playing")
                nextPlayer.play()
            }.onFailure {
                Timber.tag(TAG).e(it, "Failed to prepare next voice message")
                expectedAutoplayMediaId = null
            }
        }
    }

    /**
     * Find the next event item (skipping virtual items like day separators)
     * in the chronological direction (toward newer = lower index).
     */
    private fun findNextEventItem(
        items: List<AutoplayTimelineItemInfo>,
        currentIndex: Int,
    ): AutoplayTimelineItemInfo? {
        for (i in currentIndex - 1 downTo 0) {
            val item = items[i]
            // Virtual items have null eventId, skip them
            if (item.eventId != null) {
                return item
            }
        }
        return null
    }
}
