/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.RedactedContent
import io.element.android.libraries.mediaplayer.api.MediaPlayer
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface RedactedVoiceMessageManager {
    suspend fun onEachMatrixTimelineItem(timelineItems: List<MatrixTimelineItem>)
}

@ContributesBinding(RoomScope::class)
class DefaultRedactedVoiceMessageManager @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val mediaPlayer: MediaPlayer,
) : RedactedVoiceMessageManager {
    override suspend fun onEachMatrixTimelineItem(timelineItems: List<MatrixTimelineItem>) {
        withContext(dispatchers.computation) {
            mediaPlayer.state.value.let { playerState ->
                if (playerState.isPlaying && playerState.mediaId != null) {
                    val needsToPausePlayer = timelineItems.any {
                        it is MatrixTimelineItem.Event &&
                            playerState.mediaId == it.eventId?.value &&
                            it.event.content is RedactedContent
                    }
                    if (needsToPausePlayer) {
                        withContext(dispatchers.main) { mediaPlayer.pause() }
                    }
                }
            }
        }
    }
}
