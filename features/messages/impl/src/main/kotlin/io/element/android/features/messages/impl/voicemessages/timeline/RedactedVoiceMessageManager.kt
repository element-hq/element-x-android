/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
