/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.api

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.coroutines.flow.SharedFlow
import kotlin.time.Duration

/**
 * Manages auto-playing consecutive voice messages in the timeline.
 *
 * When a voice message finishes playing, if the next chronological message
 * is also a voice message, it will automatically start playing.
 * The chain continues until a non-voice message is encountered.
 */
interface VoiceMessageAutoplayManager {
    /**
     * Feed the current timeline items so the manager can determine adjacency.
     * Must be called whenever the timeline items change.
     *
     * @param items The current timeline items, ordered newest-first (index 0 = newest).
     */
    fun updateTimelineItems(items: List<AutoplayTimelineItemInfo>)

    /**
     * Emits event IDs of voice messages that should reset their playback position to 0:00.
     * This is emitted when a voice message finishes and autoplay proceeds to the next one.
     */
    val resetRequests: SharedFlow<EventId>

    /**
     * Cancel any pending autoplay chain.
     * Should be called when the user manually interacts with playback controls.
     */
    fun cancelAutoplay()
}

/**
 * Lightweight info about a timeline item needed for autoplay adjacency checks.
 */
data class AutoplayTimelineItemInfo(
    val eventId: EventId?,
    val isVoiceMessage: Boolean,
    val mediaSource: MediaSource?,
    val mimeType: String?,
    val filename: String?,
    val duration: Duration?,
)
